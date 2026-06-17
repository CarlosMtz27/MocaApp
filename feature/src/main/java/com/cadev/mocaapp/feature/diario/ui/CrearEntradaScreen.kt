package com.cadev.mocaapp.feature.diario.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.cadev.mocaapp.core.model.TipoEvento
import com.cadev.mocaapp.feature.diario.domain.model.Emocion
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearEntradaScreen(
    viewModel: DiarioViewModel,
    usuarioId: String,
    parejaId: String?,
    fecha: String,
    tipo: String = TipoEntrada.MI_DIA.name,
    onEntradaGuardada: () -> Unit,
    onRegresar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val tipoEntrada = try {
        TipoEntrada.valueOf(tipo)
    } catch (e: Exception) {
        TipoEntrada.MI_DIA
    }

    // Formato de fecha legible
    val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatoVisible = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "MX"))
    val fechaVisible = try {
        formatoVisible.format(formatoEntrada.parse(fecha)!!)
            .replaceFirstChar { it.uppercase() }
    } catch (e: Exception) { fecha }

    // URIs temporales para cámara y video
    var uriCameraTemp by remember { mutableStateOf<Uri?>(null) }
    var uriVideoTemp by remember { mutableStateOf<Uri?>(null) }
    var mostrarDialogoMedia by remember { mutableStateOf(false) }

    // Acción pendiente hasta que se conceda el permiso
    var accionPendiente by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Función para crear URI temporal
    fun crearUriTemporal(carpeta: String, extension: String): Uri {
        val dir = File(context.cacheDir, carpeta).also { it.mkdirs() }
        val archivo = File(dir, "${UUID.randomUUID()}.$extension")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )
    }

    // Launchers
    val launcherPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            accionPendiente?.invoke()
        }
        accionPendiente = null
    }

    fun pedirPermisoYEjecutar(accion: () -> Unit) {
        accionPendiente = accion
        launcherPermisoCamara.launch(android.Manifest.permission.CAMERA)
    }

    val launcherGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri -> viewModel.agregarFoto(uri.toString()) }
    }

    val launcherCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito) {
            uriCameraTemp?.let { viewModel.agregarFoto(it.toString()) }
        }
    }

    val launcherVideo = rememberLauncherForActivityResult(
        ActivityResultContracts.CaptureVideo()
    ) { exito ->
        if (exito) {
            uriVideoTemp?.let { viewModel.agregarVideo(it.toString()) }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.limpiarFormulario()
        if (tipoEntrada == TipoEntrada.RECUERDO && parejaId != null) {
            viewModel.toggleCompartir()
        }
    }

    LaunchedEffect(uiState.entradaCreada) {
        if (uiState.entradaCreada) onEntradaGuardada()
    }

    if (mostrarDialogoMedia) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoMedia = false },
            title = { Text("Agregar media") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                mostrarDialogoMedia = false
                                launcherGaleria.launch("image/*")
                            }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.PhotoLibrary, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Galería de fotos")
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                mostrarDialogoMedia = false
                                pedirPermisoYEjecutar {
                                    val uri = crearUriTemporal("camera", "jpg")
                                    uriCameraTemp = uri
                                    launcherCamara.launch(uri)
                                }
                            }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.PhotoCamera, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Tomar foto")
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                mostrarDialogoMedia = false
                                pedirPermisoYEjecutar {
                                    val uri = crearUriTemporal("video", "mp4")
                                    uriVideoTemp = uri
                                    launcherVideo.launch(uri)
                                }
                            }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Videocam, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Grabar video")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarDialogoMedia = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${tipoEntrada.emoji} ${tipoEntrada.etiqueta}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = fechaVisible,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.guardarEntrada(usuarioId, parejaId, fecha, tipo) },
                        enabled = !uiState.cargando
                    ) {
                        if (uiState.cargando) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Check, "Guardar", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Etiquetas (Solo si es RECUERDO)
            if (tipoEntrada == TipoEntrada.RECUERDO) {
                SeccionEtiquetas(
                    etiquetaSeleccionada = uiState.etiqueta,
                    etiquetaPersonalizada = uiState.etiquetaPersonalizada,
                    onEtiquetaSeleccionada = { viewModel.actualizarEtiqueta(it) },
                    onEtiquetaPersonalizada = { viewModel.actualizarEtiquetaPersonalizada(it) }
                )
            }

            OutlinedTextField(
                value = uiState.titulo,
                onValueChange = { viewModel.actualizarTitulo(it) },
                label = {
                    Text(
                        when (tipoEntrada) {
                            TipoEntrada.MI_DIA -> "¿Cómo fue tu día?"
                            TipoEntrada.RECUERDO -> "¿Qué quieres recordar?"
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            SeccionEmociones(
                emocionesSeleccionadas = uiState.emocionesSeleccionadas,
                onToggle = { viewModel.toggleEmocion(it) }
            )

            OutlinedTextField(
                value = uiState.detalles,
                onValueChange = { viewModel.actualizarDetalles(it) },
                label = { Text("Cuéntame más...") },
                modifier = Modifier.fillMaxWidth().height(140.dp),
                maxLines = 8
            )

            SeccionMedia(
                fotos = uiState.fotosSeleccionadas,
                videos = uiState.videosSeleccionados,
                onAgregarMedia = { mostrarDialogoMedia = true },
                onEliminarFoto = { viewModel.eliminarFoto(it) },
                onEliminarVideo = { viewModel.eliminarVideo(it) }
            )

            if (parejaId != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { viewModel.toggleCompartir() }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("💕 Compartir con mi pareja", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (uiState.compartir) "Tu pareja podrá ver esto" else "Solo tú puedes verlo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(checked = uiState.compartir, onCheckedChange = { viewModel.toggleCompartir() })
                }
            }

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SeccionEtiquetas(
    etiquetaSeleccionada: String,
    etiquetaPersonalizada: String,
    onEtiquetaSeleccionada: (String) -> Unit,
    onEtiquetaPersonalizada: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("¿De qué se trata?", style = MaterialTheme.typography.titleMedium)

        val filas = TipoEvento.entries.chunked(3)
        filas.forEach { fila ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                fila.forEach { tipo ->
                    val seleccionada = etiquetaSeleccionada == tipo.name
                    Box(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (seleccionada) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (seleccionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(50.dp))
                                .clickable { onEtiquetaSeleccionada(tipo.name) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = tipo.emoji, fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = tipo.etiqueta,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (seleccionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
                repeat(3 - fila.size) { Box(modifier = Modifier.weight(1f)) }
            }
        }

        if (etiquetaSeleccionada == TipoEvento.OTRO.name) {
            OutlinedTextField(
                value = etiquetaPersonalizada,
                onValueChange = onEtiquetaPersonalizada,
                label = { Text("¿Cuál?") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SeccionEmociones(
    emocionesSeleccionadas: List<Emocion>,
    onToggle: (Emocion) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("¿Cómo te sentiste?", style = MaterialTheme.typography.titleMedium)
        val filas = Emocion.entries.chunked(4)
        filas.forEach { fila ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                fila.forEach { emocion ->
                    val seleccionada = emocionesSeleccionadas.contains(emocion)
                    Box(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (seleccionada) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, if (seleccionada) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(12.dp))
                                .clickable { onToggle(emocion) }
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = emocion.emoji, fontSize = 22.sp)
                            Text(
                                text = emocion.etiqueta,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (seleccionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                repeat(4 - fila.size) { Box(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun SeccionMedia(
    fotos: List<String>,
    videos: List<String>,
    onAgregarMedia: () -> Unit,
    onEliminarFoto: (String) -> Unit,
    onEliminarVideo: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Fotos y videos", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onAgregarMedia) {
                Icon(Icons.Outlined.PhotoCamera, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Agregar")
            }
        }

        if (fotos.isEmpty() && videos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onAgregarMedia),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📷 Toca para agregar fotos o videos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            val todosLosArchivos = fotos.map { Pair(it, false) } + videos.map { Pair(it, true) }
            val filas = todosLosArchivos.chunked(3)
            filas.forEach { fila ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    fila.forEach { (uri, esVideo) ->
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(8.dp))) {
                            AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            if (esVideo) {
                                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.Videocam, "Video", tint = Color.White, modifier = Modifier.size(28.dp))
                                }
                            }
                            IconButton(
                                onClick = { if (esVideo) onEliminarVideo(uri) else onEliminarFoto(uri) },
                                modifier = Modifier.align(Alignment.TopEnd).size(28.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50.dp))
                            ) {
                                Icon(Icons.Filled.Close, "Eliminar", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    repeat(3 - fila.size) { Box(modifier = Modifier.weight(1f)) }
                }
            }
            TextButton(onClick = onAgregarMedia, modifier = Modifier.align(Alignment.End)) { Text("+ Agregar más") }
        }
    }
}
