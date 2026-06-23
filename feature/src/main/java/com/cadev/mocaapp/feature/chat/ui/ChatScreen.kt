package com.cadev.mocaapp.feature.chat.ui

import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.res.painterResource
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.cadev.mocaapp.feature.chat.domain.model.EstadoMensaje
import com.cadev.mocaapp.feature.chat.domain.model.Mensaje
import com.cadev.mocaapp.feature.chat.domain.model.ReaccionType
import com.cadev.mocaapp.feature.chat.domain.model.TipoMensaje
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * ESTA ES LA PANTALLA DE CONVERSACIÓN PRIVADA
 * 
 * Qué hace:
 * Aquí es donde nuestra pareja habla en tiempo real. Diseñamos burbujas para 
 * enviar textos, fotos, grabaciones de voz y vídeos. También permitimos 
 * reaccionar a los mensajes con dibujos divertidos.
 * 
 * Cómo lo podemos modificar:
 * Si queremos cambiar el color de las burbujas de los mensajes, debemos ir a la 
 * función `BurbujaMensajeMejorada` y ajustar los colores en `colorFondo`.
 */
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

    /**
     * Activamos la conexión al chat compartido en cuanto abrimos la pantalla.
     */
    LaunchedEffect(usuarioId, parejaId) {
        viewModel.inicializar(usuarioId, parejaId)
    }

    /**
     * Hacemos que la conversación baje automáticamente al último mensaje recibido.
     */
    LaunchedEffect(uiState.mensajes.size) {
        if (uiState.mensajes.isNotEmpty()) {
            listState.animateScrollToItem(uiState.mensajes.size - 1)
        }
    }

    /**
     * Marcamos los mensajes como leídos cuando volvemos a abrir la conversación.
     */
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.marcarComoLeido()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }


    var grabandoAudio by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var archivoAudio by remember { mutableStateOf<File?>(null) }
    var tiempoGrabacion by remember { mutableStateOf(0) }
    var mostrarMenuMedia by remember { mutableStateOf(false) }
    var fotoVisor by remember { mutableStateOf<String?>(null) }
    var accionPendiente by remember { mutableStateOf<String?>(null) }
    var uriCameraTemp by remember { mutableStateOf<Uri?>(null) }
    var uriVideoTemp by remember { mutableStateOf<Uri?>(null) }

    /**
     * Crea un archivo vacío para guardar archivos multimedia temporales
     */
    fun crearUri(carpeta: String, ext: String): Uri {
        val dir = File(context.cacheDir, carpeta).also { it.mkdirs() }
        val archivo = File(dir, "${UUID.randomUUID()}.$ext")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }

    /**
     * Gestores para elegir o capturar fotos y vídeos
     */
    val launcherGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.enviarFoto(it.toString()) }
    }
    val launcherCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (exito) uriCameraTemp?.let { viewModel.enviarFoto(it.toString()) }
    }
    val launcherVideo = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { exito ->
        if (exito) uriVideoTemp?.let { viewModel.enviarVideo(it.toString()) }
    }
    val launcherPermisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) {
            when (accionPendiente) {
                "foto" -> { val uri = crearUri("camera", "jpg"); uriCameraTemp = uri; launcherCamara.launch(uri) }
                "video" -> { val uri = crearUri("video", "mp4"); uriVideoTemp = uri; launcherVideo.launch(uri) }
            }
            accionPendiente = null
        }
    }

    /**
     * Gestor para realizar grabaciones de voz usando el micrófono
     */
    val launcherPermisoAudio = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
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
                    prepare(); start()
                }
                grabandoAudio = true
                scope.launch { while (grabandoAudio) { delay(1000); tiempoGrabacion++ } }
            } catch (e: Exception) { grabandoAudio = false }
        }
    }

    /**
     * Visor para ampliar las fotos recibidas a pantalla completa
     */
    if (fotoVisor != null) {
        Dialog(onDismissRequest = { fotoVisor = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                AsyncImage(model = fotoVisor, contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                IconButton(onClick = { fotoVisor = null }, modifier = Modifier.align(Alignment.TopStart).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)) {
                    Icon(Icons.Filled.Close, null, tint = Color.White)
                }
            }
        }
    }

    /**
     * Menú flotante que aparece al mantener pulsado un mensaje para reaccionar
     */
    if (uiState.mostrarReacciones && uiState.mensajeSeleccionado != null) {
        ReactionBar(
            onReactionSelected = { viewModel.reaccionar(it) },
            onDelete = if (uiState.mensajeSeleccionado!!.remitenteId == usuarioId) {
                { viewModel.eliminarMensaje(uiState.mensajeSeleccionado!!.id) }
            } else null,
            onDismiss = { viewModel.cerrarReacciones() }
        )
    }

    /**
     * Menú para elegir si compartir algo de la galería o usar la cámara
     */
    if (mostrarMenuMedia) {
        MediaMenu(
            onGallery = { launcherGaleria.launch("image/*") },
            onCamera = { accionPendiente = "foto"; launcherPermisoCamara.launch(android.Manifest.permission.CAMERA) },
            onVideo = { accionPendiente = "video"; launcherPermisoCamara.launch(android.Manifest.permission.CAMERA) },
            onDismiss = { mostrarMenuMedia = false }
        )
    }

    Scaffold(
        topBar = {
            /**
             * Barra superior con el nombre la foto y el estado de la pareja (escribiendo o conectado)
             */
            ChatTopBar(
                nombre = nombrePareja,
                foto = fotoPareja,
                escribiendo = uiState.parejaEscribiendo,
                onBack = onRegresar
            )
        },
        bottomBar = {
            /**
             * Barra inferior con el cuadro de texto y el botón de grabación de voz
             */
            InputBarMejorada(
                texto = uiState.textoActual,
                enviando = uiState.enviando,
                grabando = grabandoAudio,
                tiempoGrabacion = tiempoGrabacion,
                onTextoChange = { viewModel.actualizarTexto(it) },
                onEnviar = { viewModel.enviarTexto() },
                onAbrirMedia = { mostrarMenuMedia = true },
                onIniciarGrabacion = { launcherPermisoAudio.launch(android.Manifest.permission.RECORD_AUDIO) },
                onDetenerGrabacion = {
                    try {
                        mediaRecorder?.apply { stop(); release() }
                        mediaRecorder = null
                        grabandoAudio = false
                        archivoAudio?.let { viewModel.enviarAudio(Uri.fromFile(it).toString(), tiempoGrabacion) }
                    } catch (e: Exception) { grabandoAudio = false }
                },
                onCancelarGrabacion = {
                    try { mediaRecorder?.apply { stop(); release() } } catch (e: Exception) { }
                    mediaRecorder = null; grabandoAudio = false; archivoAudio?.delete(); tiempoGrabacion = 0
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f))
                    )
                )
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                val grupos = uiState.mensajes.groupBy { msg ->
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(msg.creadoEn.toDate())
                }

                /**
                 * Se agrupan los mensajes por día para mostrar separadores de fecha
                 */
                grupos.forEach { (fecha, mensajesDelDia) ->
                    item { FechaHeader(fecha = fecha) }
                    items(items = mensajesDelDia, key = { it.id }) { mensaje ->
                        BurbujaMensajeMejorada(
                            mensaje = mensaje,
                            esMio = mensaje.remitenteId == usuarioId,
                            onLongPress = { viewModel.seleccionarMensaje(mensaje) },
                            onFotoClick = { fotoVisor = it },
                            onEnlaceClick = { url -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                        )
                    }
                }

                /**
                 * Indicador de que un mensaje pesado se está enviando a la nube
                 */
                if (uiState.enviando) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.CenterEnd) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Función que dibuja la cabecera del chat con la información de la pareja
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(nombre: String, foto: String?, escribiendo: Boolean, onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)) {
                    if (!foto.isNullOrBlank()) {
                        AsyncImage(model = foto, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(nombre.firstOrNull()?.uppercase() ?: "?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.Start) {
                    Text(nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    AnimatedContent(targetState = escribiendo, label = "Status") { isTyping ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (isTyping) {
                                Text("escribiendo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                BurbujaEscribiendo()
                            } else {
                                Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                                Text("conectados", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar") }
        },
        actions = {
            IconButton(onClick = { }) { Icon(Icons.Default.MoreVert, null) }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

/**
 * Función que crea la burbuja visual para cada mensaje adaptando su forma según quién lo envía
 */
@Composable
private fun BurbujaMensajeMejorada(
    mensaje: Mensaje,
    esMio: Boolean,
    onLongPress: () -> Unit,
    onFotoClick: (String) -> Unit,
    onEnlaceClick: (String) -> Unit
) {
    val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(mensaje.creadoEn.toDate())
    val colorFondo = if (esMio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
    val colorTexto = if (esMio) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer

    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        contentAlignment = if (esMio) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(horizontalAlignment = if (esMio) Alignment.End else Alignment.Start) {
            Surface(
                modifier = Modifier.widthIn(max = 280.dp).pointerInput(Unit) { detectTapGestures(onLongPress = { onLongPress() }) },
                shape = RoundedCornerShape(
                    topStart = 20.dp, topEnd = 20.dp,
                    bottomStart = if (esMio) 20.dp else 4.dp,
                    bottomEnd = if (esMio) 4.dp else 20.dp
                ),
                color = colorFondo
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    /**
                     * Se elige el diseño según si el mensaje es texto foto vídeo o audio
                     */
                    when (mensaje.tipo) {
                        TipoMensaje.FOTO.name -> AsyncImage(model = mensaje.mediaUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(220.dp).clip(RoundedCornerShape(12.dp)).clickable { onFotoClick(mensaje.mediaUrl) })
                        TipoMensaje.VIDEO.name -> ReproductorVideo(url = mensaje.mediaUrl)
                        TipoMensaje.AUDIO.name, TipoMensaje.VOZ.name -> ReproductorAudio(url = mensaje.mediaUrl, duracion = mensaje.duracionSegundos, colorTexto = colorTexto)
                        TipoMensaje.ENLACE.name -> Text(text = mensaje.texto, style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline), color = if (esMio) Color.White else MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onEnlaceClick(mensaje.texto) })
                        else -> Text(text = mensaje.texto, style = MaterialTheme.typography.bodyLarge, color = colorTexto)
                    }
                    Row(modifier = Modifier.align(Alignment.End).padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = hora, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = colorTexto.copy(alpha = 0.6f))
                        /**
                         * Indica si el mensaje ha sido enviado recibido o leído
                         */
                        if (esMio) EstadoIcon(estado = mensaje.estado, color = colorTexto)
                    }
                }
            }
            if (mensaje.reacciones.isNotEmpty()) {
                ReaccionesBadge(reacciones = mensaje.reacciones.values.toList(), esMio = esMio)
            }
        }
    }
}

/**
 * Pequeña animación de puntos suspensivos para indicar que la pareja está tecleando
 */
@Composable
private fun BurbujaEscribiendo() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val dot1 by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = keyframes { durationMillis = 1000; 0f at 0; 1f at 200; 0f at 400; 0f at 1000 }), label = "dot1")
    val dot2 by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = keyframes { durationMillis = 1000; 0f at 200; 1f at 400; 0f at 600; 0f at 1000 }), label = "dot2")
    val dot3 by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = keyframes { durationMillis = 1000; 0f at 400; 1f at 600; 0f at 800; 0f at 1000 }), label = "dot3")

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        listOf(dot1, dot2, dot3).forEach { alpha ->
            Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f + alpha * 0.7f)).offset(y = (-1 * alpha).dp))
        }
    }
}

