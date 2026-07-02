package com.cadev.mocaapp.feature.eventos.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cadev.mocaapp.core.model.TipoEvento
import com.cadev.mocaapp.core.ui.*
import com.cadev.mocaapp.feature.eventos.domain.model.RecordatorioOpcion
import com.cadev.mocaapp.feature.ui.screens.LoadingTransition
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleEventoScreen(
    viewModel: EventoViewModel,
    eventoId: String,
    usuarioId: String,
    onRegresar: () -> Unit,
    onEditar: (String) -> Unit,
    onConvertirEnRecuerdo: (String, String) -> Unit
) {
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var mostrarConfirmarEliminar by remember { mutableStateOf(false) }
    var mostrarMenuOptions by remember { mutableStateOf(false) }
    var mostrarPosponerDialog by remember { mutableStateOf(false) }

    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else StitchPrimary
    val colorBackground = if (isDark) Color(0xFF1E1B14) else Color(0xFFF9F9F9)
    val colorOnSurface = if (isDark) Color(0xFFE7BBC6) else StitchOnSurface
    val colorOnSurfaceVariant = if (isDark) Color(0xFFD3C3C5) else StitchOnSurfaceVariant
    val colorBorder = colorPrimary.copy(alpha = if (isDark) 0.15f else 0.3f)

    LaunchedEffect(eventoId) {
        viewModel.cargarEvento(eventoId)
    }

    LaunchedEffect(uiState.eliminado) {
        if (uiState.eliminado) onRegresar()
    }

    val evento = uiState.eventoActual

    if (mostrarConfirmarEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarEliminar = false },
            containerColor = if (isDark) Color(0xFF2D2921) else Color.White,
            title = { Text("Eliminar evento", color = colorOnSurface) },
            text = { Text("¿Seguro que quieres eliminar este evento de vuestro santuario?", color = colorOnSurfaceVariant) },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmarEliminar = false
                        viewModel.eliminarEvento(context, eventoId)
                    }
                ) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmarEliminar = false }) { Text("Cancelar", color = colorOnSurfaceVariant) }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        evento?.titulo ?: "Detalle del Plan", 
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = colorPrimary
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar", tint = colorPrimary)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { mostrarMenuOptions = true }) {
                            Icon(Icons.Default.MoreVert, "Opciones", tint = colorPrimary)
                        }
                        DropdownMenu(
                            expanded = mostrarMenuOptions,
                            onDismissRequest = { mostrarMenuOptions = false },
                            modifier = Modifier.background(if (isDark) Color(0xFF2D2921) else Color.White)
                        ) {
                            if (evento?.creadoPor == usuarioId) {
                                DropdownMenuItem(
                                    text = { Text("Editar", color = colorOnSurface) },
                                    leadingIcon = { Icon(Icons.Default.Edit, null, tint = colorPrimary) },
                                    onClick = {
                                        mostrarMenuOptions = false
                                        onEditar(eventoId)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        mostrarMenuOptions = false
                                        mostrarConfirmarEliminar = true
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Posponer", color = colorOnSurface) },
                                leadingIcon = { Icon(Icons.Default.Update, null, tint = colorPrimary) },
                                onClick = {
                                    mostrarMenuOptions = false
                                    mostrarPosponerDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = (if (isDark) colorBackground else Color.White).copy(alpha = 0.6f),
                    scrolledContainerColor = (if (isDark) colorBackground else Color.White).copy(alpha = 0.8f)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorBackground)
                .drawBehind {
                    if (!isDark) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFD1DC).copy(alpha = 0.4f), Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.5f),
                                radius = size.width * 0.4f
                            ),
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.5f),
                            radius = size.width * 0.4f
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFCBE3C7).copy(alpha = 0.4f), Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.3f),
                                radius = size.width * 0.4f
                            ),
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.3f),
                            radius = size.width * 0.4f
                        )
                    }
                }
        ) {
            if (uiState.cargando || evento == null) {
                LoadingTransition()
            } else {
                val tipo = try { TipoEvento.valueOf(evento.tipo) } catch (e: Exception) { TipoEvento.OTRO }
                
                val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formatoLegible = SimpleDateFormat("EEEE, d 'de' MMMM", Locale.forLanguageTag("es-MX"))
                val fechaLegible = try {
                    formatoLegible.format(formatoEntrada.parse(evento.fecha)!!).replaceFirstChar { it.uppercase() }
                } catch (e: Exception) { evento.fecha }

                val hoy = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val eventTime = try { formatoEntrada.parse(evento.fecha)!!.time } catch(e: Exception) { 0L }
                val diasRestantes = ((eventTime - hoy) / (1000 * 60 * 60 * 24)).toInt()
                val esPasado = eventTime < hoy

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Hero Image (Floating)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(24.dp),
                                ambientColor = colorPrimary.copy(alpha = 0.2f),
                                spotColor = colorPrimary.copy(alpha = 0.2f)
                            )
                            .border(1.dp, colorBorder, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                    ) {
                        AsyncImage(
                            model = if (evento.fotoUrl.isNotBlank()) evento.fotoUrl else "https://lh3.googleusercontent.com/aida-public/AB6AXuCRc82V4KVP9FOlS_pp-thLvKjo5135tuSOmeemSlGXEiY082DiFRsQ_8-UQ2W45lwJXIBWtep4XIZKBn4xrmzniQuvKiwaaaULfVr-rwJ0HhIPEm-crI01cbJmAhC-vVlonbGBlPGi5ezXnXXurp5shwKaPJwcrnfdewwlY_THGw8jv5yhcK5A3sEuma3N5qF0M5zVo5h5ZMe9cYUpAlj2q_9vnP-_8rZ-kAe14_wu4QTGsyiMk4E4oRX4L1cLuCR686pbXIb-xlk",
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, colorBackground.copy(alpha = 0.8f)),
                                        startY = 400f
                                    )
                                )
                        )
                    }

                    // Ethereal Sphere Countdown & Main Details
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-64).dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 3D Sphere (Floating effect)
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .shadow(
                                    elevation = 32.dp,
                                    shape = CircleShape,
                                    ambientColor = colorPrimary.copy(alpha = 0.2f),
                                    spotColor = colorPrimary.copy(alpha = 0.2f)
                                )
                                .background(
                                    Brush.radialGradient(
                                        colors = if (isDark) listOf(Color(0xFF442D34), Color(0xFF2D141C)) else listOf(Color.White, Color(0xFFFFD1DC), Color(0xFFE8BBC5)),
                                        center = androidx.compose.ui.geometry.Offset(80f, 80f)
                                    ),
                                    CircleShape
                                )
                                .border(1.dp, colorBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (diasRestantes == 0) "HOY" else if (diasRestantes < 0) Math.abs(diasRestantes).toString() else diasRestantes.toString(),
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontSize = if (diasRestantes == 0) 36.sp else 48.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif,
                                        color = colorPrimary
                                    )
                                )
                                if (diasRestantes != 0) {
                                    Text(
                                        text = if (esPasado) "DÍAS ATRÁS" else "DÍAS",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = colorPrimary.copy(alpha = 0.7f),
                                            letterSpacing = 2.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Glass Overlay Card (Floating effect)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .shadow(
                                    elevation = 40.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    ambientColor = Color.Black.copy(alpha = 0.1f),
                                    spotColor = Color.Black.copy(alpha = 0.1f)
                                )
                                .then(
                                    if (isDark) Modifier.background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                                    else Modifier.background(
                                        Brush.linearGradient(
                                            colors = listOf(Color.White.copy(alpha = 0.8f), Color.White.copy(alpha = 0.4f))
                                        ),
                                        RoundedCornerShape(24.dp)
                                    )
                                )
                                .border(1.dp, colorBorder, RoundedCornerShape(24.dp))
                                .blur(0.5.dp)
                                .padding(top = 40.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(
                                    text = evento.titulo,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif,
                                        color = colorPrimary
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "$fechaLegible • ${evento.hora} hrs",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = colorOnSurfaceVariant
                                    )
                                )
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    BadgeTag(icon = tipo.icono, text = tipo.etiqueta, containerColor = if (isDark) colorPrimary.copy(alpha = 0.2f) else Color(0xFFFFD9E1), contentColor = colorPrimary)
                                    BadgeTag(icon = Icons.Default.Favorite, text = "Especial", containerColor = if (isDark) Color(0xFF374C37) else Color(0xFFD1E9CD), contentColor = if (isDark) Color(0xFFB5CDB2) else colorPrimary)
                                }
                            }
                        }
                    }

                    // Location Card (Floating)
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .offset(y = (-32).dp)
                            .fillMaxWidth()
                            .shadow(
                                elevation = 32.dp,
                                shape = RoundedCornerShape(24.dp),
                                ambientColor = Color.Black.copy(alpha = 0.1f),
                                spotColor = Color.Black.copy(alpha = 0.1f)
                            )
                            .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.65f), RoundedCornerShape(24.dp))
                            .border(1.dp, colorBorder, RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DetailRowItem(
                            icon = Icons.Default.CalendarMonth,
                            text = fechaLegible,
                            subtext = "A las ${evento.hora} hrs",
                            isDark = isDark
                        )

                        DetailRowItem(
                            icon = Icons.Default.LocationOn,
                            text = if (evento.lugar.isNotBlank()) evento.lugar else "Vuestro Santuario",
                            subtext = "Ubicación",
                            isDark = isDark
                        )
                    }

                    // Notes Card (Floating)
                    if (evento.descripcion.isNotBlank()) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .offset(y = (-32).dp)
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 32.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    ambientColor = Color.Black.copy(alpha = 0.1f),
                                    spotColor = Color.Black.copy(alpha = 0.1f)
                                )
                                .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.65f), RoundedCornerShape(24.dp))
                                .border(1.dp, colorBorder, RoundedCornerShape(24.dp))
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Default.EditNote, null, tint = colorPrimary, modifier = Modifier.size(24.dp))
                                Text(
                                    "Notas Especiales",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = colorPrimary
                                    )
                                )
                            }
                            Text(
                                text = evento.descripcion,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = colorOnSurfaceVariant,
                                    lineHeight = 24.sp,
                                    fontStyle = FontStyle.Italic
                                )
                            )
                        }
                    }

                    // Primary Action (Floating)
                    if (esPasado && !evento.convertidoEnRecuerdo) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp, horizontal = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = {
                                    viewModel.marcarComoRecuerdo(eventoId)
                                    onConvertirEnRecuerdo(evento.fecha, evento.titulo)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .shadow(
                                        elevation = 20.dp,
                                        shape = CircleShape,
                                        spotColor = if (isDark) colorPrimary else Color(0xFFFFD1DC)
                                    ),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDark) colorPrimary else StitchPrimaryContainer,
                                    contentColor = if (isDark) Color.Black else StitchPrimary
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Icon(Icons.Default.Stars, null, modifier = Modifier.size(20.dp))
                                    Text(
                                        "CONVERTIR EN RECUERDO",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }

    if (mostrarPosponerDialog) {
        PosponerDialog(
            actualFecha = evento?.fecha ?: "",
            actualHora = evento?.hora ?: "12:00",
            isDark = isDark,
            onDismiss = { mostrarPosponerDialog = false },
            onConfirm = { nuevaFecha, nuevaHora ->
                viewModel.posponerEvento(eventoId, nuevaFecha, nuevaHora)
                mostrarPosponerDialog = false
            }
        )
    }
}

