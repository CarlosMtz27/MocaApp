package com.cadev.mocaapp.feature.diario.ui

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.cadev.mocaapp.feature.diario.domain.model.Comentario
import com.cadev.mocaapp.feature.diario.domain.model.Emocion
import com.cadev.mocaapp.feature.diario.domain.model.EntradaDiario
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

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


    LaunchedEffect(entradaId) {
        viewModel.cargarDetalle(entradaId)
        viewModel.cargarNombreUsuario(usuarioId)
    }

    val entrada = uiState.entradaDetalle
    val esMia = entrada?.usuarioId == usuarioId
    val nombreUsuario = uiState.nombreUsuario

    // Visor de foto a pantalla completa
    var fotoVisor by remember { mutableStateOf<String?>(null) }

    // Visor de pantalla completa
    if (fotoVisor != null) {
        VisorPantallaCompleta(
            url = fotoVisor!!,
            onCerrar = { fotoVisor = null },
            onDescargar = { url ->
                descargarArchivo(context, url)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (entrada != null) {
                        val tipo = try {
                            TipoEntrada.valueOf(entrada.tipo)
                        } catch (e: Exception) { TipoEntrada.MI_DIA }
                        Text(
                            text = "${tipo.emoji} ${entrada.titulo}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                actions = {
                    // Solo el dueño puede editar
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
        }
    ) { padding ->

        if (uiState.cargando && entrada == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (entrada == null) return@Scaffold

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            //Encabezado
            item {
                EncabezadoEntrada(entrada = entrada)
            }

            //Emociones
            if (entrada.emociones.isNotEmpty()) {
                item {
                    SeccionEmocionesDetalle(emociones = entrada.emociones)
                }
            }

            //Descripción
            if (entrada.detalles.isNotBlank()) {
                item {
                    Text(
                        text = entrada.detalles,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            //Grid de fotos
            if (entrada.fotos.isNotEmpty() || entrada.videos.isNotEmpty()) {
                item {
                    SeccionMediaDetalle(
                        fotos = entrada.fotos,
                        videos = entrada.videos,
                        onFotoClick = { url -> fotoVisor = url },
                        onVideoClick = { url -> fotoVisor = url }
                    )
                }
            }

            // Divisor comentarios
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(
                        horizontal = 20.dp, vertical = 16.dp
                    )
                )
                Text(
                    text = "💬 Comentarios (${uiState.comentarios.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            //Lista de comentarios
            if (uiState.comentarios.isEmpty()) {
                item {
                    Text(
                        text = "Sé el primero en comentar 💕",
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

            //Input nuevo comentario
            item {
                InputComentario(
                    texto = uiState.nuevoComentario,
                    onTextoChange = { viewModel.actualizarNuevoComentario(it) },
                    onEnviar = { viewModel.publicarComentario(usuarioId, nombreUsuario) }
                )
            }
        }
    }
}

//Encabezado de la entrada
@Composable
private fun EncabezadoEntrada(entrada: EntradaDiario) {
    val tipo = try {
        TipoEntrada.valueOf(entrada.tipo)
    } catch (e: Exception) { TipoEntrada.MI_DIA }

    val colorTipo = Color(
        android.graphics.Color.parseColor("#${tipo.colorHex}")
    )

    val formatoFecha = SimpleDateFormat(
        "EEEE d 'de' MMMM, yyyy", Locale("es", "MX")
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
        // Tipo + etiqueta
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
                Text(text = tipo.emoji, fontSize = 18.sp)
            }
            Column {
                Text(
                    text = tipo.etiqueta + if (entrada.etiqueta.isNotBlank())
                        " · ${entrada.etiqueta}" else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = colorTipo
                )
                Text(
                    text = fechaVisible,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Título grande
        Text(
            text = entrada.titulo,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Badge compartida
        if (entrada.compartida) {
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("💕", fontSize = 12.sp)
                Text(
                    text = "Compartida con tu pareja",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

//Emociones en detalle
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = emocion.emoji, fontSize = 24.sp)
                    Text(
                        text = emocion.etiqueta,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                            .copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

//Grid de fotos y videos

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
        modifier = Modifier.padding(
            horizontal = 20.dp, vertical = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "📷 Fotos y videos (${todosLosItems.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        todosLosItems.chunked(3).forEach { fila ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                fila.forEach { (uri, esVideo) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                if (esVideo) onVideoClick(uri)
                                else onFotoClick(uri)
                            }
                    ) {
                        AsyncImage(
                            model = uri,
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
                                Icon(
                                    Icons.Outlined.Videocam,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
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

//Visor pantalla completa
@Composable
private fun VisorPantallaCompleta(
    url: String,
    onCerrar: () -> Unit,
    onDescargar: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Imagen a pantalla completa
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            // Botón cerrar
            IconButton(
                onClick = onCerrar,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Cerrar",
                    tint = Color.White
                )
            }

            // Botón descargar
            IconButton(
                onClick = { onDescargar(url) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Filled.Download,
                    contentDescription = "Descargar",
                    tint = Color.White
                )
            }
        }
    }
}

//Función de descarga

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

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE)
                as DownloadManager
        dm.enqueue(request)
    } catch (e: Exception) {
        // Si falla la descarga silenciosamente
    }
}

// Tarjeta de comentario

@Composable
private fun TarjetaComentario(
    comentario: Comentario,
    esMio: Boolean,
    onEliminar: () -> Unit
) {
    val formatoHora = SimpleDateFormat("d MMM · HH:mm", Locale("es", "MX"))

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
            // Nombre del autor encima del bubble
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
                    topStart = 16.dp,
                    topEnd = 16.dp,
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

            // Hora mas eliminar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
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
                    text = formatoHora.format(comentario.creadoEn),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                        .copy(alpha = 0.4f)
                )
            }
        }
    }
}

//Input de nuevo comentario

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
                    else MaterialTheme.colorScheme.onSurface
                        .copy(alpha = 0.3f)
                )
            }
        }
    }
}