/**
 * Función que crea la barra donde el usuario escribe sus mensajes y gestiona el envío
 */
@Composable
private fun InputBarMejorada(
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
    Surface(modifier = Modifier.fillMaxWidth().navigationBarsPadding(), tonalElevation = 2.dp, color = MaterialTheme.colorScheme.surface) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!grabando) {
                IconButton(onClick = onAbrirMedia, modifier = Modifier.padding(bottom = 4.dp)) { Icon(Icons.Default.AddCircleOutline, null, tint = MaterialTheme.colorScheme.primary) }
                TextField(
                    value = texto, onValueChange = onTextoChange, placeholder = { Text("Escribe algo lindo...") },
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(24.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent
                    ),
                    maxLines = 5, shape = RoundedCornerShape(24.dp)
                )
                /**
                 * El botón cambia entre enviar texto y grabar voz según si hay algo escrito
                 */
                AnimatedContent(targetState = texto.isNotBlank() || enviando, label = "SendIcon") { isActive ->
                    if (isActive) {
                        FloatingActionButton(
                            onClick = { if (!enviando) onEnviar() },
                            modifier = Modifier.size(48.dp), shape = CircleShape,
                            containerColor = if (enviando) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            if (enviando) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            else Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(20.dp))
                        }
                    } else {
                        IconButton(onClick = onIniciarGrabacion, modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), CircleShape)) { Icon(Icons.Default.Mic, null, tint = MaterialTheme.colorScheme.primary) }
                    }
                }
            } else {
                /**
                 * Diseño que aparece mientras se está grabando un audio de voz
                 */
                Row(modifier = Modifier.fillMaxWidth().height(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(onClick = onCancelarGrabacion) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val alpha by rememberInfiniteTransition().animateFloat(initialValue = 1f, targetValue = 0.2f, animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse), label = "alpha")
                        Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.error.copy(alpha = alpha)))
                        Text("Grabando... ${tiempoGrabacion}s", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                    }
                    IconButton(onClick = onDetenerGrabacion, modifier = Modifier.background(MaterialTheme.colorScheme.error, CircleShape)) { Icon(Icons.Default.Stop, null, tint = Color.White) }
                }
            }
        }
    }
}

