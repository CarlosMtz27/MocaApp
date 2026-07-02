package com.cadev.mocaapp.feature.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.cadev.mocaapp.core.model.TipoEvento
import com.cadev.mocaapp.feature.diario.domain.model.Emocion
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import com.cadev.mocaapp.feature.diario.ui.DiarioViewModel
import com.cadev.mocaapp.feature.ui.components.HeartToggle
import com.cadev.mocaapp.feature.ui.theme.*
import java.io.File
import java.util.*

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

/**
 * PANTALLA DE CREACIÓN DE ENTRADA (SECCIÓN 4.3)
 * Adaptada para "Día" o "Recuerdo" con textos en español, toggle de corazón y soporte para Modo Oscuro.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEntryScreen(
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
    val scrollState = rememberScrollState()
    val isDark = esModoOscuro()
    
    // Media selection logic
    var uriCameraTemp by remember { mutableStateOf<Uri?>(null) }
    var uriVideoTemp by remember { mutableStateOf<Uri?>(null) }
    var mostrarDialogoMedia by remember { mutableStateOf(false) }

    fun crearUriTemporal(carpeta: String, extension: String): Uri {
        val dir = File(context.cacheDir, carpeta).also { it.mkdirs() }
        val archivo = File(dir, "${UUID.randomUUID()}.$extension")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }

    val launcherGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris.forEach { uri -> viewModel.agregarFoto(uri.toString()) }
    }
    val launcherCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (exito) uriCameraTemp?.let { viewModel.agregarFoto(it.toString()) }
    }
    val launcherVideo = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { exito ->
        if (exito) uriVideoTemp?.let { viewModel.agregarVideo(it.toString()) }
    }

    LaunchedEffect(Unit) {
        viewModel.limpiarFormulario()
        if (tipo == TipoEntrada.RECUERDO.name && parejaId != null) {
            viewModel.toggleCompartir()
        }
    }

    LaunchedEffect(uiState.entradaCreada) {
        if (uiState.entradaCreada) onEntradaGuardada()
    }

    if (mostrarDialogoMedia) {
        MediaSourceDialog(
            onDismiss = { mostrarDialogoMedia = false },
            onGallery = { launcherGaleria.launch("image/*") },
            onCamera = {
                val uri = crearUriTemporal("camera", "jpg")
                uriCameraTemp = uri
                launcherCamara.launch(uri)
            },
            onVideo = {
                val uri = crearUriTemporal("video", "mp4")
                uriVideoTemp = uri
                launcherVideo.launch(uri)
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // BARRA SUPERIOR
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        // Sombra sutil solo en la parte inferior
                        val shadowHeight = 3.dp.toPx()
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = if (isDark) 0.2f else 0.08f), Color.Transparent),
                                startY = size.height,
                                endY = size.height + shadowHeight
                            )
                        )
                    },
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onRegresar, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(
                        text = if (tipo == TipoEntrada.RECUERDO.name) "Nuevo Recuerdo" else "Mi Día",
                        style = OrganicTypography.headlineMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(
                        onClick = { viewModel.guardarEntrada(usuarioId, parejaId, fecha, tipo) },
                        enabled = !uiState.cargando
                    ) {
                        if (uiState.cargando) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = "Guardar",
                                style = OrganicTypography.labelMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                // CATEGORÍA (Solo para Recuerdos)
                if (tipo == TipoEntrada.RECUERDO.name) {
                    Text(
                        text = "¿De qué se trata?",
                        style = OrganicTypography.headlineMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(TipoEvento.entries) { evento ->
                            val selected = uiState.etiqueta == evento.name
                            CategoryChip(
                                label = evento.etiqueta,
                                icon = evento.icono,
                                selected = selected,
                                onClick = { viewModel.actualizarEtiqueta(evento.name) }
                            )
                        }
                    }
                }

                // TÍTULO DINÁMICO
                Text(
                    text = if (tipo == TipoEntrada.RECUERDO.name) "¿Qué quieres recordar?" else "¿Cómo fue tu día?",
                    style = OrganicTypography.headlineMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
                
                TextField(
                    value = uiState.titulo,
                    onValueChange = { viewModel.actualizarTitulo(it) },
                    placeholder = { Text(if (tipo == TipoEntrada.RECUERDO.name) "Momento especial..." else "Resumen del día...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp)
                        .shadow(3.dp, RoundedCornerShape(12.dp)) 
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // EMOCIONES
                Text(
                    text = "¿Cómo te sentiste?",
                    style = OrganicTypography.headlineMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(Emocion.entries) { emocion ->
                        val selected = uiState.emocionesSeleccionadas.contains(emocion)
                        EmojiItem(
                            emoji = getEmojiForEmocion(emocion),
                            label = emocion.etiqueta,
                            selected = selected,
                            onClick = { viewModel.toggleEmocion(emocion) }
                        )
                    }
                }

                // CUÉNTAME MÁS
                Text(
                    text = "Cuéntame más...",
                    style = OrganicTypography.headlineMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
                
                TextField(
                    value = uiState.detalles,
                    onValueChange = { viewModel.actualizarDetalles(it) },
                    placeholder = { Text("Escribe tus pensamientos aquí...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .heightIn(min = 160.dp)
                        .shadow(3.dp, RoundedCornerShape(12.dp)) 
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                // FOTOS Y VIDEOS
                Text(
                    text = "Fotos y Videos",
                    style = OrganicTypography.headlineMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item { AddMediaButton(onClick = { mostrarDialogoMedia = true }) }
                    
                    items(uiState.fotosSeleccionadas) { uri ->
                        MediaItem(uri = uri, onRemove = { viewModel.eliminarFoto(uri) })
                    }
                    items(uiState.videosSeleccionados) { uri ->
                        MediaItem(uri = uri, esVideo = true, onRemove = { viewModel.eliminarVideo(uri) })
                    }
                }

                Spacer(modifier = Modifier.height(24.dp)) 

                // COMPARTIR CON MI PAREJA
                if (parejaId != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .shadow(4.dp, RoundedCornerShape(24.dp)) 
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else Color(0xFFFFEEF3)) 
                            .border(1.dp, if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f) else Color(0xFFF5DCE6), RoundedCornerShape(24.dp))
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "¿Quieres compartirlo con tu pareja?",
                                    style = OrganicTypography.headlineMedium.copy(
                                        fontSize = 16.sp, 
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 22.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (uiState.compartir) "Tu pareja podrá ver este momento" else "Solo tú podrás ver este momento",
                                    style = OrganicTypography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            HeartToggle(
                                checked = uiState.compartir,
                                onCheckedChange = { viewModel.toggleCompartir() }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                modifier = Modifier.size(18.dp), 
                tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = OrganicTypography.labelMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmojiItem(
    emoji: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (selected) Color(0xFFE1F5FE) else Color.Transparent) 
                .border(
                    width = 2.dp,
                    color = if (selected) Color(0xFF03A9F4) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 24.sp)
        }
        Text(
            text = label,
            style = OrganicTypography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.alpha(if (selected) 1f else 0.7f)
        )
    }
}

@Composable
fun AddMediaButton(onClick: () -> Unit) {
    val stroke = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
    val isDark = esModoOscuro()
    Box(
        modifier = Modifier
            .size(96.dp)
            .shadow(3.dp, RoundedCornerShape(12.dp)) 
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp)) 
            .drawBehind {
                drawRoundRect(color = if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFA1455A).copy(alpha = 0.4f), style = stroke)
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                text = "Añadir", 
                style = OrganicTypography.labelSmall, 
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun MediaItem(uri: String, esVideo: Boolean = false, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)))
        if (esVideo) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.align(Alignment.Center).size(32.dp)
            )
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun MediaSourceDialog(
    onDismiss: () -> Unit,
    onGallery: () -> Unit,
    onCamera: () -> Unit,
    onVideo: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir multimedia") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ListItem(
                    headlineContent = { Text("Galería") },
                    leadingContent = { Icon(Icons.Outlined.PhotoLibrary, null) },
                    modifier = Modifier.clickable { onDismiss(); onGallery() }
                )
                ListItem(
                    headlineContent = { Text("Tomar Foto") },
                    leadingContent = { Icon(Icons.Outlined.PhotoCamera, null) },
                    modifier = Modifier.clickable { onDismiss(); onCamera() }
                )
                ListItem(
                    headlineContent = { Text("Grabar Video") },
                    leadingContent = { Icon(Icons.Outlined.Videocam, null) },
                    modifier = Modifier.clickable { onDismiss(); onVideo() }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

fun getEmojiForEmocion(emocion: Emocion): String = when(emocion) {
    Emocion.FELIZ -> "😊"
    Emocion.AMADO -> "🥰"
    Emocion.EMOCIONADO -> "🤩"
    Emocion.TRANQUILO -> "😌"
    Emocion.NOSTALGICO -> "🥺"
    Emocion.TRISTE -> "😢"
    Emocion.AGRADECIDO -> "🙏"
    Emocion.AVENTURERO -> "🌍"
}
