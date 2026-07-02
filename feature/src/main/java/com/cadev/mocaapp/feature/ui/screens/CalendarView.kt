package com.cadev.mocaapp.feature.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cadev.mocaapp.feature.diario.domain.model.DiaCalendarioInfo
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import com.cadev.mocaapp.feature.diario.ui.DiarioViewModel
import com.cadev.mocaapp.feature.ui.animations.AnimacionFadeIn
import com.cadev.mocaapp.feature.ui.components.GlassCard
import com.cadev.mocaapp.feature.ui.components.NeuButton
import com.cadev.mocaapp.feature.ui.components.neuFlat
import com.cadev.mocaapp.feature.ui.components.neuInset
import com.cadev.mocaapp.feature.ui.theme.*
import com.cadev.mocaapp.feature.ui.utils.FondoMeshMoca
import java.text.SimpleDateFormat
import java.util.*

/**
 * PANTALLA DE CALENDARIO DE RECUERDOS (SECCIÓN 4.1)

 */
@Composable
fun CalendarView(
    viewModel: DiarioViewModel,
    usuarioId: String,
    parejaId: String?,
    relacionId: String,
    onRegresar: () -> Unit,
    onDiaSeleccionado: (fecha: String) -> Unit,
    onVerListado: () -> Unit,
    onVerEventos: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var calendario by remember { mutableStateOf(Calendar.getInstance()) }
    val isDark = esModoOscuro()
    
    val mesNombre = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("es-MX"))
        .format(calendario.time)
        .replaceFirstChar { it.uppercase() }

    LaunchedEffect(usuarioId, parejaId, relacionId) {
        viewModel.iniciarEscucha(usuarioId, parejaId, relacionId)
    }

    FondoMeshMoca {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // CABECERA: MES Y NAVEGACIÓN
            AnimacionFadeIn {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NeuButton(
                        onClick = {
                            calendario = (calendario.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                        },
                        modifier = Modifier.size(40.dp),
                        radioBorde = 20.dp,
                        colorFondo = MaterialTheme.colorScheme.surface
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft, 
                            contentDescription = "Anterior", 
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = mesNombre,
                        style = OrganicTypography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    NeuButton(
                        onClick = {
                            calendario = (calendario.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                        },
                        modifier = Modifier.size(40.dp),
                        radioBorde = 20.dp,
                        colorFondo = MaterialTheme.colorScheme.surface
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight, 
                            contentDescription = "Siguiente", 
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // CUADRÍCULA DEL CALENDARIO
            AnimacionFadeIn(delayMillis = 200) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neuFlat(
                            radioBorde = 32.dp,
                            colorFondo = MaterialTheme.colorScheme.surface,
                            sombraOscura = if (isDark) Color.Black.copy(alpha = 0.5f) else Color(0xFFE8E0D5)
                        )
                        .padding(horizontal = 12.dp, vertical = 20.dp)
                ) {
                    CalendarioGrid(
                        calendario = calendario,
                        diasConEntrada = uiState.diasConEntrada,
                        onDiaClick = onDiaSeleccionado
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // LEYENDA (Glassmorphism)
            AnimacionFadeIn(delayMillis = 400) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LeyendaItem(color = Color(0xFF4ADE80), texto = "Mi día")
                    Spacer(modifier = Modifier.width(12.dp))
                    LeyendaItem(color = Color(0xFF60A5FA), texto = "Recuerdo")
                    Spacer(modifier = Modifier.width(12.dp))
                    LeyendaItem(color = Color(0xFFFACC15), texto = "Evento")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BOTONES DE ACCIÓN (Glassmorphism)
            AnimacionFadeIn(delayMillis = 600) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionButton(
                        texto = "Ver Lista",
                        icono = Icons.AutoMirrored.Filled.ViewList,
                        onClick = onVerListado,
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        texto = "Eventos",
                        icono = Icons.AutoMirrored.Filled.EventNote,
                        onClick = onVerEventos,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarioGrid(
    calendario: Calendar,
    diasConEntrada: Map<String, DiaCalendarioInfo>,
    onDiaClick: (String) -> Unit
) {
    val diasSemana = listOf("D", "L", "M", "X", "J", "V", "S")
    val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    val cal = calendario.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val primerDiaSemana = cal.get(Calendar.DAY_OF_WEEK) - 1
    val maxDias = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val items = mutableListOf<Int?>()
    repeat(primerDiaSemana) { items.add(null) }
    for (i in 1..maxDias) { items.add(i) }

    Column {
        // Días de la semana
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            diasSemana.forEach { dia ->
                Text(
                    text = dia,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = OrganicTypography.labelSmall,
                    color = MaterialTheme.colorScheme.primary, // Cambiado a rosa para visibilidad
                    letterSpacing = 2.sp
                )
            }
        }

        // Días del mes
        val filas = items.chunked(7)
        filas.forEach { semana ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                semana.forEach { dia ->
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (dia != null) {
                            val fechaCal = (calendario.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, dia) }
                            val fechaStr = formatoFecha.format(fechaCal.time)
                            val info = diasConEntrada[fechaStr]
                            
                            val hoyStr = formatoFecha.format(Date())
                            val esFuturo = fechaStr > hoyStr
                            val esHoy = fechaStr == hoyStr

                            DiaCelda(
                                numero = dia,
                                info = info,
                                esFuturo = esFuturo,
                                esHoy = esHoy,
                                onClick = { onDiaClick(fechaStr) }
                            )
                        }
                    }
                }
                // Rellenar si la semana no está completa
                if (semana.size < 7) {
                    repeat(7 - semana.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun DiaCelda(
    numero: Int,
    info: DiaCalendarioInfo?,
    esFuturo: Boolean,
    esHoy: Boolean,
    onClick: () -> Unit
) {
    val tieneFoto = !info?.primeraFoto.isNullOrBlank()
    val tieneEntrada = info != null && info.tipos.isNotEmpty()

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(MocaShapes.medium)
            .then(
                if (tieneFoto) Modifier.neuInset(radioBorde = 12.dp)
                else Modifier
            )
            .alpha(if (esFuturo) 0.6f else 1f)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (tieneFoto) {
            AsyncImage(
                model = info?.primeraFoto,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().blur(0.5.dp),
                contentScale = ContentScale.Crop,
                alpha = 0.6f
            )
        }

        // Corazón para el día actual
        if (esHoy) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = numero.toString(),
            style = OrganicTypography.bodyLarge.copy(
                fontWeight = if (tieneEntrada || esHoy) FontWeight.Bold else FontWeight.Normal,
                fontSize = 18.sp
            ),
            color = if (tieneFoto) Color.White else MaterialTheme.colorScheme.primary
        )

        // Indicadores de tipo (puntitos) siempre visibles
        if (tieneEntrada) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                info?.tipos?.distinct()?.take(3)?.forEach { tipoNombre ->
                    val colorPunto = when {
                        tipoNombre.startsWith("EVENTO_") -> Color(0xFFFACC15) // Amarillo Evento
                        tipoNombre == TipoEntrada.RECUERDO.name -> Color(0xFF60A5FA) // Azul Recuerdo
                        tipoNombre == TipoEntrada.MI_DIA.name -> Color(0xFF4ADE80) // Verde Mi Día
                        else -> MocaPrimaryContainer
                    }
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(colorPunto, CircleShape)
                            .border(0.5.dp, Color.White.copy(alpha = 0.8f), CircleShape) // Borde para visibilidad sobre fotos
                    )
                }
            }
        }
    }
}

@Composable
fun LeyendaItem(color: Color, texto: String) {
    GlassCard(
        bordeRedondeado = 20.dp,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = texto, 
                style = OrganicTypography.labelSmall, 
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ActionButton(
    texto: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        alHacerClick = onClick,
        bordeRedondeado = 24.dp,
        modifier = modifier.height(56.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary, 
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = texto,
                style = OrganicTypography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
