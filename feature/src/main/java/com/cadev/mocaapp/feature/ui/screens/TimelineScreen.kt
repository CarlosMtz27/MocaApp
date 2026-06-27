package com.cadev.mocaapp.feature.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cadev.mocaapp.feature.diario.domain.model.Emocion
import com.cadev.mocaapp.feature.diario.domain.model.EntradaDiario
import com.cadev.mocaapp.feature.diario.ui.DiarioViewModel
import com.cadev.mocaapp.feature.diario.ui.FiltroListado
import com.cadev.mocaapp.feature.diario.ui.OrdenListado
import com.cadev.mocaapp.feature.eventos.domain.model.Evento
import com.cadev.mocaapp.feature.ui.animations.AnimacionFadeIn
import com.cadev.mocaapp.feature.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * VISTA DE HISTORIAL COMBINADO (SECCIÓN 4.5)
 * Fiel al diseño "Timeline - Our Sanctuary" del HTML.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: DiarioViewModel,
    usuarioId: String,
    parejaId: String?,
    relacionId: String,
    onVerDetalleEntrada: (String) -> Unit,
    onVerDetalleEvento: (String) -> Unit,
    onIrAAjustes: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(usuarioId, parejaId, relacionId) {
        viewModel.iniciarEscucha(usuarioId, parejaId, relacionId)
    }

    // Agrupar items por mes
    val itemsAgrupados = remember(uiState.entradas, uiState.eventos, uiState.filtro, uiState.orden) {
        val listaCompleta = mutableListOf<Any>()
        
        if (uiState.filtro == FiltroListado.TODOS || uiState.filtro == FiltroListado.RECUERDOS) {
            listaCompleta.addAll(uiState.entradas)
        }
        if (uiState.filtro == FiltroListado.TODOS || uiState.filtro == FiltroListado.EVENTOS) {
            listaCompleta.addAll(uiState.eventos)
        }

        val sdfMes = SimpleDateFormat("MMMM yyyy", Locale("es", "MX"))
        val sdfFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val sortedList = if (uiState.orden == OrdenListado.RECIENTE) {
            listaCompleta.sortedByDescending { if (it is EntradaDiario) it.fecha else (it as Evento).fecha }
        } else {
            listaCompleta.sortedBy { if (it is EntradaDiario) it.fecha else (it as Evento).fecha }
        }

        sortedList.groupBy { item ->
            val fechaStr = if (item is EntradaDiario) item.fecha else (item as Evento).fecha
            val date = sdfFecha.parse(fechaStr) ?: Date()
            sdfMes.format(date).replaceFirstChar { it.uppercase() }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Our Sanctuary",
                        style = OrganicTypography.headlineMedium.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                        color = MocaPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = MocaPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onIrAAjustes) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MocaPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MocaBackground)
            )
        },
        containerColor = MocaBackground
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Filtros
            FilterTabsRow(
                filtroActual = uiState.filtro,
                onCambiarFiltro = { viewModel.cambiarFiltro(it) },
                onToggleOrden = {
                    viewModel.cambiarOrden(
                        if (uiState.orden == OrdenListado.RECIENTE) OrdenListado.ANTIGUO else OrdenListado.RECIENTE
                    )
                }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Línea discontinua vertical (Timeline Line)
                        val strokeWidth = 1.dp.toPx()
                        val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        drawLine(
                            color = MocaOutlineVariant,
                            start = Offset(24.dp.toPx(), 0f),
                            end = Offset(24.dp.toPx(), size.height),
                            strokeWidth = strokeWidth,
                            pathEffect = dashPathEffect
                        )
                    },
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                itemsAgrupados.forEach { (mes, itemsDelMes) ->
                    stickyHeader {
                        MonthHeader(mes)
                    }

                    items(itemsDelMes) { item ->
                        AnimacionFadeIn {
                            TimelineItemEntry(
                                item = item,
                                onClick = {
                                    if (item is EntradaDiario) onVerDetalleEntrada(item.id)
                                    else onVerDetalleEvento((item as Evento).id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterTabsRow(
    filtroActual: FiltroListado,
    onCambiarFiltro: (FiltroListado) -> Unit,
    onToggleOrden: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterTabButton(
                text = "Todo",
                selected = filtroActual == FiltroListado.TODOS,
                onClick = { onCambiarFiltro(FiltroListado.TODOS) }
            )
            FilterTabButton(
                text = "Recuerdos",
                selected = filtroActual == FiltroListado.RECUERDOS,
                onClick = { onCambiarFiltro(FiltroListado.RECUERDOS) }
            )
            FilterTabButton(
                text = "Eventos",
                selected = filtroActual == FiltroListado.EVENTOS,
                onClick = { onCambiarFiltro(FiltroListado.EVENTOS) }
            )
        }
        
        IconButton(
            onClick = onToggleOrden,
            modifier = Modifier
                .size(40.dp)
                .background(MocaSurfaceContainer, CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort", tint = MocaOnSurfaceVariant)
        }
    }
}

@Composable
fun FilterTabButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) MocaPrimaryContainer else MocaSurfaceContainer,
        modifier = Modifier.height(40.dp)
    ) {
        Box(modifier = Modifier.padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = OrganicTypography.labelMedium,
                color = if (selected) MocaOnPrimaryContainer else MocaOnSurfaceVariant
            )
        }
    }
}

@Composable
fun MonthHeader(mes: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MocaBackground.copy(alpha = 0.9f))
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dot on the line
        Box(
            modifier = Modifier
                .size(12.dp)
                .offset(x = 2.dp) // Alineado con la línea dashed
                .background(MocaPrimaryFixedDim, CircleShape)
                .border(4.dp, MocaBackground, CircleShape)
        )
        
        Text(
            text = mes,
            modifier = Modifier.padding(start = 24.dp),
            style = OrganicTypography.headlineMedium.copy(
                fontSize = 24.sp,
                fontStyle = FontStyle.Italic,
                fontFamily = SerifFontFamily
            ),
            color = MocaOnPrimaryFixedVariant
        )
    }
}