/**
 * Función que dibuja el selector de dibujos para reaccionar a los mensajes
 */
@Composable
private fun ReactionBar(onReactionSelected: (ReaccionType) -> Unit, onDelete: (() -> Unit)?, onDismiss: () -> Unit) {
    val reacciones = listOf(
        ReaccionType.CORAZON to com.cadev.mocaapp.feature.R.drawable.ic_reaccion_corazon,
        ReaccionType.RISA to com.cadev.mocaapp.feature.R.drawable.ic_reaccion_risa,
        ReaccionType.ASOMBRO to com.cadev.mocaapp.feature.R.drawable.ic_reaccion_asombro,
        ReaccionType.TRISTE to com.cadev.mocaapp.feature.R.drawable.ic_reaccion_triste,
        ReaccionType.LIKE to com.cadev.mocaapp.feature.R.drawable.ic_reaccion_like,
        ReaccionType.FUEGO to com.cadev.mocaapp.feature.R.drawable.ic_reaccion_fuego
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), tonalElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    reacciones.forEach { (tipo, iconRes) ->
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = tipo.id,
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { onReactionSelected(tipo); onDismiss() }
                                .padding(4.dp)
                        )
                    }
                }
                if (onDelete != null) {
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = { onDelete(); onDismiss() }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Icon(Icons.Default.Delete, null); Spacer(Modifier.width(8.dp)); Text("Eliminar mensaje")
                    }
                }
            }
        }
    }
}

