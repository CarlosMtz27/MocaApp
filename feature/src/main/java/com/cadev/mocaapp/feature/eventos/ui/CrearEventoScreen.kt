package com.cadev.mocaapp.feature.eventos.ui

import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cadev.mocaapp.core.model.TipoEvento
import com.cadev.mocaapp.core.ui.*
import com.cadev.mocaapp.feature.eventos.domain.model.RecordatorioOpcion
import com.cadev.mocaapp.feature.eventos.ui.components.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearEventoScreen(
    viewModel: EventoViewModel,
    usuarioId: String,
    parejaId: String,
    relacionId: String,
    onGuardado: () -> Unit,
    onRegresar: () -> Unit
) {
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var mostrarConfeti by remember { mutableStateOf(false) }

    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else StitchPrimary
    val colorBackground = if (isDark) Color(0xFF1E1B14) else StitchBackground
    val colorOnSurface = if (isDark) Color(0xFFE7BBC6) else StitchOnSurface
    val colorOnSurfaceVariant = if (isDark) Color(0xFFD3C3C5) else StitchOnSurfaceVariant

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.subirImagen(it.toString()) }
    }

    LaunchedEffect(uiState.guardado) {
        if (uiState.guardado) {
            mostrarConfeti = true
            delay(1500)
            viewModel.limpiarFormulario()
            onGuardado()
        }
    }

    val datePickerState = rememberDatePickerState()
    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarTimePicker by remember { mutableStateOf(false) }
    var mostrarReminderPicker by remember { mutableStateOf(false) }

    LaunchedEffect(datePickerState.selectedDateMillis) {
        val millis = datePickerState.selectedDateMillis ?: return@LaunchedEffect
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        val fecha = "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        viewModel.actualizarFecha(fecha)
    }

    if (mostrarDatePicker) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (mostrarTimePicker) {
        val hora = uiState.hora.split(":").getOrNull(0)?.toIntOrNull() ?: 12
        val min = uiState.hora.split(":").getOrNull(1)?.toIntOrNull() ?: 0
        TimePickerDialog(context, { _, h, m ->
            viewModel.actualizarHora("%02d:%02d".format(h, m))
            mostrarTimePicker = false
        }, hora, min, true).show()
        mostrarTimePicker = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorBackground)
            .then(if (!isDark) Modifier.meshGradientBackground() else Modifier)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) colorBackground.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.5f))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onRegresar,
                    modifier = Modifier.background(if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF4EDE1), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = colorPrimary)
                }
                Text(
                    text = "Nuevo Evento",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                    color = colorPrimary
                )
                TextButton(
                    onClick = { viewModel.guardarEvento(context, usuarioId, parejaId, relacionId) },
                    enabled = uiState.titulo.isNotBlank() && uiState.fecha.isNotBlank() && !uiState.cargando
                ) {
                    Text(
                        "Guardar", 
                        fontWeight = FontWeight.Bold, 
                        color = colorPrimary,
                        fontSize = 16.sp
                    )
                }
            }

            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { visible = true }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Tipo de Evento
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(600, 100)) + slideInVertically(tween(600, 100)) { 20 }
                ) {
                    SectionCard(title = "Tipo de Evento") {
                        val tiposGrid = listOf(
                            TipoEvento.CITA to "Cita",
                            TipoEvento.ANIVERSARIO to "Aniversario",
                            TipoEvento.CUMPLEANOS to "Cumple",
                            TipoEvento.VIAJE to "Viaje",
                            TipoEvento.ESPECIAL to "Celebrar",
                            TipoEvento.CENA to "Cena"
                        )
                        
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            for (i in 0 until 2) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    for (j in 0 until 3) {
                                        val index = i * 3 + j
                                        if (index < tiposGrid.size) {
                                            val (tipo, label) = tiposGrid[index]
                                            EventTypeButton(
                                                tipo = tipo,
                                                label = label,
                                                isSelected = uiState.tipo == tipo.name,
                                                isDark = isDark,
                                                onClick = { viewModel.actualizarTipo(tipo.name) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                EventTypeButton(
                                    tipo = TipoEvento.OTRO,
                                    label = "Otro",
                                    isSelected = uiState.tipo == TipoEvento.OTRO.name,
                                    isDark = isDark,
                                    onClick = { viewModel.actualizarTipo(TipoEvento.OTRO.name) },
                                    modifier = Modifier.fillMaxWidth(0.33f)
                                )
                            }
                        }
                    }
                }

                // 2. Información
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(600, 200)) + slideInVertically(tween(600, 200)) { 20 }
                ) {
                    SectionCard(title = "Información") {
                        MinimalTextField(
                            value = uiState.titulo,
                            onValueChange = viewModel::actualizarTitulo,
                            placeholder = "Título del evento *",
                            isTitle = true
                        )
                        MinimalTextField(
                            value = uiState.lugar,
                            onValueChange = viewModel::actualizarLugar,
                            placeholder = "Lugar (ej: Restaurante, Parque...)"
                        )
                        
                        // Selector de Imagen Premium
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF4EDE1))
                                .clickable { pickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.fotoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = uiState.fotoUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PhotoCamera, null, tint = Color.White, modifier = Modifier.size(32.dp))
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (uiState.subiendoImagen) {
                                        CircularProgressIndicator(color = colorPrimary, modifier = Modifier.size(24.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text("Subiendo...", fontSize = 12.sp, color = colorPrimary)
                                    } else {
                                        Icon(Icons.Default.AddPhotoAlternate, null, tint = colorPrimary, modifier = Modifier.size(32.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text("Añadir foto", fontSize = 14.sp, color = colorPrimary, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }

                        MinimalTextField(
                            value = uiState.descripcion,
                            onValueChange = viewModel::actualizarDescripcion,
                            placeholder = "Descripción (opcional)",
                            singleLine = false,
                            minLines = 4
                        )
                    }
                }

                // 3. Fecha y Hora
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(600, 300)) + slideInVertically(tween(600, 300)) { 20 }
                ) {
                    SectionCard(title = "Fecha y Hora") {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DateTimeInput(
                                label = "FECHA",
                                value = if (uiState.fecha.isBlank()) "Fecha" else {
                                    try {
                                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(uiState.fecha)
                                        SimpleDateFormat("dd MMM", Locale.getDefault()).format(date!!).uppercase()
                                    } catch (e: Exception) { uiState.fecha }
                                },
                                icon = Icons.Default.CalendarMonth,
                                isDark = isDark,
                                onClick = { mostrarDatePicker = true },
                                modifier = Modifier.weight(1f)
                            )
                            DateTimeInput(
                                label = "HORA",
                                value = uiState.hora.ifBlank { "Hora" },
                                icon = Icons.Default.Schedule,
                                isDark = isDark,
                                onClick = { mostrarTimePicker = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 4. Recordatorio
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(600, 400)) + slideInVertically(tween(600, 400)) { 20 }
                ) {
                    SectionCard(title = "Recordatorio") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Activar Aviso", 
                                fontSize = 16.sp, 
                                fontWeight = FontWeight.SemiBold, 
                                color = colorOnSurface
                            )
                            
                            val trackColor by animateColorAsState(
                                if (uiState.recordatorio) (if (isDark) colorPrimary else StitchPrimaryContainer) else (if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFE9E2D6)), 
                                label = "color"
                            )
                            Box(
                                modifier = Modifier
                                    .size(50.dp, 30.dp)
                                    .clip(CircleShape)
                                    .background(trackColor)
                                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { 
                                        viewModel.toggleRecordatorio() 
                                    }
                                    .padding(2.dp)
                            ) {
                                val thumbOffset by animateFloatAsState(if (uiState.recordatorio) 20f else 0f, label = "thumb")
                                Box(
                                    modifier = Modifier
                                        .offset(x = thumbOffset.dp)
                                        .size(26.dp)
                                        .shadow(2.dp, CircleShape)
                                        .background(Color.White, CircleShape)
                                )
                            }
                        }

                        if (uiState.recordatorio) {
                            Box(modifier = Modifier.padding(top = 8.dp)) {
                                val opcionActual = RecordatorioOpcion.entries.find { it.minutos == uiState.minutosAntes } ?: RecordatorioOpcion.UNA_HORA
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(32.dp))
                                        .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF4EDE1))
                                        .clickable { mostrarReminderPicker = true }
                                        .padding(horizontal = 20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = opcionActual.etiqueta,
                                        fontSize = 16.sp,
                                        color = colorOnSurface
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = colorOnSurfaceVariant
                                    )
                                }

                                if (mostrarReminderPicker) {
                                    DropdownMenu(
                                        expanded = mostrarReminderPicker,
                                        onDismissRequest = { mostrarReminderPicker = false },
                                        modifier = Modifier.fillMaxWidth(0.8f).background(if (isDark) Color(0xFF2D2921) else Color.White)
                                    ) {
                                        RecordatorioOpcion.entries.forEach { opcion ->
                                            DropdownMenuItem(
                                                text = { Text(opcion.etiqueta, color = colorOnSurface) },
                                                onClick = {
                                                    viewModel.actualizarMinutosAntes(opcion.minutos)
                                                    mostrarReminderPicker = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        Text(
                            "Tu pareja también recibirá una notificación cuando se cree este evento.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorOnSurfaceVariant.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                if (uiState.error != null) {
                    Text(
                        uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
                
                Spacer(Modifier.height(100.dp))
            }
        }

        if (mostrarConfeti) {
            ConfettiOverlay()
            Box(
                modifier = Modifier.fillMaxSize().background((if (isDark) Color.Black else Color.White).copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CheckCircle, 
                        contentDescription = null, 
                        tint = if (isDark) Color(0xFFB5CDB2) else Color(0xFFD1E9CD), 
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "¡Evento Guardado!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = colorPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun EventTypeButton(
    tipo: TipoEvento,
    label: String,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else StitchPrimary
    val colorPrimaryContainer = if (isDark) colorPrimary.copy(alpha = 0.2f) else StitchPrimaryContainer
    val colorSurfaceVariant = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF4EDE1)
    val colorOnSurface = if (isDark) Color(0xFFE7BBC6) else StitchOnSurface
    val colorOnSurfaceVariant = if (isDark) Color(0xFFD3C3C5) else StitchOnSurfaceVariant

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "scale")

    Column(
        modifier = modifier
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (isSelected) colorPrimaryContainer else colorSurfaceVariant)
                .border(2.dp, if (isSelected) colorPrimary else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tipo.icono,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = if (isSelected) colorPrimary else colorOnSurfaceVariant
            )
        }
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) colorOnSurface else colorOnSurfaceVariant
        )
    }
}

@Composable
fun ConfettiOverlay() {
    val particles = remember { List(50) { ConfettiParticle() } }
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val y = (particle.y + progress * size.height * particle.speed) % size.height
            val x = particle.x * size.width + (Math.sin(progress.toDouble() * 8 + particle.x.toDouble()) * 30).toFloat()
            drawCircle(
                color = particle.color.copy(alpha = 0.5f),
                radius = 12f,
                center = Offset(x, y)
            )
        }
    }
}

class ConfettiParticle {
    val x = Random.nextFloat()
    val y = Random.nextFloat()
    val speed = 0.5f + Random.nextFloat()
    val color = listOf(
        Color(0xFFFFD1DC), // Rosa
        Color(0xFFD1E9CD), // Verde
        Color(0xFFFFF0E0), // Durazno
        Color(0xFFE8F5E9)  // Menta
    ).random()
}
