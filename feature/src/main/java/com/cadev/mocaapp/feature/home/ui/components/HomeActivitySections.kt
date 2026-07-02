package com.cadev.mocaapp.feature.home.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cadev.mocaapp.feature.ui.components.GlassCard
import kotlin.time.Duration.Companion.milliseconds

data class EventoPlan(val id: String, val titulo: String, val fechaHora: String, val urlImagen: String)
data class RecuerdoMemoria(val id: String, val titulo: String, val detalles: String, val urlImagen: String, val cantidadFotos: Int)

@Composable
fun HomeActivitySections(
    proximosEventos: List<EventoPlan>,
    pendientesEventos: List<EventoPlan>,
    recuerdosRecientes: List<RecuerdoMemoria>,
    onVerEvento: (String) -> Unit,
    onVerRecuerdo: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(titulo = "Planes de Pareja", isDark = isDark)

        // TabRow con indicador animado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    drawLine(
                        color = if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFD3C3C5).copy(alpha = 0.3f),
                        start = androidx.compose.ui.geometry.Offset(0f, size.height),
                        end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                        strokeWidth = strokeWidth
                    )
                }
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                TabItem(
                    text = "Próximos",
                    isSelected = selectedTab == 0,
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 0 }
                )
                TabItem(
                    text = "Pendientes",
                    isSelected = selectedTab == 1,
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 1 }
                )
            }
            
            // Línea indicadora animada
            val indicatorAlignment = animateAlignmentAsState(if (selectedTab == 0) Alignment.BottomStart else Alignment.BottomEnd)
            
            Box(
                modifier = Modifier
                    .align(indicatorAlignment.value)
                    .fillMaxWidth(0.5f)
                    .padding(horizontal = 40.dp)
                    .height(3.dp)
                    .background(if (isDark) Color(0xFFE7BBC6) else Color(0xFF78555E), RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                (fadeIn(animationSpec = tween(300)) + slideInHorizontally { if (targetState > initialState) 100 else -100 })
                    .togetherWith(fadeOut(animationSpec = tween(300)))
            },
            label = "TabContentTransition"
        ) { tabIndex ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val listaAMostrar = if (tabIndex == 0) proximosEventos else pendientesEventos
                
                if (listaAMostrar.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (tabIndex == 0) "Sin planes, ¡planea algo increíble!" else "¡Todo al día! No hay pendientes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDark) Color(0xFFD3C3C5) else Color(0xFF4F4446).copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    listaAMostrar.forEachIndexed { index, evento ->
                        AnimatedCardEntrance(index = index) {
                            PlanCard(evento = evento, isDark = isDark, onClick = { onVerEvento(evento.id) })
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        SectionHeader(titulo = "Recuerdos Recientes", isDark = isDark)

        val listState = rememberLazyListState()
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
        ) {
            itemsIndexed(recuerdosRecientes) { index, recuerdo ->
                AnimatedCardEntrance(index = index, delayMultiplier = 150) {
                    RecuerdoCard(recuerdo = recuerdo, isDark = isDark, onClick = { onVerRecuerdo(recuerdo.id) })
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun animateAlignmentAsState(targetAlignment: Alignment): State<Alignment> {
    val transition = updateTransition(targetAlignment, label = "AlignmentTransition")
    return transition.animateValue(
        typeConverter = TwoWayConverter({ AnimationVector1D(0f) }, { targetAlignment }),
        label = "AlignmentValue"
    ) { it }
}

@Composable
fun AnimatedCardEntrance(
    index: Int,
    delayMultiplier: Int = 100,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((index * delayMultiplier).toLong().milliseconds)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 20 },
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
fun SectionHeader(titulo: String, isDark: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = if (isDark) Color(0xFFE7BBC6) else Color(0xFF78555E),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = titulo,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else Color(0xFF1E1B14)
        )
    }
}

@Composable
fun TabItem(
    text: String,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else Color(0xFF78555E)
    val colorVariant = if (isDark) Color(0xFFD3C3C5) else Color(0xFF4F4446)

    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) colorPrimary else colorVariant
        )
    }
}

@Composable
fun PlanCard(evento: EventoPlan, isDark: Boolean, onClick: () -> Unit) {
    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else Color(0xFF78555E)
    
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        bordeRedondeado = 20.dp,
        colorFondo = if (isDark) Color.Black.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.4f),
        colorBorde = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.2f),
        alHacerClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.5f))
                    .border(1.dp, colorPrimary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = com.cadev.mocaapp.feature.R.drawable.ic_reaccion_corazon), 
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = evento.titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else Color(0xFF1E1B14),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = colorPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = evento.fechaHora,
                        fontSize = 14.sp,
                        color = colorPrimary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colorPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun RecuerdoCard(recuerdo: RecuerdoMemoria, isDark: Boolean, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.width(280.dp),
        bordeRedondeado = 24.dp,
        colorFondo = if (isDark) Color.Black.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.4f),
        colorBorde = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.2f),
        alHacerClick = onClick
    ) {
        Column {
            Box(modifier = Modifier.height(140.dp).fillMaxWidth()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(recuerdo.urlImagen)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                                startY = 100f
                            )
                        )
                )

                if (recuerdo.cantidadFotos > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = recuerdo.cantidadFotos.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = recuerdo.titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF1E1B14)
                )
                Text(
                    text = recuerdo.detalles,
                    fontSize = 14.sp,
                    color = if (isDark) Color(0xFFD3C3C5) else Color(0xFF4F4446),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