/**
 * Pequeña etiqueta con los dibujos elegidos por la pareja para un mensaje
 */
@Composable
private fun ReaccionesBadge(reacciones: List<String>, esMio: Boolean) {
    val mapaIconos = mapOf(
        ReaccionType.CORAZON.id to com.cadev.mocaapp.feature.R.drawable.ic_reaccion_corazon,
        ReaccionType.RISA.id to com.cadev.mocaapp.feature.R.drawable.ic_reaccion_risa,
        ReaccionType.ASOMBRO.id to com.cadev.mocaapp.feature.R.drawable.ic_reaccion_asombro,
        ReaccionType.TRISTE.id to com.cadev.mocaapp.feature.R.drawable.ic_reaccion_triste,
        ReaccionType.LIKE.id to com.cadev.mocaapp.feature.R.drawable.ic_reaccion_like,
        ReaccionType.FUEGO.id to com.cadev.mocaapp.feature.R.drawable.ic_reaccion_fuego
    )

    Row(
        modifier = Modifier
            .offset(y = (-8).dp)
            .padding(start = if (esMio) 0.dp else 8.dp, end = if (esMio) 8.dp else 0.dp)
            .shadow(2.dp, CircleShape)
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                CircleShape
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        reacciones.distinct().forEach { reaccionId -> 
            val iconRes = mapaIconos[reaccionId]
            if (iconRes != null) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(14.dp).padding(horizontal = 1.dp)
                )
            } else {
                // Si por alguna razón hay un emoji viejo en la base de datos, lo intentamos traducir o mostrar
                val translatedIcon = when(reaccionId) {
                    "❤️" -> com.cadev.mocaapp.feature.R.drawable.ic_reaccion_corazon
                    "😂" -> com.cadev.mocaapp.feature.R.drawable.ic_reaccion_risa
                    "😮" -> com.cadev.mocaapp.feature.R.drawable.ic_reaccion_asombro
                    "😢" -> com.cadev.mocaapp.feature.R.drawable.ic_reaccion_triste
                    "👍" -> com.cadev.mocaapp.feature.R.drawable.ic_reaccion_like
                    "🔥" -> com.cadev.mocaapp.feature.R.drawable.ic_reaccion_fuego
                    else -> null
                }
                if (translatedIcon != null) {
                    Icon(
                        painter = painterResource(id = translatedIcon),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(14.dp).padding(horizontal = 1.dp)
                    )
                } else {
                    Text(reaccionId, fontSize = 12.sp)
                }
            }
        }
    }
}



/**
 * Función que crea el menú desplegable para elegir qué tipo de archivo enviar
 */
@Composable
private fun MediaMenu(onGallery: () -> Unit, onCamera: () -> Unit, onVideo: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), tonalElevation = 8.dp) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Compartir", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                MediaOption(Icons.Default.PhotoLibrary, "Galería", "Busca esa foto especial", onGallery)
                MediaOption(Icons.Default.PhotoCamera, "Cámara", "Captura el momento", onCamera)
                MediaOption(Icons.Default.Videocam, "Video", "Graba un recuerdo", onVideo)
            }
        }
    }
}