@Composable
private fun BadgeTag(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, containerColor: Color, contentColor: Color) {
    Surface(
        color = containerColor,
        shape = CircleShape,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = contentColor)
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

@Composable
private fun DetailRowItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, subtext: String, isDark: Boolean) {
    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else StitchPrimary
    val colorOnSurface = if (isDark) Color(0xFFE7BBC6) else StitchOnSurface
    val colorOnSurfaceVariant = if (isDark) Color(0xFFD3C3C5) else StitchOnSurfaceVariant

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = colorPrimary, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(
                text = text, 
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = colorOnSurface)
            )
            Text(
                text = subtext, 
                style = MaterialTheme.typography.bodySmall.copy(color = colorOnSurfaceVariant)
            )
        }
    }
}

@Composable
fun PosponerDialog(
    actualFecha: String,
    actualHora: String,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val context = LocalContext.current
    var fecha by remember { mutableStateOf(actualFecha) }
    var hora by remember { mutableStateOf(actualHora) }
    val colorOnSurface = if (isDark) Color(0xFFE7BBC6) else StitchOnSurface
    val colorOnSurfaceVariant = if (isDark) Color(0xFFD3C3C5) else StitchOnSurfaceVariant

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Color(0xFF2D2921) else Color.White,
        title = { Text("Posponer evento", color = colorOnSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Elige la nueva fecha y hora para vuestro plan.", color = colorOnSurfaceVariant)
                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(context, { _, y, m, d ->
                            fecha = "%04d-%02d-%02d".format(y, m + 1, d)
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, null)
                    Spacer(Modifier.width(8.dp))
                    Text(fecha)
                }
                OutlinedButton(
                    onClick = {
                        val split = hora.split(":")
                        val h = split.getOrNull(0)?.toIntOrNull() ?: 12
                        val m = split.getOrNull(1)?.toIntOrNull() ?: 0
                        TimePickerDialog(context, { _, selectedH, selectedM ->
                            hora = "%02d:%02d".format(selectedH, selectedM)
                        }, h, m, true).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AccessTime, null)
                    Spacer(Modifier.width(8.dp))
                    Text(hora)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(fecha, hora) }) { Text("Confirmar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = colorOnSurfaceVariant) }
        }
    )
}