@Composable
fun TimelineItemEntry(item: Any, onClick: () -> Unit) {
    val esEntrada = item is EntradaDiario
    val titulo = if (esEntrada) (item as EntradaDiario).titulo else (item as Evento).titulo
    val fechaStr = if (esEntrada) (item as EntradaDiario).fecha else (item as Evento).fecha
    
    val subtitulo = if (esEntrada) (item as EntradaDiario).detalles else {
        val ev = item as Evento
        "${ev.descripcion.ifBlank { "Sin descripción" }} • ${ev.hora}"
    }

    val imagenUrl = if (esEntrada) {
        (item as EntradaDiario).fotos.firstOrNull()
    } else {
        (item as Evento).fotoUrl.ifBlank { null }
    }
    
    val diaNum = remember(fechaStr) {
        try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fechaStr) ?: Date()
            SimpleDateFormat("dd MMM", Locale("es", "MX")).format(date)
        } catch (e: Exception) { "" }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 24.dp, bottom = 32.dp)
            .clickable(onClick = onClick)
    ) {
        // Icono en la línea dashed
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    if (esEntrada) MocaSurfaceContainerHighest else MocaTertiaryContainer,
                    CircleShape
                )
                .border(4.dp, MocaBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (esEntrada) Icons.Default.AttachFile else Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(10.dp),
                tint = if (esEntrada) MocaOnSurfaceVariant else MocaOnTertiaryContainer
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Card de contenido
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(32.dp),
            color = if (esEntrada) MocaSurfaceContainerLowest else MocaTertiaryFixed.copy(alpha = 0.3f),
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "$diaNum • ${if (esEntrada) "Recuerdo" else "Evento"}",
                    style = OrganicTypography.labelSmall.copy(letterSpacing = 1.sp),
                    color = if (esEntrada) MocaOnSurfaceVariant.copy(alpha = 0.7f) else MocaTertiary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // SI HAY IMAGEN, LA MOSTRAMOS AQUÍ
                    if (imagenUrl != null) {
                        AsyncImage(
                            model = imagenUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = titulo,
                            style = OrganicTypography.headlineMedium.copy(
                                fontSize = 20.sp,
                                fontFamily = SerifFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MocaOnSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = subtitulo,
                            style = OrganicTypography.bodyMedium,
                            color = if (esEntrada) MocaOnSurfaceVariant else MocaTertiary.copy(alpha = 0.8f),
                            maxLines = 3
                        )
                    }
                }
                
                // Emociones (solo para recuerdos/días)
                if (esEntrada && (item as EntradaDiario).emociones.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item.emociones.take(3).forEach { emocionId ->
                            val etiqueta = try { Emocion.valueOf(emocionId).etiqueta } catch(e: Exception) { emocionId }
                            Surface(
                                shape = CircleShape,
                                color = MocaSurfaceContainer,
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Text(
                                    text = etiqueta,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = OrganicTypography.labelSmall.copy(fontSize = 10.sp),
                                    color = MocaOnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Extensiones de color faltantes
val MocaPrimaryFixedDim = Color(0xFFE7BBC6)
val MocaOnPrimaryFixedVariant = Color(0xFF5E3E47)
val MocaTertiaryFixed = Color(0xFFD1E9CD)
val MocaOnTertiaryFixedVariant = Color(0xFF374C37)
val MocaSurfaceContainerHighest = Color(0xFFE9E2D6)
