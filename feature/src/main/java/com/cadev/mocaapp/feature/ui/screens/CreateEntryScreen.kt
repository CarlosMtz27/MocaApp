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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.cadev.mocaapp.core.model.TipoEvento
import com.cadev.mocaapp.feature.diario.domain.model.Emocion
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import com.cadev.mocaapp.feature.diario.ui.DiarioViewModel
import com.cadev.mocaapp.feature.ui.theme.*
import java.io.File
import java.util.*

/**
 * PANTALLA DE CREACIÓN DE ENTRADA (SECCIÓN 4.3)
 * Fiel al diseño "New Entry" del HTML.
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
    
    // Logic for media selection (kept from original)
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
        color = Color(0xFFFCF8F9)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onRegresar, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF1D0C10))
                }
                Text(
                    text = "New Entry",
                    style = OrganicTypography.headlineMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = Color(0xFF1D0C10)
                )
                TextButton(
                    onClick = { viewModel.guardarEntrada(usuarioId, parejaId, fecha, tipo) },
                    enabled = !uiState.cargando
                ) {
                    if (uiState.cargando) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            "Save",
                            style = OrganicTypography.labelMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            color = Color(0xFFA1455A)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                // WHAT IT IS ABOUT
                Text(
                    text = "What it is about",
                    style = OrganicTypography.headlineMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = Color(0xFF1D0C10),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TipoEvento.entries.forEach { evento ->
                        val selected = uiState.etiqueta == evento.name
                        CategoryChip(
                            label = evento.etiqueta,
                            icon = evento.icono,
                            selected = selected,
                            onClick = { viewModel.actualizarEtiqueta(evento.name) }
                        )
                    }
                }

                // WHAT DO YOU WANT TO REMEMBER
                Text(
                    text = "What do you want to remember",
                    style = OrganicTypography.headlineMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = Color(0xFF1D0C10),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
                
                TextField(
                    value = uiState.titulo,
                    onValueChange = { viewModel.actualizarTitulo(it) },
                    placeholder = { Text("Main highlight...", color = Color(0xFFA1455A).copy(alpha = 0.7f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFEACDD4), RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFCF8F9),
                        unfocusedContainerColor = Color(0xFFFCF8F9),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFFA1455A)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // HOW DID YOU FEEL
                Text(
                    text = "How did you feel",
                    style = OrganicTypography.headlineMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = Color(0xFF1D0C10),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Emocion.entries.forEach { emocion ->
                        val selected = uiState.emocionesSeleccionadas.contains(emocion)
                        EmojiItem(
                            emoji = getEmojiForEmocion(emocion),
                            label = emocion.etiqueta,
                            selected = selected,
                            onClick = { viewModel.toggleEmocion(emocion) }
                        )
                    }
                }

                // TELL ME MORE
                Text(
                    text = "Tell me more",
                    style = OrganicTypography.headlineMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = Color(0xFF1D0C10),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
                
                TextField(
                    value = uiState.detalles,
                    onValueChange = { viewModel.actualizarDetalles(it) },
                    placeholder = { Text("Write your thoughts here...", color = MocaOutline.copy(alpha = 0.7f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .heightIn(min = 160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MocaOutlineVariant, RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                // PHOTOS & VIDEOS
                Text(
                    text = "Photos & Videos",
                    style = OrganicTypography.headlineMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = Color(0xFF1D0C10),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AddMediaButton(onClick = { mostrarDialogoMedia = true })
                    
                    uiState.fotosSeleccionadas.forEach { uri ->
                        MediaItem(uri = uri, onRemove = { viewModel.eliminarFoto(uri) })
                    }
                    uiState.videosSeleccionados.forEach { uri ->
                        MediaItem(uri = uri, esVideo = true, onRemove = { viewModel.eliminarVideo(uri) })
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // SHARE WITH MY PARTNER
                if (parejaId != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                            .background(Color.White)
                            .border(1.dp, MocaSurfaceVariant, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Share with my partner",
                                    style = OrganicTypography.headlineMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    color = MocaOnSurface
                                )
                                Text(
                                    text = "They will be notified of this entry.",
                                    style = OrganicTypography.bodyMedium,
                                    color = MocaOnSurfaceVariant
                                )
                            }
                            Switch(
                                checked = uiState.compartir,
                                onCheckedChange = { viewModel.toggleCompartir() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MocaPrimary
                                )
                            )
                        }
                    }
                }
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
        color = if (selected) Color(0xFFF4E6E9) else Color(0xFFF4E6E9).copy(alpha = 0.3f),
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF1D0C10))
            Text(
                text = label,
                style = OrganicTypography.labelMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                color = Color(0xFF1D0C10)
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
            .alpha(if (selected) 1f else 0.7f)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (selected) MocaPrimaryContainer else MocaSurfaceContainer)
                .border(2.dp, if (selected) MocaPrimary else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 24.sp)
        }
        Text(
            text = label,
            style = OrganicTypography.labelMedium,
            color = if (selected) MocaOnSurface else MocaOnSurfaceVariant
        )
    }
}

@Composable
fun AddMediaButton(onClick: () -> Unit) {
    val stroke = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(12.dp))
            .drawBehind {
                drawRoundRect(color = MocaOutlineVariant, style = stroke)
            }
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MocaOnSurfaceVariant)
            Text("Add Media", style = OrganicTypography.labelSmall, color = MocaOnSurfaceVariant)
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
                Icons.Default.PlayArrow,
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
            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
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
        title = { Text("Add Media") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ListItem(
                    headlineContent = { Text("Gallery") },
                    leadingContent = { Icon(Icons.Outlined.PhotoLibrary, null) },
                    modifier = Modifier.clickable { onDismiss(); onGallery() }
                )
                ListItem(
                    headlineContent = { Text("Take Photo") },
                    leadingContent = { Icon(Icons.Outlined.PhotoCamera, null) },
                    modifier = Modifier.clickable { onDismiss(); onCamera() }
                )
                ListItem(
                    headlineContent = { Text("Record Video") },
                    leadingContent = { Icon(Icons.Outlined.Videocam, null) },
                    modifier = Modifier.clickable { onDismiss(); onVideo() }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
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