/**
 * Función auxiliar para dibujar cada una de las opciones del menú de archivos
 */
@Composable
private fun MediaOption(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) }
        Column { Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) }
    }
}

/**
 * Función que dibuja el separador de día con un formato de fecha fácil de entender
 */
@Composable
private fun FechaHeader(fecha: String) {
    val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val texto = if (fecha == hoy) "Hoy" else { try { SimpleDateFormat("d 'de' MMMM", Locale.forLanguageTag("es-MX")).format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fecha)!!) } catch (e: Exception) { fecha } }
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(50)) { Text(text = texto, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

/**
 * Función que muestra los iconos de confirmación de envío recepción y lectura de los mensajes
 */
@Composable
private fun EstadoIcon(estado: String, color: Color) {
    val icon = when (estado) { EstadoMensaje.ENVIANDO.name -> Icons.Default.Schedule; EstadoMensaje.ENVIADO.name -> Icons.Default.Check; EstadoMensaje.ENTREGADO.name -> Icons.Default.DoneAll; EstadoMensaje.LEIDO.name -> Icons.Default.DoneAll; else -> Icons.Default.Check }
    val tint = if (estado == EstadoMensaje.LEIDO.name) MaterialTheme.colorScheme.inversePrimary else color.copy(alpha = 0.5f)
    Icon(icon, null, modifier = Modifier.size(14.dp), tint = tint)
}

/**
 * Función que crea un pequeño reproductor para ver los vídeos recibidos dentro del chat
 */
@Composable
private fun ReproductorVideo(url: String) {
    val context = LocalContext.current
    var mostrarReproductor by remember { mutableStateOf(false) }
    Box(modifier = Modifier.size(220.dp, 140.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black).clickable { mostrarReproductor = true }, contentAlignment = Alignment.Center) {
        AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(), alpha = 0.7f)
        Icon(Icons.Filled.PlayCircle, null, tint = Color.White, modifier = Modifier.size(48.dp))
    }
    if (mostrarReproductor) {
        Dialog(onDismissRequest = { mostrarReproductor = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                val exoPlayer = remember { androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply { setMediaItem(androidx.media3.common.MediaItem.fromUri(url)); prepare(); playWhenReady = true } }
                DisposableEffect(Unit) { onDispose { exoPlayer.release() } }
                AndroidView(factory = { androidx.media3.ui.PlayerView(it).apply { player = exoPlayer; useController = true } }, modifier = Modifier.fillMaxSize())
                IconButton(onClick = { mostrarReproductor = false }, modifier = Modifier.align(Alignment.TopStart).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)) { Icon(Icons.Filled.Close, null, tint = Color.White) }
            }
        }
    }
}

/**
 * Función que crea los controles para escuchar los mensajes de voz con barra de progreso
 */
@Composable
private fun ReproductorAudio(url: String, duracion: Int, colorTexto: Color) {
    val context = LocalContext.current
    var reproduciendo by remember { mutableStateOf(false) }
    var progreso by remember { mutableStateOf(0f) }
    var duracionReal by remember { mutableStateOf(duracion) }
    val exoPlayer = remember { androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply { setMediaItem(androidx.media3.common.MediaItem.fromUri(url)); prepare() } }
    LaunchedEffect(reproduciendo) {
        while (reproduciendo) {
            delay(200); val duration = exoPlayer.duration.takeIf { it > 0 } ?: 1L
            progreso = exoPlayer.currentPosition.toFloat() / duration.toFloat(); duracionReal = (duration / 1000).toInt()
            if (exoPlayer.playbackState == androidx.media3.common.Player.STATE_ENDED) { reproduciendo = false; progreso = 0f; exoPlayer.seekTo(0) }
        }
    }
    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.width(200.dp)) {
        IconButton(onClick = { if (reproduciendo) { exoPlayer.pause(); reproduciendo = false } else { exoPlayer.play(); reproduciendo = true } }, modifier = Modifier.size(36.dp)) { Icon(if (reproduciendo) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle, null, tint = colorTexto, modifier = Modifier.size(32.dp)) }
        Column(modifier = Modifier.weight(1f)) {
            LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape), color = colorTexto, trackColor = colorTexto.copy(alpha = 0.2f))
            Text(text = "%d:%02d".format(duracionReal / 60, duracionReal % 60), style = MaterialTheme.typography.labelSmall, color = colorTexto.copy(alpha = 0.6f))
        }
    }
}
