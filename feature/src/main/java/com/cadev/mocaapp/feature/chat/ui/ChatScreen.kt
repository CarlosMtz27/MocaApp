package com.cadev.mocaapp.feature.chat.ui

import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.cadev.mocaapp.feature.chat.domain.model.EstadoMensaje
import com.cadev.mocaapp.feature.chat.domain.model.Mensaje
import com.cadev.mocaapp.feature.chat.domain.model.TipoMensaje
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    usuarioId: String,
    parejaId: String,
    nombrePareja: String,
    fotoPareja: String?,
    onRegresar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(usuarioId, parejaId) {
        viewModel.inicializar(usuarioId, parejaId)
    }

    LaunchedEffect(uiState.mensajes.size) {
        if (uiState.mensajes.isNotEmpty()) {
            listState.animateScrollToItem(uiState.mensajes.size - 1)
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.marcarComoLeido()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    //Estados locales
    var grabandoAudio by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var archivoAudio by remember { mutableStateOf<File?>(null) }
    var tiempoGrabacion by remember { mutableStateOf(0) }
    var mostrarMenuMedia by remember { mutableStateOf(false) }
    var fotoVisor by remember { mutableStateOf<String?>(null) }
    var accionPendiente by remember { mutableStateOf<String?>(null) }
    var uriCameraTemp by remember { mutableStateOf<Uri?>(null) }
    var uriVideoTemp by remember { mutableStateOf<Uri?>(null) }

    //Helper URI temporal
    fun crearUri(carpeta: String, ext: String): Uri {
        val dir = File(context.cacheDir, carpeta).also { it.mkdirs() }
        val archivo = File(dir, "${UUID.randomUUID()}.$ext")
        return FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", archivo
        )
    }

    //Launchers
    val launcherGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.enviarFoto(it.toString()) } }

    val launcherCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito) uriCameraTemp?.let { viewModel.enviarFoto(it.toString()) }
    }

    val launcherVideo = rememberLauncherForActivityResult(
        ActivityResultContracts.CaptureVideo()
    ) { exito ->
        if (exito) uriVideoTemp?.let { viewModel.enviarVideo(it.toString()) }
    }

    //Cámara: acción DENTRO del callback
    val launcherPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            when (accionPendiente) {
                "foto" -> {
                    val uri = crearUri("camera", "jpg")
                    uriCameraTemp = uri
                    launcherCamara.launch(uri)
                }
                "video" -> {
                    val uri = crearUri("video", "mp4")
                    uriVideoTemp = uri
                    launcherVideo.launch(uri)
                }
            }
            accionPendiente = null
        }
    }

    //Audio: grabación DENTRO del callback
    val launcherPermisoAudio = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            val dir = File(context.cacheDir, "audio").also { it.mkdirs() }
            val archivo = File(dir, "${UUID.randomUUID()}.m4a")
            archivoAudio = archivo
            tiempoGrabacion = 0
            try {
                mediaRecorder = MediaRecorder(context).apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(archivo.absolutePath)
                    prepare()
                    start()
                }
                grabandoAudio = true
                scope.launch {
                    while (grabandoAudio) {
                        delay(1000)
                        tiempoGrabacion++
                    }
                }
            } catch (e: Exception) {
                grabandoAudio = false
            }
        }
    }

    //Visor foto fullscreen
    if (fotoVisor != null) {
        Dialog(
            onDismissRequest = { fotoVisor = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = fotoVisor,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { fotoVisor = null },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Filled.Close, null, tint = Color.White)
                }
            }
        }
    }

    //Panel reacciones, eliminar
    if (uiState.mostrarReacciones && uiState.mensajeSeleccionado != null) {
        val esMio = uiState.mensajeSeleccionado!!.remitenteId == usuarioId
        AlertDialog(
            onDismissRequest = { viewModel.cerrarReacciones() },
            title = { Text("Reaccionar") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("❤️", "😂", "😮", "😢", "👍", "🔥").forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 28.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember {
                                            MutableInteractionSource()
                                        },
                                        indication = ripple()
                                    ) { viewModel.reaccionar(emoji) }
                                    .padding(8.dp)
                            )
                        }
                    }
                    if (esMio) {
                        HorizontalDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(
                                    interactionSource = remember {
                                        MutableInteractionSource()
                                    },
                                    indication = ripple()
                                ) {
                                    viewModel.eliminarMensaje(
                                        uiState.mensajeSeleccionado!!.id
                                    )
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Delete, null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                "Eliminar mensaje",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.cerrarReacciones() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    //Menú media
    if (mostrarMenuMedia) {
        AlertDialog(
            onDismissRequest = { mostrarMenuMedia = false },
            title = { Text("Enviar archivo") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Foto de galería") },
                        leadingContent = { Icon(Icons.Filled.PhotoLibrary, null) },
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple()
                        ) {
                            mostrarMenuMedia = false
                            launcherGaleria.launch("image/*")
                        }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Tomar foto") },
                        leadingContent = { Icon(Icons.Filled.PhotoCamera, null) },
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple()
                        ) {
                            mostrarMenuMedia = false
                            accionPendiente = "foto"
                            launcherPermisoCamara.launch(
                                android.Manifest.permission.CAMERA
                            )
                        }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Grabar video") },
                        leadingContent = { Icon(Icons.Filled.Videocam, null) },
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple()
                        ) {
                            mostrarMenuMedia = false
                            accionPendiente = "video"
                            launcherPermisoCamara.launch(
                                android.Manifest.permission.CAMERA
                            )
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarMenuMedia = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    //Scaffold principal
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!fotoPareja.isNullOrBlank()) {
                                AsyncImage(
                                    model = fotoPareja,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    nombrePareja.firstOrNull()?.uppercase() ?: "?",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Column {
                            Text(
                                nombrePareja,
                                style = MaterialTheme.typography.titleMedium
                            )
                            AnimatedVisibility(visible = uiState.parejaEscribiendo) {
                                BurbujaEscribiendo()
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            InputBar(
                texto = uiState.textoActual,
                enviando = uiState.enviando,
                grabando = grabandoAudio,
                tiempoGrabacion = tiempoGrabacion,
                onTextoChange = { viewModel.actualizarTexto(it) },
                onEnviar = { viewModel.enviarTexto() },
                onAbrirMedia = { mostrarMenuMedia = true },
                onIniciarGrabacion = {
                    launcherPermisoAudio.launch(
                        android.Manifest.permission.RECORD_AUDIO
                    )
                },
                onDetenerGrabacion = {
                    try {
                        mediaRecorder?.apply { stop(); release() }
                        mediaRecorder = null
                        grabandoAudio = false
                        archivoAudio?.let { archivo ->
                            viewModel.enviarAudio(
                                Uri.fromFile(archivo).toString(),
                                tiempoGrabacion
                            )
                        }
                    } catch (e: Exception) {
                        grabandoAudio = false
                    }
                },
                onCancelarGrabacion = {
                    try { mediaRecorder?.apply { stop(); release() } }
                    catch (e: Exception) { }
                    mediaRecorder = null
                    grabandoAudio = false
                    archivoAudio?.delete()
                    tiempoGrabacion = 0
                }
            )
        }
    ) { padding ->

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            val grupos = uiState.mensajes.groupBy { msg ->
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(msg.creadoEn.toDate())
            }

            grupos.forEach { (fecha, mensajesDelDia) ->
                item { FechaHeader(fecha = fecha) }
                items(items = mensajesDelDia, key = { it.id }) { mensaje ->
                    BurbujaMensaje(
                        mensaje = mensaje,
                        esMio = mensaje.remitenteId == usuarioId,
                        onLongPress = { viewModel.seleccionarMensaje(mensaje) },
                        onFotoClick = { fotoVisor = it },
                        onEnlaceClick = { url ->
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            )
                        }
                    )
                }
            }

            if (uiState.enviando) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

//Fecha separadora
@Composable
private fun FechaHeader(fecha: String) {
    val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val ayer = Calendar.getInstance()
        .apply { add(Calendar.DAY_OF_YEAR, -1) }
        .let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.time) }

    val texto = when (fecha) {
        hoy -> "Hoy"
        ayer -> "Ayer"
        else -> try {
            SimpleDateFormat("d 'de' MMMM", Locale("es", "MX")).format(
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fecha)!!
            )
        } catch (e: Exception) { fecha }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

//Burbuja de mensaje
@Composable
private fun BurbujaMensaje(
    mensaje: Mensaje,
    esMio: Boolean,
    onLongPress: () -> Unit,
    onFotoClick: (String) -> Unit,
    onEnlaceClick: (String) -> Unit
) {
    val hora = SimpleDateFormat("HH:mm", Locale.getDefault())
        .format(mensaje.creadoEn.toDate())

    val colorFondo = if (esMio)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    val colorTexto = if (esMio)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (esMio) 48.dp else 0.dp,
                end = if (esMio) 0.dp else 48.dp,
                top = 2.dp,
                bottom = 2.dp
            ),
        horizontalAlignment = if (esMio) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 80.dp, max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (esMio) 16.dp else 4.dp,
                        bottomEnd = if (esMio) 4.dp else 16.dp
                    )
                )
                .background(colorFondo)
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onLongPress() })
                }
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 12.dp, end = 12.dp,
                    top = 8.dp, bottom = 4.dp
                )
            ) {
                when (mensaje.tipo) {

                    //Foto
                    TipoMensaje.FOTO.name -> {
                        if (mensaje.mediaUrl.isNotBlank()) {
                            AsyncImage(
                                model = mensaje.mediaUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(
                                        interactionSource = remember {
                                            MutableInteractionSource()
                                        },
                                        indication = ripple()
                                    ) { onFotoClick(mensaje.mediaUrl) }
                            )
                        }
                    }

                    //Video
                    TipoMensaje.VIDEO.name -> {
                        if (mensaje.mediaUrl.isNotBlank()) {
                            // ← Reproductor completo con dialog
                            ReproductorVideo(url = mensaje.mediaUrl)
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Videocam, null, tint = colorTexto)
                                Text(
                                    "Cargando video...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorTexto
                                )
                            }
                        }
                    }

                    //Audio, Voz
                    TipoMensaje.AUDIO.name, TipoMensaje.VOZ.name -> {
                        if (mensaje.mediaUrl.isNotBlank()) {
                            //Reproductor con barra de progreso
                            ReproductorAudio(
                                url = mensaje.mediaUrl,
                                duracion = mensaje.duracionSegundos,
                                colorTexto = colorTexto
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Mic, null, tint = colorTexto)
                                Text(
                                    "Cargando audio...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorTexto
                                )
                            }
                        }
                    }

                    //Enlace
                    TipoMensaje.ENLACE.name -> {
                        Text(
                            text = mensaje.texto,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = TextDecoration.Underline
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable(
                                interactionSource = remember {
                                    MutableInteractionSource()
                                },
                                indication = ripple()
                            ) { onEnlaceClick(mensaje.texto) }
                        )
                    }

                    //Texto
                    else -> {
                        Text(
                            text = mensaje.texto,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorTexto
                        )
                    }
                }

                //Hora mas estado
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = hora,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = colorTexto.copy(alpha = 0.5f)
                    )
                    if (esMio) {
                        EstadoIcon(estado = mensaje.estado, color = colorTexto)
                    }
                }
            }
        }

        //Reacciones
        if (mensaje.reacciones.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(
                        start = if (esMio) 0.dp else 4.dp,
                        end = if (esMio) 4.dp else 0.dp,
                        bottom = 2.dp
                    )
                    .clip(RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(50.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                mensaje.reacciones.values.distinct().forEach { emoji ->
                    Text(emoji, fontSize = 14.sp)
                }
            }
        }
    }
}

