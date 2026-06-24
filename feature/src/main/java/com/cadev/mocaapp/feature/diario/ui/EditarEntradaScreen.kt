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
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.cadev.mocaapp.core.model.TipoEvento
import com.cadev.mocaapp.feature.diario.domain.model.Emocion
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import java.io.File
import java.util.*

/**
 * ESTA ES LA PANTALLA PARA MODIFICAR RECUERDOS
 * 
 * Qué hace:
 * Aquí permitimos que el usuario cambie algo que ya guardó antes. Se pueden 
 * editar los textos, las emociones y añadir o quitar fotos y vídeos. Lo que 
 * se acaba de añadir aparece con una etiqueta de "Nueva".
 * 
 * Cómo lo podemos modificar:
 * Si queremos que no se puedan borrar fotos antiguas, debemos ocultar el botón 
 * de eliminar dentro de la función `SeccionMediaEditar` para los items existentes.
 */
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

    /**
     * Se limpia el formulario y se descarga el recuerdo elegido al entrar
     */
    LaunchedEffect(entradaId) {
        viewModel.limpiarFormulario()
        viewModel.cargarEntradaParaEditar(entradaId)
    }

    /**
     * Si los cambios se guardan bien se avisa para salir de esta pantalla
     */
    LaunchedEffect(uiState.entradaActualizada) {
        if (uiState.entradaActualizada) onGuardado()
    }

    val entrada = uiState.entradaActual
    val tipoEntrada = try {
        TipoEntrada.valueOf(entrada?.tipo ?: TipoEntrada.MI_DIA.name)
    } catch (e: Exception) {
        TipoEntrada.MI_DIA
    }

    var uriCameraTemp by remember { mutableStateOf<Uri?>(null) }
    var uriVideoTemp by remember { mutableStateOf<Uri?>(null) }
    var mostrarDialogoMedia by remember { mutableStateOf(false) }
    var accionPendiente by remember { mutableStateOf<(() -> Unit)?>(null) }

    /**
     * Crea un archivo temporal para guardar lo que capture la cámara
     */
    fun crearUriTemporal(carpeta: String, extension: String): Uri {
        val dir = File(context.cacheDir, carpeta).also { it.mkdirs() }
        val archivo = File(dir, "${UUID.randomUUID()}.$extension")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }

    /**
     * Gestor para solicitar permiso de cámara
     */
    val launcherPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) accionPendiente?.invoke()
        accionPendiente = null
    }

    /**
     * Pide permiso antes de abrir la cámara
     */
    fun pedirPermisoYEjecutar(accion: () -> Unit) {
        accionPendiente = accion
        launcherPermiso.launch(android.Manifest.permission.CAMERA)
    }

    /**
     * Selecciona archivos nuevos de la galería
     */
    val launcherGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> uris.forEach { viewModel.agregarFoto(it.toString()) } }

    /**
     * Toma una foto nueva con la cámara
     */
    val launcherCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito) uriCameraTemp?.let { viewModel.agregarFoto(it.toString()) }
    }

    /**
     * Graba un vídeo nuevo con la cámara
     */
    val launcherVideo = rememberLauncherForActivityResult(
        ActivityResultContracts.CaptureVideo()
    ) { exito ->
        if (exito) uriVideoTemp?.let { viewModel.agregarVideo(it.toString()) }
    }

    /**
     * Diálogo para elegir el origen de los nuevos archivos multimedia
     */
    if (mostrarDialogoMedia) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoMedia = false },
            title = { Text("Agregar media") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable {
                            mostrarDialogoMedia = false
                            launcherGaleria.launch("image/*")
                        }.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.PhotoLibrary, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Galería de fotos")
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable {
                            mostrarDialogoMedia = false
                            pedirPermisoYEjecutar {
                                val uri = crearUriTemporal("camera", "jpg")
                                uriCameraTemp = uri
                                launcherCamara.launch(uri)
                            }
                        }.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.PhotoCamera, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Tomar foto")
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable {
                            mostrarDialogoMedia = false
                            pedirPermisoYEjecutar {
                                val uri = crearUriTemporal("video", "mp4")
                                uriVideoTemp = uri
                                launcherVideo.launch(uri)
                            }
                        }.padding(12.dp),
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
                            text = "Editar ${tipoEntrada.etiqueta.lowercase()}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (entrada != null) {
                            Text(
                                text = entrada.fecha,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                    /**
                     * Botón para confirmar y guardar todos los cambios realizados
                     */
                    IconButton(
                        onClick = { viewModel.guardarEdicion(parejaId) },
                        enabled = !uiState.cargando && entrada != null
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

        /**
         * Pantalla de carga si todavía no se ha bajado la información del recuerdo
         */
        if (uiState.cargando && entrada == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
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
            /**
             * Selector de categoría si es un tipo de entrada de tipo recuerdo
             */
            if (tipoEntrada == TipoEntrada.RECUERDO) {
                SeccionEtiquetasEditar(
                    etiquetaSeleccionada = uiState.etiqueta,
                    etiquetaPersonalizada = uiState.etiquetaPersonalizada,
                    onEtiquetaSeleccionada = { viewModel.actualizarEtiqueta(it) },
                    onEtiquetaPersonalizada = { viewModel.actualizarEtiquetaPersonalizada(it) }
                )
            }

            /**
             * Campo para cambiar el título principal
             */
            OutlinedTextField(
                value = uiState.titulo,
                onValueChange = { viewModel.actualizarTitulo(it) },
                label = {
                    Text(
                        text = (when (tipoEntrada) {
                            TipoEntrada.MI_DIA -> "¿Cómo fue tu día?"
                            TipoEntrada.RECUERDO -> "¿Qué quieres recordar?"
                        }) + " *"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.error != null && uiState.titulo.isBlank(),
                supportingText = {
                    if (uiState.error != null && uiState.titulo.isBlank()) {
                        Text("El título no puede estar vacío", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            /**
             * Selector de emociones para modificar cómo te sentías ese día
             */
            SeccionEmocionesEditar(
                emocionesSeleccionadas = uiState.emocionesSeleccionadas,
                onToggle = { viewModel.toggleEmocion(it) }
            )

            /**
             * Campo de texto grande para modificar la historia o los detalles
             */
            OutlinedTextField(
                value = uiState.detalles,
                onValueChange = { viewModel.actualizarDetalles(it) },
                label = { Text("Cuéntame más... *") },
                modifier = Modifier.fillMaxWidth().height(140.dp),
                maxLines = 8,
                isError = uiState.error != null && uiState.detalles.isBlank(),
                supportingText = {
                    if (uiState.error != null && uiState.detalles.isBlank()) {
                        Text("Los detalles son obligatorios", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            /**
             * Gestión combinada de archivos viejos y archivos recién añadidos
             */
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

            /**
             * Opción para cambiar si el recuerdo es privado o compartido con la pareja
             */
            if (parejaId != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { viewModel.toggleCompartir() }.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = com.cadev.mocaapp.feature.R.drawable.ic_corazon),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Compartir con mi pareja", style = MaterialTheme.typography.titleMedium)
                        }
                        Text(
                            text = if (uiState.compartir) "Tu pareja podrá ver esto" else "Solo tú puedes verlo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(checked = uiState.compartir, onCheckedChange = { viewModel.toggleCompartir() })
                }
            }

            /**
             * Muestra mensajes si hay errores al intentar guardar los cambios
             */
            if (uiState.error != null) {
                Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

/**
 * Función que muestra los temas para clasificar el recuerdo en el modo edición
 */
@Composable
private fun SeccionEtiquetasEditar(
    etiquetaSeleccionada: String,
    etiquetaPersonalizada: String,
    onEtiquetaSeleccionada: (String) -> Unit,
    onEtiquetaPersonalizada: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("¿De qué se trata?", style = MaterialTheme.typography.titleMedium)
        TipoEvento.entries.chunked(3).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                fila.forEach { tipo ->
                    val seleccionada = etiquetaSeleccionada == tipo.name
                    Box(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50.dp)).background(if (seleccionada) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface).border(1.dp, if (seleccionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(50.dp)).clickable { onEtiquetaSeleccionada(tipo.name) }.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = tipo.icono,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (seleccionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(tipo.etiqueta, style = MaterialTheme.typography.bodySmall, color = if (seleccionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, maxLines = 1)
                        }
                    }
                }
                repeat(3 - fila.size) { Box(modifier = Modifier.weight(1f)) }
            }
        }
        if (etiquetaSeleccionada == TipoEvento.OTRO.name) {
            OutlinedTextField(value = etiquetaPersonalizada, onValueChange = onEtiquetaPersonalizada, label = { Text("¿Cuál?") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
    }
}

/**
 * Función que muestra el selector de estados de ánimo en el modo edición
 */
@Composable
private fun SeccionEmocionesEditar(
    emocionesSeleccionadas: List<Emocion>,
    onToggle: (Emocion) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("¿Cómo te sentiste?", style = MaterialTheme.typography.titleMedium)
        Emocion.entries.chunked(4).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                fila.forEach { emocion ->
                    val seleccionada = emocionesSeleccionadas.contains(emocion)
                    Box(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (seleccionada) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant).border(1.dp, if (seleccionada) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(12.dp)).clickable { onToggle(emocion) }.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = emocion.iconRes),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(emocion.etiqueta, style = MaterialTheme.typography.labelSmall, color = if (seleccionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                        }
                    }
                }
                repeat(4 - fila.size) { Box(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

/**
 * Función que permite gestionar tanto las fotos que ya estaban guardadas como las nuevas que se añadan
 */
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
    val hayMedia = fotosExistentes.isNotEmpty() || videosExistentes.isNotEmpty() || fotosNuevas.isNotEmpty() || videosNuevos.isNotEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Fotos y videos", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onAgregarMedia) {
                Icon(Icons.Outlined.PhotoCamera, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Agregar")
            }
        }
        if (!hayMedia) {
            Box(modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onAgregarMedia), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = "Toca para agregar fotos o videos", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        } else {
            data class ItemMedia(val uri: String, val esVideo: Boolean, val esExistente: Boolean)
            val todosLosItems = fotosExistentes.map { ItemMedia(it, false, true) } + videosExistentes.map { ItemMedia(it, true, true) } + fotosNuevas.map { ItemMedia(it, false, false) } + videosNuevos.map { ItemMedia(it, true, false) }
            val filas = todosLosItems.chunked(3)
            filas.forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    fila.forEach { item ->
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(8.dp))) {
                            AsyncImage(model = item.uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            /**
                             * Pequeño aviso para diferenciar lo que se acaba de añadir de lo que ya estaba guardado
                             */
                            if (!item.esExistente) {
                                Box(modifier = Modifier.align(Alignment.BottomStart).padding(4.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                    Text(text = "Nueva", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                }
                            }
                            if (item.esVideo) {
                                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.Videocam, null, tint = Color.White, modifier = Modifier.size(28.dp))
                                }
                            }
                            /**
                             * Botón para borrar el archivo del recuerdo
                             */
                            IconButton(
                                onClick = {
                                    when {
                                        item.esExistente && !item.esVideo -> onEliminarFotoExistente(item.uri)
                                        item.esExistente && item.esVideo -> onEliminarVideoExistente(item.uri)
                                        !item.esExistente && !item.esVideo -> onEliminarFotoNueva(item.uri)
                                        else -> onEliminarVideoNuevo(item.uri)
                                    }
                                },
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
