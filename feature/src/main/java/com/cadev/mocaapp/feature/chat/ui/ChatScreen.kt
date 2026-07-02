package com.cadev.mocaapp.feature.chat.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Brush
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.cadev.mocaapp.feature.chat.domain.model.Mensaje
import com.cadev.mocaapp.feature.chat.ui.components.*
import com.cadev.mocaapp.feature.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * ESTA ES LA PANTALLA DE CONVERSACIÓN PRIVADA (VERSIÓN ORGÁNICA Y ANIMADA)
 * 
 * Qué hace:
 * Implementa la interfaz de chat privado fiel al HTML, con burbujas modernas,
 * animaciones de escritura y soporte para multimedia.
 */
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    usuarioId: String,
    usuarioNombre: String,
    usuarioFoto: String?,
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
        viewModel.inicializar(usuarioId, usuarioNombre, usuarioFoto, parejaId)
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

    fun crearUri(carpeta: String, ext: String): Uri {
        val dir = File(context.cacheDir, carpeta).also { it.mkdirs() }
        val archivo = File(dir, "${UUID.randomUUID()}.$ext")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }

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

    if (mostrarMenuMedia) {
        MediaMenu(
            onGallery = { 
                mostrarMenuMedia = false
                launcherGaleria.launch("image/*") 
            },
            onCamera = { 
                mostrarMenuMedia = false
                accionPendiente = "foto"
                launcherPermisoCamara.launch(android.Manifest.permission.CAMERA) 
            },
            onVideo = { 
                mostrarMenuMedia = false
                accionPendiente = "video"
                launcherPermisoCamara.launch(android.Manifest.permission.CAMERA) 
            },
            onDismiss = { mostrarMenuMedia = false }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            ChatHeader(
                nombre = nombrePareja,
                foto = fotoPareja,
                estaActivo = true,
                onBack = onRegresar
            )
        },
        bottomBar = {
            if (grabandoAudio) {
                RecordingBar(
                    tiempo = tiempoGrabacion,
                    onCancelar = {
                        try { mediaRecorder?.apply { stop(); release() } } catch (e: Exception) { }
                        mediaRecorder = null; grabandoAudio = false; archivoAudio?.delete(); tiempoGrabacion = 0
                    },
                    onDetener = {
                        try {
                            mediaRecorder?.apply { stop(); release() }
                            mediaRecorder = null
                            grabandoAudio = false
                            archivoAudio?.let { viewModel.enviarAudio(Uri.fromFile(it).toString(), tiempoGrabacion) }
                        } catch (e: Exception) { grabandoAudio = false }
                    }
                )
            } else {
                ChatInput(
                    texto = uiState.textoActual,
                    onTextoChange = { viewModel.actualizarTexto(it) },
                    onEnviar = { viewModel.enviarTexto() },
                    onMediaClick = { mostrarMenuMedia = true },
                    onMicClick = { launcherPermisoAudio.launch(android.Manifest.permission.RECORD_AUDIO) }
                )
            }
        }
    ) { padding ->
        val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .then(
                    if (isDark) Modifier.background(Color(0xFF1E1B14))
                    else Modifier.background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFF8EF), Color(0xFFFFD9E2).copy(alpha = 0.2f))
                        )
                    )
                )
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val grupos = uiState.mensajes.groupBy { msg ->
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(msg.creadoEn.toDate())
                }

                grupos.forEach { (fecha, mensajesDelDia) ->
                    item { 
                        FechaChatHeader(fecha = fecha)
                    }
                    items(items = mensajesDelDia, key = { it.id }) { mensaje ->
                        MensajeItem(
                            mensaje = mensaje,
                            esMio = mensaje.remitenteId == usuarioId,
                            fotoRemitente = if (mensaje.remitenteId == usuarioId) usuarioFoto else fotoPareja,
                            onFotoClick = { fotoVisor = it }
                        )
                    }
                }

                if (uiState.parejaEscribiendo) {
                    item {
                        TypingIndicator(nombre = nombrePareja)
                    }
                }

                if (uiState.enviando) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.CenterEnd) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MocaPrimary)
                        }
                    }
                }
                
                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun FechaChatHeader(fecha: String) {
    val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val texto = if (fecha == hoy) "Hoy" else { 
        try { 
            SimpleDateFormat("d 'de' MMMM", Locale.forLanguageTag("es-MX")).format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fecha)!!) 
        } catch (e: Exception) { fecha } 
    }
    
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = if (isDark) Color.White.copy(alpha = 0.05f) else MocaSurfaceVariant.copy(alpha = 0.3f),
            shape = CircleShape
        ) {
            Text(
                text = texto.uppercase(),
                style = OrganicTypography.labelSmall.copy(
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFD3C3C5) else MocaOnSurfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun RecordingBar(
    tiempo: Int,
    onCancelar: () -> Unit,
    onDetener: () -> Unit
) {
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme
    val surfaceColor = if (isDark) Color(0xFF2D2921) else MocaSurfaceContainerLowest

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding(),
        color = surfaceColor,
        shape = CircleShape,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onCancelar) {
                Icon(Icons.Default.Delete, "Cancelar", tint = MocaError)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val alpha by rememberInfiniteTransition(label = "").animateFloat(
                    initialValue = 1f,
                    targetValue = 0.2f,
                    animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse),
                    label = "alpha"
                )
                Box(Modifier.size(8.dp).clip(CircleShape).background(MocaError.copy(alpha = alpha)))
                Text(
                    text = "Grabando... %d:%02d".format(tiempo / 60, tiempo % 60),
                    style = OrganicTypography.labelLarge,
                    color = MocaError
                )
            }
            
            IconButton(
                onClick = onDetener,
                modifier = Modifier.background(MocaError, CircleShape)
            ) {
                Icon(Icons.Default.Stop, "Detener", tint = Color.White)
            }
        }
    }
}

@Composable
private fun MediaMenu(onGallery: () -> Unit, onCamera: () -> Unit, onVideo: () -> Unit, onDismiss: () -> Unit) {
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme
    val surfaceColor = if (isDark) Color(0xFF2D2921) else MocaSurfaceContainerLowest
    val onSurfaceColor = if (isDark) Color(0xFFEEE7DB) else MocaOnSurface

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp), 
            tonalElevation = 8.dp,
            color = surfaceColor
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Compartir", 
                    style = OrganicTypography.headlineMedium.copy(fontSize = 20.sp), 
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor
                )
                MediaOption(Icons.Default.PhotoLibrary, "Galería", "Busca esa foto especial", onGallery)
                MediaOption(Icons.Default.PhotoCamera, "Cámara", "Captura el momento", onCamera)
                MediaOption(Icons.Default.Videocam, "Video", "Graba un recuerdo", onVideo)
            }
        }
    }
}

@Composable
private fun MediaOption(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme
    val onSurfaceColor = if (isDark) Color(0xFFEEE7DB) else MocaOnSurface
    val primaryColor = if (isDark) Color(0xFFE7BBC6) else MocaPrimary
    val primaryContainer = if (isDark) Color(0xFF5E3E47) else MocaPrimaryContainer

    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(8.dp), 
        verticalAlignment = Alignment.CenterVertically, 
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            Modifier.size(48.dp).background(primaryContainer, CircleShape), 
            contentAlignment = Alignment.Center
        ) { 
            Icon(icon, null, tint = primaryColor) 
        }
        Column { 
            Text(
                title, 
                style = OrganicTypography.bodyLarge, 
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor
            )
            Text(
                subtitle, 
                style = OrganicTypography.bodySmall, 
                color = onSurfaceColor.copy(alpha = 0.5f)
            ) 
        }
    }
}