//Icono de estado
@Composable
private fun EstadoIcon(estado: String, color: Color) {
    when (estado) {
        EstadoMensaje.ENVIANDO.name -> Icon(
            Icons.Filled.Schedule, null,
            modifier = Modifier.size(12.dp),
            tint = color.copy(alpha = 0.4f)
        )
        EstadoMensaje.ENVIADO.name -> Icon(
            Icons.Filled.Check, null,
            modifier = Modifier.size(12.dp),
            tint = color.copy(alpha = 0.5f)
        )
        EstadoMensaje.ENTREGADO.name -> Row {
            Icon(
                Icons.Filled.Check, null,
                modifier = Modifier.size(12.dp),
                tint = color.copy(alpha = 0.5f)
            )
            Icon(
                Icons.Filled.Check, null,
                modifier = Modifier.size(12.dp).offset(x = (-4).dp),
                tint = color.copy(alpha = 0.5f)
            )
        }
        EstadoMensaje.LEIDO.name -> Row {
            Icon(
                Icons.Filled.Check, null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Icon(
                Icons.Filled.Check, null,
                modifier = Modifier.size(12.dp).offset(x = (-4).dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

//Barra de input
@Composable
private fun InputBar(
    texto: String,
    enviando: Boolean,
    grabando: Boolean,
    tiempoGrabacion: Int,
    onTextoChange: (String) -> Unit,
    onEnviar: () -> Unit,
    onAbrirMedia: () -> Unit,
    onIniciarGrabacion: () -> Unit,
    onDetenerGrabacion: () -> Unit,
    onCancelarGrabacion: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!grabando) {
                IconButton(onClick = onAbrirMedia) {
                    Icon(
                        Icons.Filled.AttachFile, null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                OutlinedTextField(
                    value = texto,
                    onValueChange = onTextoChange,
                    placeholder = { Text("Mensaje...") },
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp)
                )

                if (texto.isNotBlank()) {
                    IconButton(
                        onClick = onEnviar,
                        enabled = !enviando,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.primary, CircleShape
                            )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send, null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onIniciarGrabacion,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Filled.Mic, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                //Modo grabacion
                IconButton(onClick = onCancelarGrabacion) {
                    Icon(
                        Icons.Filled.Delete, null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.FiberManualRecord, null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            "${tiempoGrabacion}s",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                IconButton(
                    onClick = onDetenerGrabacion,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.primary, CircleShape
                        )
                ) {
                    Icon(Icons.Filled.Stop, null, tint = Color.White)
                }
            }
        }
    }
}

//Reproductor de Video

@Composable
private fun ReproductorVideo(url: String) {
    val context = LocalContext.current
    var mostrarReproductor by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(200.dp, 120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ) { mostrarReproductor = true },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.PlayArrow, null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }

    if (mostrarReproductor) {
        Dialog(
            onDismissRequest = { mostrarReproductor = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                val exoPlayer = remember {
                    androidx.media3.exoplayer.ExoPlayer.Builder(context)
                        .build().apply {
                            setMediaItem(
                                androidx.media3.common.MediaItem.fromUri(url)
                            )
                            prepare()
                            playWhenReady = true
                        }
                }

                DisposableEffect(Unit) {
                    onDispose { exoPlayer.release() }
                }

                AndroidView(
                    factory = {
                        androidx.media3.ui.PlayerView(it).apply {
                            player = exoPlayer
                            useController = true
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = { mostrarReproductor = false },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Filled.Close, null, tint = Color.White)
                }
            }
        }
    }
}

//Reproductor de Audio, Voz
@Composable
private fun ReproductorAudio(
    url: String,
    duracion: Int,
    colorTexto: Color
) {
    val context = LocalContext.current
    var reproduciendo by remember { mutableStateOf(false) }
    var progreso by remember { mutableStateOf(0f) }
    var duracionReal by remember { mutableStateOf(duracion) }

    val exoPlayer = remember {
        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
            setMediaItem(androidx.media3.common.MediaItem.fromUri(url))
            prepare()
        }
    }

    LaunchedEffect(reproduciendo) {
        while (reproduciendo) {
            delay(200)
            val duration = exoPlayer.duration.takeIf { it > 0 } ?: 1L
            val position = exoPlayer.currentPosition
            progreso = position.toFloat() / duration.toFloat()
            duracionReal = (duration / 1000).toInt()

            if (exoPlayer.playbackState ==
                androidx.media3.common.Player.STATE_ENDED) {
                reproduciendo = false
                progreso = 0f
                exoPlayer.seekTo(0)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.width(200.dp)
    ) {
        IconButton(
            onClick = {
                if (reproduciendo) {
                    exoPlayer.pause()
                    reproduciendo = false
                } else {
                    exoPlayer.play()
                    reproduciendo = true
                }
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                if (reproduciendo) Icons.Filled.Pause
                else Icons.Filled.PlayArrow,
                null,
                tint = colorTexto,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(50.dp)),
                color = colorTexto,
                trackColor = colorTexto.copy(alpha = 0.2f)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = formatearDuracion(duracionReal),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = colorTexto.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun BurbujaEscribiendo() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    // Tres puntos con delay entre sí
    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0f at 0; 1f at 200; 0f at 400; 0f at 1200
            }
        ), label = "dot1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0f at 200; 1f at 400; 0f at 600; 0f at 1200
            }
        ), label = "dot2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0f at 400; 1f at 600; 0f at 800; 0f at 1200
            }
        ), label = "dot3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        listOf(dot1, dot2, dot3).forEach { alpha ->
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f + alpha * 0.6f)
                    )
                    .offset(y = (-2 * alpha).dp)
            )
        }
    }
}

private fun formatearDuracion(segundos: Int): String {
    val min = segundos / 60
    val seg = segundos % 60
    return "%d:%02d".format(min, seg)
}