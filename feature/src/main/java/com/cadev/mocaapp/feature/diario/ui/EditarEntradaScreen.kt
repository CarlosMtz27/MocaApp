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
import com.cadev.mocaapp.feature.diario.domain.model.Emocion
import com.cadev.mocaapp.feature.diario.domain.model.EtiquetaDiaEspecial
import com.cadev.mocaapp.feature.diario.domain.model.EtiquetaEvento
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarEntradaScreen(
    viewModel: DiarioViewModel,
    entradaId: String,
    parejaId: String?,
    onGuardado: () -> Unit,
    onRegresar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Cargar la entrada al entrar
    LaunchedEffect(entradaId) {
        viewModel.limpiarFormulario()
        viewModel.cargarEntradaParaEditar(entradaId)
    }

    // Navegar cuando se guarda
    LaunchedEffect(uiState.entradaActualizada) {
        if (uiState.entradaActualizada) onGuardado()
    }

    val entrada = uiState.entradaActual
    val tipoEntrada = try {
        TipoEntrada.valueOf(entrada?.tipo ?: TipoEntrada.MI_DIA.name)
    } catch (e: Exception) {
        TipoEntrada.MI_DIA
    }

    // URIs temporales
    var uriCameraTemp by remember { mutableStateOf<Uri?>(null) }
    var uriVideoTemp by remember { mutableStateOf<Uri?>(null) }
    var mostrarDialogoMedia by remember { mutableStateOf(false) }
    var accionPendiente by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun crearUriTemporal(carpeta: String, extension: String): Uri {
        val dir = File(context.cacheDir, carpeta).also { it.mkdirs() }
        val archivo = File(dir, "${UUID.randomUUID()}.$extension")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )
    }

    val launcherPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) accionPendiente?.invoke()
        accionPendiente = null
    }

    fun pedirPermisoYEjecutar(accion: () -> Unit) {
        accionPendiente = accion
        launcherPermiso.launch(android.Manifest.permission.CAMERA)
    }

    val launcherGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> uris.forEach { viewModel.agregarFoto(it.toString()) } }

    val launcherCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito) uriCameraTemp?.let { viewModel.agregarFoto(it.toString()) }
    }

    val launcherVideo = rememberLauncherForActivityResult(
        ActivityResultContracts.CaptureVideo()
    ) { exito ->
        if (exito) uriVideoTemp?.let { viewModel.agregarVideo(it.toString()) }
    }

    // Diálogo media
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
                        Icon(Icons.Outlined.PhotoLibrary, null,
                            tint = MaterialTheme.colorScheme.primary)
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
                        Icon(Icons.Outlined.PhotoCamera, null,
                            tint = MaterialTheme.colorScheme.primary)
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
                        Icon(Icons.Outlined.Videocam, null,
                            tint = MaterialTheme.colorScheme.primary)
                        Text("Grabar video")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarDialogoMedia = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "✏️ Editar ${tipoEntrada.etiqueta.lowercase()}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (entrada != null) {
                            Text(
                                text = entrada.fecha,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                                    .copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.guardarEdicion(parejaId) },
                        enabled = !uiState.cargando && entrada != null
                    ) {
                        if (uiState.cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Guardar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->

        if (uiState.cargando && entrada == null) {
            // Cargando la entrada por primera vez
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            //Etiquetas
            if (tipoEntrada == TipoEntrada.EVENTO ||
                tipoEntrada == TipoEntrada.DIA_ESPECIAL) {
                SeccionEtiquetasEditar(
                    tipo = tipoEntrada,
                    etiquetaSeleccionada = uiState.etiqueta,
                    etiquetaPersonalizada = uiState.etiquetaPersonalizada,
                    onEtiquetaSeleccionada = { viewModel.actualizarEtiqueta(it) },
                    onEtiquetaPersonalizada = {
                        viewModel.actualizarEtiquetaPersonalizada(it)
                    }
                )
            }

            //Título
            OutlinedTextField(
                value = uiState.titulo,
                onValueChange = { viewModel.actualizarTitulo(it) },
                label = {
                    Text(when (tipoEntrada) {
                        TipoEntrada.MI_DIA       -> "¿Cómo fue tu día?"
                        TipoEntrada.RECUERDO     -> "¿Cuál es el recuerdo?"
                        TipoEntrada.EVENTO       -> "Nombre del evento"
                        TipoEntrada.DIA_ESPECIAL -> "¿Qué celebran?"
                    })
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            //Emociones
            if (tipoEntrada != TipoEntrada.EVENTO) {
                SeccionEmocionesEditar(
                    emocionesSeleccionadas = uiState.emocionesSeleccionadas,
                    onToggle = { viewModel.toggleEmocion(it) }
                )
            }

            // Descripción
            OutlinedTextField(
                value = uiState.detalles,
                onValueChange = { viewModel.actualizarDetalles(it) },
                label = { Text("Cuéntame más...") },
                modifier = Modifier.fillMaxWidth().height(140.dp),
                maxLines = 8
            )

            //Media existente mas nueva
            SeccionMediaEditar(
                fotosExistentes = entrada?.fotos ?: emptyList(),
                videosExistentes = entrada?.videos ?: emptyList(),
                fotosNuevas = uiState.fotosSeleccionadas,
                videosNuevos = uiState.videosSeleccionados,
                onAgregarMedia = { mostrarDialogoMedia = true },
                onEliminarFotoExistente = { viewModel.eliminarFotoExistente(it) },
                onEliminarVideoExistente = { viewModel.eliminarVideoExistente(it) },
                onEliminarFotoNueva = { viewModel.eliminarFoto(it) },
                onEliminarVideoNuevo = { viewModel.eliminarVideo(it) }
            )

            //Compartir
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
                        Text(
                            text = "💕 Compartir con mi pareja",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (uiState.compartir)
                                "Tu pareja podrá ver esto"
                            else "Solo tú puedes verlo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                                .copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = uiState.compartir,
                        onCheckedChange = { viewModel.toggleCompartir() }
                    )
                }
            }

            //Error
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

// Sección media con dos grupos: existente y nueva

@Composable
private fun SeccionMediaEditar(
    fotosExistentes: List<String>,
    videosExistentes: List<String>,
    fotosNuevas: List<String>,
    videosNuevos: List<String>,
    onAgregarMedia: () -> Unit,
    onEliminarFotoExistente: (String) -> Unit,
    onEliminarVideoExistente: (String) -> Unit,
    onEliminarFotoNueva: (String) -> Unit,
    onEliminarVideoNuevo: (String) -> Unit
) {
    val hayMedia = fotosExistentes.isNotEmpty() || videosExistentes.isNotEmpty() ||
            fotosNuevas.isNotEmpty() || videosNuevos.isNotEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Fotos y videos", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onAgregarMedia) {
                Icon(Icons.Outlined.PhotoCamera, null,
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Agregar")
            }
        }

        if (!hayMedia) {
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
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            // Combinar todo en un solo grid
            // false = foto, true = video
            // "existente" vs "nueva" determina qué función de eliminar usar
            data class ItemMedia(
                val uri: String,
                val esVideo: Boolean,
                val esExistente: Boolean
            )

            val todosLosItems = fotosExistentes.map {
                ItemMedia(it, false, true)
            } + videosExistentes.map {
                ItemMedia(it, true, true)
            } + fotosNuevas.map {
                ItemMedia(it, false, false)
            } + videosNuevos.map {
                ItemMedia(it, true, false)
            }

            val filas = todosLosItems.chunked(3)
            filas.forEach { fila ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    fila.forEach { item ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = item.uri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Badge "Nueva" para fotos recién agregadas
                            if (!item.esExistente) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            MaterialTheme.colorScheme.primary
                                                .copy(alpha = 0.8f)
                                        )
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Nueva",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }

                            // Ícono video
                            if (item.esVideo) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.Videocam,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            // Botón eliminar
                            IconButton(
                                onClick = {
                                    when {
                                        item.esExistente && !item.esVideo ->
                                            onEliminarFotoExistente(item.uri)
                                        item.esExistente && item.esVideo ->
                                            onEliminarVideoExistente(item.uri)
                                        !item.esExistente && !item.esVideo ->
                                            onEliminarFotoNueva(item.uri)
                                        else ->
                                            onEliminarVideoNuevo(item.uri)
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(28.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.5f),
                                        RoundedCornerShape(50.dp)
                                    )
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Eliminar",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                    repeat(3 - fila.size) { Box(modifier = Modifier.weight(1f)) }
                }
            }

            TextButton(
                onClick = onAgregarMedia,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("+ Agregar más")
            }
        }
    }
}

// Etiquetas y emociones reutilizadas
// Son idénticas a CrearEntradaScreen — las extraemos para no duplicar

@Composable
private fun SeccionEtiquetasEditar(
    tipo: TipoEntrada,
    etiquetaSeleccionada: String,
    etiquetaPersonalizada: String,
    onEtiquetaSeleccionada: (String) -> Unit,
    onEtiquetaPersonalizada: (String) -> Unit
) {
    val etiquetas = when (tipo) {
        TipoEntrada.EVENTO ->
            EtiquetaEvento.entries.map { Pair(it.etiqueta, it.emoji) }
        TipoEntrada.DIA_ESPECIAL ->
            EtiquetaDiaEspecial.entries.map { Pair(it.etiqueta, it.emoji) }
        else -> emptyList()
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Tipo de ${tipo.etiqueta.lowercase()}",
            style = MaterialTheme.typography.titleMedium
        )
        etiquetas.chunked(3).forEach { fila ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                fila.forEach { (etiqueta, emoji) ->
                    val seleccionada = etiquetaSeleccionada == etiqueta
                    Box(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(50.dp))
                                .background(
                                    if (seleccionada)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface
                                )
                                .border(
                                    1.dp,
                                    if (seleccionada)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(50.dp)
                                )
                                .clickable { onEtiquetaSeleccionada(etiqueta) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(emoji, fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                etiqueta,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (seleccionada)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                repeat(3 - fila.size) { Box(modifier = Modifier.weight(1f)) }
            }
        }
        val labelPersonalizada = when (tipo) {
            TipoEntrada.EVENTO -> EtiquetaEvento.PERSONALIZADA.etiqueta
            TipoEntrada.DIA_ESPECIAL -> EtiquetaDiaEspecial.PERSONALIZADA.etiqueta
            else -> ""
        }
        if (etiquetaSeleccionada == labelPersonalizada) {
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
private fun SeccionEmocionesEditar(
    emocionesSeleccionadas: List<Emocion>,
    onToggle: (Emocion) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("¿Cómo te sentiste?", style = MaterialTheme.typography.titleMedium)
        Emocion.entries.chunked(4).forEach { fila ->
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
                                .background(
                                    if (seleccionada)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    1.dp,
                                    if (seleccionada)
                                        MaterialTheme.colorScheme.primary
                                    else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onToggle(emocion) }
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(emocion.emoji, fontSize = 22.sp)
                            Text(
                                emocion.etiqueta,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (seleccionada)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
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