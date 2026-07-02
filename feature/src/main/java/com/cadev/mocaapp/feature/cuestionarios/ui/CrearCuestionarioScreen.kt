package com.cadev.mocaapp.feature.cuestionarios.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.cadev.mocaapp.core.ui.meshGradientBackground
import com.cadev.mocaapp.feature.cuestionarios.domain.model.*
import com.cadev.mocaapp.feature.cuestionarios.ui.components.*
import com.cadev.mocaapp.feature.ui.theme.*
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CrearCuestionarioScreen(
    viewModel: CuestionarioViewModel,
    usuarioId: String,
    parejaId: String,
    relacionId: String,
    onCreado: () -> Unit,
    onRegresar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var etiquetasSeleccionadas by remember { mutableStateOf(setOf<String>()) }
    var preguntas by remember { mutableStateOf(listOf<Pregunta>()) }
    var mostrarTipoPreguntaSheet by remember { mutableStateOf(false) }

    val etiquetasDisponibles = listOf("Divertido", "Hot", "Profundo", "Romántico", "Reto", "Picante")

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "scale"
    )

    LaunchedEffect(uiState.creadoExitoso) {
        if (uiState.creadoExitoso) {
            viewModel.resetearCreacion()
            onCreado()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(color = MocaSurface.copy(alpha = 0.95f), shadowElevation = 4.dp, modifier = Modifier.statusBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar", tint = MocaPrimary)
                    }
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, null, tint = MocaAccentPink, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Crear Reto",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MocaPrimary
                        )
                    }
                    TextButton(
                        onClick = {
                            if (titulo.isNotBlank() && preguntas.isNotEmpty()) {
                                viewModel.crearCuestionario(
                                    Cuestionario(
                                        titulo = titulo,
                                        descripcion = descripcion,
                                        categoria = CategoriaCuestionario.PERSONALIZADO.name,
                                        etiquetas = etiquetasSeleccionadas.toList(),
                                        preguntas = preguntas,
                                        creadoPor = usuarioId,
                                        relacionId = relacionId
                                    ),
                                    parejaId
                                )
                            }
                        },
                        enabled = titulo.isNotBlank() && preguntas.isNotEmpty() && !uiState.creando
                    ) {
                        if (uiState.creando) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Guardar", fontWeight = FontWeight.Black, color = MocaAccentPink)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().meshGradientBackground()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp, 24.dp, 16.dp, 120.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Favorite, null, tint = MocaPrimaryContainer, modifier = Modifier.size(56.dp).graphicsLayer(scaleX = pulseScale, scaleY = pulseScale))
                        Spacer(Modifier.height(12.dp))
                        Text("Estás en un lugar seguro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MocaPrimary)
                        Text("Preguntar y conocerse es la base de una relación sana y hermosa.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = MocaOnSurfaceVariant.copy(alpha = 0.8f), modifier = Modifier.padding(horizontal = 32.dp))
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MocaSurfaceContainerLowest),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MocaPrimaryContainer.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, null, tint = MocaAccentPink, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Información del Reto", fontWeight = FontWeight.Black, color = MocaPrimary)
                            }
                            
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("¿Cómo se llamará este test?", fontSize = 12.sp, color = MocaOnSurfaceVariant, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = titulo,
                                    onValueChange = { titulo = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MocaPrimary,
                                        unfocusedBorderColor = MocaOutline.copy(alpha = 0.2f),
                                        focusedContainerColor = MocaPrimaryContainer.copy(alpha = 0.05f)
                                    ),
                                    placeholder = { Text("Ej: Nuestra historia, Mis gustos...", modifier = Modifier.alpha(0.5f)) }
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Unas palabras para tu pareja", fontSize = 12.sp, color = MocaOnSurfaceVariant, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = descripcion,
                                    onValueChange = { descripcion = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MocaPrimary,
                                        unfocusedBorderColor = MocaOutline.copy(alpha = 0.2f),
                                        focusedContainerColor = MocaPrimaryContainer.copy(alpha = 0.05f)
                                    ),
                                    placeholder = { Text("¿De qué trata este test?", modifier = Modifier.alpha(0.5f)) }
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Etiquetas (elige las que apliquen)", fontSize = 12.sp, color = MocaOnSurfaceVariant, fontWeight = FontWeight.Bold)
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    etiquetasDisponibles.forEach { etiqueta ->
                                        TagChip(
                                            text = etiqueta,
                                            isSelected = etiqueta in etiquetasSeleccionadas,
                                            onClick = {
                                                etiquetasSeleccionadas = if (etiqueta in etiquetasSeleccionadas) {
                                                    etiquetasSeleccionadas - etiqueta
                                                } else {
                                                    etiquetasSeleccionadas + etiqueta
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (preguntas.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MocaPrimaryContainer.copy(alpha = 0.15f)),
                            border = androidx.compose.foundation.BorderStroke(2.dp, MocaPrimaryContainer.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Favorite, null, modifier = Modifier.size(90.dp).alpha(0.1f), tint = MocaPrimary)
                                    Icon(Icons.Default.AddCircle, null, modifier = Modifier.size(48.dp), tint = MocaPrimary)
                                }
                                Text("Tu reto está esperando", style = MaterialTheme.typography.titleMedium, color = MocaPrimary, fontWeight = FontWeight.Black)
                                Text("Cada pregunta es una oportunidad para conectar más profundamente.", style = MaterialTheme.typography.bodyMedium, color = MocaOnSurfaceVariant.copy(alpha = 0.8f), textAlign = TextAlign.Center)
                                Button(onClick = { mostrarTipoPreguntaSheet = true }, shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = MocaPrimary), elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp), modifier = Modifier.height(56.dp)) {
                                    Icon(Icons.Default.Favorite, null)
                                    Spacer(Modifier.width(12.dp))
                                    Text("Añadir mi primera pregunta", fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                } else {
                    itemsIndexed(preguntas) { index, pregunta ->
                        AnimatedVisibility(visible = true, enter = slideInVertically { it } + fadeIn()) {
                            EditorPreguntaMejorado(
                                numero = index + 1,
                                pregunta = pregunta,
                                subiendoFoto = uiState.subiendoFoto,
                                onCambiar = { nueva -> preguntas = preguntas.toMutableList().also { it[index] = nueva } },
                                onEliminar = { preguntas = preguntas.toMutableList().also { it.removeAt(index) } },
                                onSubirImagenPregunta = { rutaLocal ->
                                    viewModel.subirFotoPregunta(rutaLocal) { url ->
                                        preguntas = preguntas.toMutableList().also { it[index] = pregunta.copy(imagenUrl = url) }
                                    }
                                }
                            )
                        }
                    }

                    item {
                        Button(onClick = { mostrarTipoPreguntaSheet = true }, modifier = Modifier.fillMaxWidth().height(64.dp).shadow(8.dp, CircleShape, spotColor = MocaAccentPink.copy(alpha = 0.3f)), shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = MocaAccentPink)) {
                            Icon(Icons.Default.Favorite, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Añadir otra pregunta mágica", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }

    if (mostrarTipoPreguntaSheet) {
        AddQuestionTypeBottomSheet(
            onDismiss = { mostrarTipoPreguntaSheet = false },
            onTypeSelected = { tipo ->
                preguntas = preguntas + Pregunta(id = UUID.randomUUID().toString(), tipo = tipo.name, opciones = if (tipo == TipoPregunta.OPCION_MULTIPLE) listOf("", "") else emptyList())
                mostrarTipoPreguntaSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuestionTypeBottomSheet(onDismiss: () -> Unit, onTypeSelected: (TipoPregunta) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MocaSurface, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Añadir Pregunta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MocaOnSurface)
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
            }
            Box(modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(14.dp)).background(MocaSurfaceContainerLow).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, null, tint = MocaOnSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Buscar tipos de pregunta...", color = MocaOnSurfaceVariant.copy(alpha = 0.6f), fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
            val tipos = listOf(
                Triple(TipoPregunta.OPCION_MULTIPLE, "Opción Múltiple", Icons.Default.RadioButtonChecked),
                Triple(TipoPregunta.TEXTO_LIBRE, "Texto Libre", Icons.AutoMirrored.Filled.ShortText),
                Triple(TipoPregunta.TEXTO_LIBRE, "Párrafo", Icons.AutoMirrored.Filled.Notes),
                Triple(TipoPregunta.ESCALA, "Escala (1-10)", Icons.Default.LinearScale),
                Triple(TipoPregunta.SI_NO, "Sí/No", Icons.Default.ThumbsUpDown),
                Triple(TipoPregunta.OPCION_MULTIPLE, "Lista Desplegable", Icons.Default.ArrowDropDownCircle),
                Triple(TipoPregunta.OPCION_MULTIPLE, "Casillas", Icons.Default.CheckBox),
                Triple(TipoPregunta.TEXTO_LIBRE, "Fecha", Icons.Default.CalendarToday)
            )
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                items(tipos.size) { index ->
                    val item = tipos[index]
                    TypeItem(title = item.second, icon = item.third, color = when(index % 4) { 0 -> MocaPrimaryContainer 1 -> MocaSecondaryContainer 2 -> MocaTertiaryContainer else -> MocaPrimaryFixedDim }, onClick = { onTypeSelected(item.first) })
                }
            }
        }
    }
}

@Composable
fun TagChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val info = obtenerEtiquetaInfo(text)
    val bgColor by animateColorAsState(if (isSelected) info.colorFondo else MocaSurfaceContainerLow, label = "bg")
    val contentColor by animateColorAsState(if (isSelected) info.colorTexto else MocaOnSurfaceVariant, label = "content")

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = bgColor,
        modifier = Modifier.height(36.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, info.colorTexto.copy(alpha = 0.3f)) 
                 else androidx.compose.foundation.BorderStroke(1.dp, MocaOutline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = info.icono,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = contentColor
            )
            Text(text = text, color = contentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TypeItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MocaSurfaceContainerLowest).border(1.dp, MocaOutlineVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp)).clickable { onClick() }.padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(color.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MocaPrimary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = MocaOnSurface)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorPreguntaMejorado(numero: Int, pregunta: Pregunta, subiendoFoto: Boolean, onCambiar: (Pregunta) -> Unit, onEliminar: () -> Unit, onSubirImagenPregunta: (String) -> Unit) {
    val context = LocalContext.current
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var mostrarSheetImagen by remember { mutableStateOf(false) }
    val launcherCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success -> if (success && tempUri != null) { onSubirImagenPregunta(tempUri.toString()) } }
    val launcherGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { onSubirImagenPregunta(it.toString()) } }
    fun launchCamera() {
        val file = File(context.cacheDir, "temp_editor_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        tempUri = uri
        launcherCamara.launch(uri)
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MocaSurfaceContainerLowest), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = MocaPrimary.copy(alpha = 0.1f), shape = CircleShape) { Text("Pregunta $numero", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontWeight = FontWeight.Bold, color = MocaPrimary, fontSize = 12.sp) }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onEliminar) { Icon(Icons.Default.Close, null, tint = MocaError.copy(alpha = 0.6f)) }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Título de la pregunta", fontSize = 12.sp, color = MocaOnSurfaceVariant, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = pregunta.texto, onValueChange = { onCambiar(pregunta.copy(texto = it)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MocaPrimary, unfocusedBorderColor = MocaOutline.copy(alpha = 0.2f)), placeholder = { Text("¿Qué quieres preguntar?", fontSize = 14.sp) })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Obligatoria", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Switch(checked = pregunta.esObligatoria, onCheckedChange = { onCambiar(pregunta.copy(esObligatoria = it)) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MocaPrimary))
            }
            if (pregunta.imagenUrl.isNotBlank()) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(16.dp))) {
                    AsyncImage(pregunta.imagenUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    IconButton(onClick = { mostrarSheetImagen = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)) { Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                }
            } else {
                OutlinedButton(onClick = { mostrarSheetImagen = true }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MocaPrimary.copy(alpha = 0.2f)), colors = ButtonDefaults.outlinedButtonColors(contentColor = MocaPrimary)) {
                    if (subiendoFoto) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else { Icon(Icons.Default.AddPhotoAlternate, null); Spacer(Modifier.width(8.dp)); Text("Añadir imagen de apoyo", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                }
            }
            if (pregunta.tipo == TipoPregunta.OPCION_MULTIPLE.name) {
                Text("Opciones", fontWeight = FontWeight.Bold, color = MocaPrimary)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    pregunta.opciones.forEachIndexed { i, opcion ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.DragIndicator, null, tint = MocaOutline.copy(alpha = 0.5f))
                            OutlinedTextField(value = opcion, onValueChange = { nueva -> val newList = pregunta.opciones.toMutableList().also { it[i] = nueva }; onCambiar(pregunta.copy(opciones = newList)) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MocaPrimary), placeholder = { Text("Opción ${i + 1}", fontSize = 14.sp) })
                            IconButton(onClick = { val newList = pregunta.opciones.toMutableList().also { it.removeAt(i) }; onCambiar(pregunta.copy(opciones = newList)) }) { Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp)) }
                        }
                    }
                    TextButton(onClick = { onCambiar(pregunta.copy(opciones = pregunta.opciones + "")) }) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(4.dp)); Text("Añadir Opción", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }

    if (mostrarSheetImagen) {
        ModalBottomSheet(onDismissRequest = { mostrarSheetImagen = false }, containerColor = MocaSurface) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp, start = 24.dp, end = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Elegir imagen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                ListItem(headlineContent = { Text("Tomar Foto") }, leadingContent = { Icon(Icons.Default.PhotoCamera, null) }, modifier = Modifier.clickable { launchCamera(); mostrarSheetImagen = false })
                ListItem(headlineContent = { Text("Elegir de Galería") }, leadingContent = { Icon(Icons.Default.PhotoLibrary, null) }, modifier = Modifier.clickable { launcherGaleria.launch("image/*"); mostrarSheetImagen = false })
            }
        }
    }
}
