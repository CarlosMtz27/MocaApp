package com.cadev.mocaapp.feature.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
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
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import com.cadev.mocaapp.feature.diario.ui.DiarioViewModel
import com.cadev.mocaapp.feature.diario.ui.FiltroListado
import com.cadev.mocaapp.feature.diario.ui.OrdenListado
import com.cadev.mocaapp.feature.eventos.domain.model.Evento
import com.cadev.mocaapp.feature.ui.animations.AnimacionFadeIn
import com.cadev.mocaapp.feature.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.ui.res.painterResource
import com.cadev.mocaapp.feature.R

/**
 * VISTA DE HISTORIAL COMBINADO (SECCIÓN 4.5)
 * Ahora integrada en el MainScaffold.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineScreen(
    viewModel: DiarioViewModel,
    usuarioId: String,
    parejaId: String?,
    relacionId: String,
    onVerDetalleEntrada: (String) -> Unit,
    onVerDetalleEvento: (String) -> Unit,
    onIrAAjustes: () -> Unit,
    onRegresar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    
    LaunchedEffect(usuarioId, parejaId, relacionId) {
        viewModel.iniciarEscucha(usuarioId, parejaId, relacionId)
    }

    val itemsAplanados = remember(uiState.entradas, uiState.eventos, uiState.filtro, uiState.orden) {
        val listaCompleta = mutableListOf<Any>()
        
        when (uiState.filtro) {
            FiltroListado.TODOS -> {
                listaCompleta.addAll(uiState.entradas)
                listaCompleta.addAll(uiState.eventos)
            }
            FiltroListado.RECUERDOS -> {
                listaCompleta.addAll(uiState.entradas.filter { it.tipo == TipoEntrada.RECUERDO.name })
            }
            FiltroListado.EVENTOS -> {
                listaCompleta.addAll(uiState.eventos)
            }
            FiltroListado.DIAS -> {
                listaCompleta.addAll(uiState.entradas.filter { it.tipo == TipoEntrada.MI_DIA.name })
            }
        }

        if (uiState.orden == OrdenListado.RECIENTE) {
            listaCompleta.sortedByDescending { if (it is EntradaDiario) it.fecha else (it as Evento).fecha }
        } else {
            listaCompleta.sortedBy { if (it is EntradaDiario) it.fecha else (it as Evento).fecha }
        }
    }

    val itemsAgrupados = remember(itemsAplanados) {
        val sdfMes = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("es-MX"))
        val sdfFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        itemsAplanados.groupBy { item ->
            val fechaStr = if (item is EntradaDiario) item.fecha else (item as Evento).fecha
            val date = sdfFecha.parse(fechaStr) ?: Date()
            sdfMes.format(date).replaceFirstChar { it.uppercase() }
        }
    }

    val mesVisibleActual = remember(itemsAgrupados) {
        derivedStateOf {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            var currentIndex = 0
            var foundMonth = "Nuestra Historia" 
            for ((mes, items) in itemsAgrupados) {
                if (firstVisibleIndex <= currentIndex + items.size) {
                    foundMonth = mes
                    break
                }
                currentIndex += items.size + 1
            }
            foundMonth
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            // Barra de acciones superior (dentro del contenido para dar espacio al Header de MainScreen)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onRegresar) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = MocaPrimary)
                }
                Spacer(Modifier.width(8.dp))
                FilterTabsRow(
                    filtroActual = uiState.filtro,
                    onCambiarFiltro = { viewModel.cambiarFiltro(it) },
                    onToggleOrden = {
                        viewModel.cambiarOrden(
                            if (uiState.orden == OrdenListado.RECIENTE) OrdenListado.ANTIGUO else OrdenListado.RECIENTE
                        )
                    }
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
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

        // BURBUJA FLOTANTE ESTILO LAUNCHER
        AnimatedVisibility(
            visible = listState.isScrollInProgress,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
        ) {
            Surface(
                color = MocaAccentPink,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = mesVisibleActual.value,
                    style = OrganicTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
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
            .fillMaxWidth(),
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
                text = "Días",
                selected = filtroActual == FiltroListado.DIAS,
                onClick = { onCambiarFiltro(FiltroListado.DIAS) }
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
        modifier = Modifier.height(36.dp)
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = OrganicTypography.labelMedium.copy(fontSize = 12.sp),
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
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .offset(x = 2.dp)
                .background(MocaPrimaryFixedDim, CircleShape)
                .border(4.dp, MocaBackground, CircleShape)
        )
        
        Text(
            text = mes,
            modifier = Modifier.padding(start = 24.dp),
            style = OrganicTypography.headlineMedium.copy(
                fontSize = 22.sp,
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
            SimpleDateFormat("dd MMM", Locale.forLanguageTag("es-MX")).format(date)
        } catch (e: Exception) { "" }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 24.dp, bottom = 32.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp) // Aumentado para mejor visibilidad
                .background(
                    if (esEntrada) MocaSurfaceContainerHighest else MocaTertiaryContainer,
                    CircleShape
                )
                .border(4.dp, MocaBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val iconoTira = when {
                !esEntrada -> Icons.Default.Event
                (item as EntradaDiario).tipo == TipoEntrada.RECUERDO.name -> Icons.Default.AutoAwesome
                else -> Icons.Default.EditNote
            }
            Icon(
                imageVector = iconoTira,
                contentDescription = null,
                modifier = Modifier.size(16.dp), // Icono más grande
                tint = if (esEntrada) MocaOnSurfaceVariant else MocaOnTertiaryContainer
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .then(
                    if (esEntrada) Modifier.shadow(2.dp, RoundedCornerShape(32.dp))
                    else Modifier
                )
                .clip(RoundedCornerShape(32.dp))
                .background(
                    if (esEntrada) MocaSurfaceContainerLowest else MocaTertiaryFixed.copy(alpha = 0.5f)
                )
        ) {
            // FONDO DECORATIVO - SOLO PARA EVENTOS
            if (!esEntrada) {
                Image(
                    painter = painterResource(id = R.drawable.eventos),
                    contentDescription = null,
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0.5f) // Más visible
                        .blur(8.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(20.dp)) {
                val etiquetaTipo = when {
                    esEntrada -> if ((item as EntradaDiario).tipo == TipoEntrada.RECUERDO.name) "Recuerdo" else "Día"
                    else -> "Evento"
                }
                Text(
                    text = "$diaNum • $etiquetaTipo",
                    style = OrganicTypography.labelSmall.copy(letterSpacing = 1.sp),
                    color = if (esEntrada) MocaOnSurfaceVariant.copy(alpha = 0.6f) else MocaOnSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
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
                            color = if (esEntrada) MocaOnSurfaceVariant else MocaOnSurface.copy(alpha = 0.8f),
                            maxLines = 3
                        )
                    }
                }
                
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
