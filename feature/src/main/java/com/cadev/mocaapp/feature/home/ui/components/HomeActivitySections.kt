package com.cadev.mocaapp.feature.home.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
    recuerdosRecientes: List<RecuerdoMemoria>,
    onVerEvento: (String) -> Unit,
    onVerRecuerdo: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Activity Hub",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E1B14),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        SectionHeader(titulo = "Planes de Pareja")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    drawLine(
                        color = Color(0xFFD3C3C5).copy(alpha = 0.3f),
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
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 0 }
                )
                TabItem(
                    text = "Pendientes",
                    isSelected = selectedTab == 1,
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
                    .background(Color(0xFF78555E), RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
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
                if (tabIndex == 0) {
                    proximosEventos.forEachIndexed { index, evento ->
                        AnimatedCardEntrance(index = index) {
                            PlanCard(evento = evento, onClick = { onVerEvento(evento.id) })
                        }
                    }
                } else {
                    proximosEventos.take(1).forEachIndexed { index, evento ->
                        AnimatedCardEntrance(index = index) {
                            PlanCard(evento = evento, onClick = { onVerEvento(evento.id) })
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        SectionHeader(titulo = "Recuerdos Recientes")

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
                    RecuerdoCard(recuerdo = recuerdo, onClick = { onVerRecuerdo(recuerdo.id) })
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
fun SectionHeader(titulo: String) {
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
            tint = Color(0xFF78555E),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = titulo,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E1B14)
        )
    }
}

@Composable
fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorPrimary = Color(0xFF78555E)
    val colorVariant = Color(0xFF4F4446)

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
fun PlanCard(evento: EventoPlan, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        bordeRedondeado = 20.dp,
        alHacerClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(evento.urlImagen)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .shadow(1.dp, RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = evento.titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E1B14),
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
                        tint = Color(0xFF78555E),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = evento.fechaHora,
                        fontSize = 14.sp,
                        color = Color(0xFF78555E)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF78555E),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun RecuerdoCard(recuerdo: RecuerdoMemoria, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.width(280.dp),
        bordeRedondeado = 24.dp,
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
                    color = Color(0xFF1E1B14)
                )
                Text(
                    text = recuerdo.detalles,
                    fontSize = 14.sp,
                    color = Color(0xFF4F4446),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
