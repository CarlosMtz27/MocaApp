package com.cadev.mocaapp.feature.diario.ui

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.cadev.mocaapp.feature.diario.domain.model.Comentario
import com.cadev.mocaapp.feature.diario.domain.model.Emocion
import com.cadev.mocaapp.feature.diario.domain.model.EntradaDiario
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import com.cadev.mocaapp.core.model.TipoEvento
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * ESTA ES LA PANTALLA DE DETALLE DE UN RECUERDO
 * 
 * Qué hace:
 * Aquí mostramos toda la información de un momento guardado: la historia completa, 
 * el mosaico de fotos y vídeos, y la sección de comentarios de nuestra pareja. 
 * También permitimos ver fotos a pantalla completa y descargarlas.
 * 
 * Cómo lo podemos modificar:
 * Si queremos añadir un botón para "Compartir en redes sociales", debemos añadirlo 
 * en las `actions` de la barra superior (TopAppBar).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleEntradaScreen(
    viewModel: DiarioViewModel,
    entradaId: String,
    usuarioId: String,
    onRegresar: () -> Unit,
    onEditar: (entradaId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    /**
     * Se cargan todos los datos del recuerdo y el nombre del usuario al entrar
     */
    LaunchedEffect(entradaId) {
        viewModel.cargarDetalle(entradaId)
        viewModel.cargarNombreUsuario(usuarioId)
    }

    val entrada = uiState.entradaDetalle
    val esMia = entrada?.usuarioId == usuarioId
    val nombreUsuario = uiState.nombreUsuario

    /**
     * Variable para controlar qué foto o vídeo se está viendo en pantalla completa
     */
    var mediaVisor by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    if (mediaVisor != null) {
        val (url, esVideo) = mediaVisor!!
        if (esVideo) {
            VisorVideo(
                url = url,
                onCerrar = { mediaVisor = null },
                onDescargar = { descargarArchivo(context, url) }
            )
        } else {
            VisorFoto(
                url = url,
                onCerrar = { mediaVisor = null },
                onDescargar = { descargarArchivo(context, url) }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (entrada != null) {
                        val tipo = try {
                            TipoEntrada.valueOf(entrada.tipo)
                        } catch (e: Exception) { TipoEntrada.MI_DIA }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = tipo.icono,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = entrada.titulo,
                                style = MaterialTheme.typography.titleMedium
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
                     * Botón para ir a la pantalla de edición solo si el recuerdo es tuyo
                     */
                    if (esMia && entrada != null) {
                        IconButton(onClick = { onEditar(entrada.id) }) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            /**
             * Cuadro de texto fijo al final para escribir un comentario nuevo
             */
            if (entrada != null) {
                val parejaId = remember(entrada, usuarioId) {
                    if (entrada.usuarioId == usuarioId) entrada.parejaId else entrada.usuarioId
                }
                InputComentario(
                    texto = uiState.nuevoComentario,
                    onTextoChange = { viewModel.actualizarNuevoComentario(it) },
                    onEnviar = {
                        viewModel.publicarComentario(usuarioId, nombreUsuario, parejaId)
                    }
                )
            }
        }
    ) { padding ->

        if (uiState.cargando && entrada == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        if (entrada == null) return@Scaffold

        /**
         * Contenedor que permite deslizar para leer toda la información del recuerdo
         */
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {

            /**
             * Muestra el tipo de recuerdo la categoría y la fecha
             */
            item { EncabezadoEntrada(entrada = entrada) }

            /**
             * Muestra la lista de sentimientos asociados a este momento
             */
            if (entrada.emociones.isNotEmpty()) {
                item {
                    SeccionEmocionesDetalle(emociones = entrada.emociones)
                }
            }

            /**
             * Muestra el texto largo de la historia
             */
            if (entrada.detalles.isNotBlank()) {
                item {
                    Text(
                        text = entrada.detalles,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(
                            horizontal = 20.dp, vertical = 8.dp
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            /**
             * Muestra una cuadrícula con todas las fotos y vídeos guardados
             */
            if (entrada.fotos.isNotEmpty() || entrada.videos.isNotEmpty()) {
                item {
                    SeccionMediaDetalle(
                        fotos = entrada.fotos,
                        videos = entrada.videos,
                        onFotoClick = { url ->
                            mediaVisor = Pair(url, false)
                        },
                        onVideoClick = { url ->
                            mediaVisor = Pair(url, true)
                        }
                    )
                }
            }

            /**
             * Título de la sección de comentarios
             */
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(
                        horizontal = 20.dp, vertical = 16.dp
                    )
                )
                Text(
                    text = "Comentarios (${uiState.comentarios.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            /**
             * Lista de todos los comentarios dejados por la pareja
             */
            if (uiState.comentarios.isEmpty()) {
                item {
                    Text(
                        text = "Sé el primero en comentar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                }
            } else {
                items(uiState.comentarios) { comentario ->
                    TarjetaComentario(
                        comentario = comentario,
                        esMio = comentario.usuarioId == usuarioId,
                        onEliminar = {
                            viewModel.eliminarComentario(comentario.id)
                        }
                    )
                }
            }

            /**
             * Muestra un mensaje de error si no se pudo publicar el comentario
             */
            if (uiState.error != null) {
                item {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Función que dibuja la parte superior del detalle con iconos y colores representativos
 */
@Composable
private fun EncabezadoEntrada(entrada: EntradaDiario) {
    val tipo = try {
        TipoEntrada.valueOf(entrada.tipo)
    } catch (e: Exception) { TipoEntrada.MI_DIA }

    val colorTipo = Color(
        android.graphics.Color.parseColor("#${tipo.colorHex}")
    )

    val formatoFecha = SimpleDateFormat(
        "EEEE d 'de' MMMM, yyyy", Locale.forLanguageTag("es-MX")
    )
    val fechaVisible = try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        formatoFecha.format(sdf.parse(entrada.fecha)!!)
            .replaceFirstChar { it.uppercase() }
    } catch (e: Exception) { entrada.fecha }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colorTipo.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tipo.icono,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = colorTipo
                )
            }
            Column {
                val etiquetaMostrada = if (entrada.etiqueta.isNotBlank()) {
                    val tipoEvento = TipoEvento.entries.find { it.name == entrada.etiqueta }
                    if (tipoEvento != null) {
                        tipoEvento.etiqueta
                    } else {
                        entrada.etiqueta
                    }
                } else ""

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tipo.etiqueta,
                        style = MaterialTheme.typography.labelMedium,
                        color = colorTipo
                    )
                    if (etiquetaMostrada.isNotBlank()) {
                        val tipoEvento = TipoEvento.entries.find { it.name == entrada.etiqueta }
                        Text(text = " · ", color = colorTipo)
                        if (tipoEvento != null) {
                            Icon(
                                imageVector = tipoEvento.icono,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = colorTipo
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(
                            text = etiquetaMostrada,
                            style = MaterialTheme.typography.labelMedium,
                            color = colorTipo
                        )
                    }
                }
                Text(
                    text = fechaVisible,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = entrada.titulo,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        if (entrada.compartida) {
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                Text(
                    text = "Compartida con tu pareja",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Muestra los dibujos de emociones seleccionados para este recuerdo
 */
@Composable
private fun SeccionEmocionesDetalle(emociones: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        emociones.forEach { nombre ->
            val emocion = try {
                Emocion.valueOf(nombre)
            } catch (e: Exception) { null }
            if (emocion != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = emocion.iconRes),
                        contentDescription = emocion.etiqueta,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = emocion.etiqueta,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

/**
 * Organiza las fotos y vídeos en un mosaico interactivo
 */
@Composable
private fun SeccionMediaDetalle(
    fotos: List<String>,
    videos: List<String>,
    onFotoClick: (String) -> Unit,
    onVideoClick: (String) -> Unit
) {
    val todosLosItems = fotos.map { Pair(it, false) } +
            videos.map { Pair(it, true) }

    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Fotos y videos (${todosLosItems.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))

        todosLosItems.chunked(3).forEach { fila ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                fila.forEach { (url, esVideo) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember {
                                    MutableInteractionSource()
                                },
                                indication = ripple()
                            ) {
                                if (esVideo) onVideoClick(url)
                                else onFotoClick(url)
                            }
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (esVideo) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                repeat(3 - fila.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Muestra una imagen ocupando toda la pantalla y permite descargarla
 */
@Composable
private fun VisorFoto(
    url: String,
    onCerrar: () -> Unit,
    onDescargar: () -> Unit
) {
    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = onCerrar,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Filled.Close, "Cerrar", tint = Color.White)
            }
            IconButton(
                onClick = onDescargar,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Filled.Download, "Descargar", tint = Color.White)
            }
        }
    }
}

/**
 * Reproductor de vídeo a pantalla completa con controles de pausa y volumen
 */
@Composable
private fun VisorVideo(
    url: String,
    onCerrar: () -> Unit,
    onDescargar: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val exoPlayer = remember {
                androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
                    setMediaItem(androidx.media3.common.MediaItem.fromUri(url))
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

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onCerrar,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Filled.Close, "Cerrar", tint = Color.White)
                }
            }

            IconButton(
                onClick = onDescargar,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Filled.Download, "Descargar", tint = Color.White)
            }
        }
    }
}

/**
 * Inicia la descarga de un archivo multimedia a la carpeta de imágenes del teléfono
 */
private fun descargarArchivo(context: Context, url: String) {
    try {
        val nombreArchivo = url.substringAfterLast("/")
            .substringBefore("?")
            .ifBlank { "moca_${System.currentTimeMillis()}" }

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Descargando archivo")
            .setDescription("MocaApp")
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_PICTURES,
                "MocaApp/$nombreArchivo"
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
            .enqueue(request)
    } catch (e: Exception) { }
}

/**
 * Función que dibuja la burbuja de cada comentario individual
 */
@Composable
private fun TarjetaComentario(
    comentario: Comentario,
    esMio: Boolean,
    onEliminar: () -> Unit
) {
    val formatoHora = SimpleDateFormat("d MMM · HH:mm", Locale.forLanguageTag("es-MX"))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = if (esMio)
            Arrangement.Start else Arrangement.End
    ) {
        Column(
            horizontalAlignment = if (esMio)
                Alignment.Start else Alignment.End,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = if (esMio) "Tú" else comentario.nombreUsuario,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (esMio)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(
                    start = if (esMio) 4.dp else 0.dp,
                    end = if (esMio) 0.dp else 4.dp,
                    bottom = 2.dp
                )
            )

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp,
                    bottomStart = if (esMio) 4.dp else 16.dp,
                    bottomEnd = if (esMio) 16.dp else 4.dp
                ),
                color = if (esMio)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 1.dp
            ) {
                Text(
                    text = comentario.texto,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (esMio)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        horizontal = 12.dp, vertical = 8.dp
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                /**
                 * Permite borrar el comentario si tú eres el autor
                 */
                if (esMio) {
                    IconButton(
                        onClick = onEliminar,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.onSurface
                                .copy(alpha = 0.3f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = formatoHora.format(comentario.creadoEn.toDate()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

/**
 * Función que crea el campo de texto inferior para enviar nuevos comentarios
 */
@Composable
private fun InputComentario(
    texto: String,
    onTextoChange: (String) -> Unit,
    onEnviar: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = texto,
                onValueChange = onTextoChange,
                placeholder = { Text("Escribe un comentario...") },
                modifier = Modifier.weight(1f),
                maxLines = 3,
                shape = RoundedCornerShape(24.dp)
            )
            IconButton(
                onClick = onEnviar,
                enabled = texto.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (texto.isNotBlank())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    tint = if (texto.isNotBlank()) Color.White
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}
