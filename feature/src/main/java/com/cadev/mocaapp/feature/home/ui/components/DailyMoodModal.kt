package com.cadev.mocaapp.feature.home.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class MoodOption(val emoji: String, val label: String)

val MOOD_OPTIONS = listOf(
    MoodOption("😔", "Low"), MoodOption("😐", "Okay"), MoodOption("😊", "Good"),
    MoodOption("🤩", "Great"), MoodOption("😄", "Feliz"), MoodOption("😠", "Enojado"),
    MoodOption("😴", "Con sueño"), MoodOption("😢", "Triste"), MoodOption("😡", "Furioso"),
    MoodOption("😂", "Riendo"), MoodOption("😲", "Asombrado"), MoodOption("😎", "Genial"),
    MoodOption("😌", "Tranquilo"), MoodOption("⏳", "Esperando")
)

/**
 * Modificador personalizado para recrear el efecto Neumórfico exacto del HTML
 */
fun Modifier.neumorphicShadow(
    shape: androidx.compose.ui.graphics.Shape = CircleShape,
    offset: Dp = 4.dp,
    blur: Dp = 8.dp,
    lightColor: Color = Color.White,
    darkColor: Color = Color(0xFFE0D9CE),
    alpha: Float = 1f
) = this.drawBehind {
    val shadowColorDark = darkColor.copy(alpha = alpha).toArgb()
    val shadowColorLight = lightColor.copy(alpha = alpha).toArgb()

    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        
        // Sombra oscura (Abajo-Derecha)
        frameworkPaint.color = Color.Transparent.toArgb()
        frameworkPaint.setShadowLayer(blur.toPx(), offset.toPx(), offset.toPx(), shadowColorDark)
        canvas.drawOutline(shape.createOutline(size, layoutDirection, this), paint)

        // Sombra clara (Arriba-Izquierda)
        frameworkPaint.setShadowLayer(blur.toPx(), -offset.toPx(), -offset.toPx(), shadowColorLight)
        canvas.drawOutline(shape.createOutline(size, layoutDirection, this), paint)
    }
}

@Composable
fun DailyMoodModal(
    onDismiss: () -> Unit,
    onMoodSelected: (MoodOption) -> Unit,
    currentMood: String = "😊",
    nombrePareja: String = "Leo",
    estadoPareja: String = "Great",
    haceCuantoPareja: String = "2 hours ago"
) {
    val colorBackground = Color(0xFFFFF8EF)
    val colorPrimary = Color(0xFF78555E)
    val colorPrimaryContainer = Color(0xFFFFD1DC)
    val colorOnSurface = Color(0xFF1E1B14)
    val colorOnSurfaceVariant = Color(0xFF4F4446)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(32.dp),
                color = colorBackground,
                shadowElevation = 8.dp
            ) {
                Box(modifier = Modifier.padding(24.dp)) {
                    // Botón Cerrar simple
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colorOnSurfaceVariant.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Cerrar",
                            modifier = Modifier.size(20.dp),
                            tint = colorOnSurfaceVariant
                        )
                    }

                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Daily Mood",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorOnSurface
                            )
                            Text(
                                "History",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorPrimary,
                                modifier = Modifier
                                    .padding(end = 40.dp)
                                    .clickable { }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Card de Selección
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = colorBackground),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "How are you feeling today?",
                                    fontSize = 16.sp,
                                    color = colorOnSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Box(modifier = Modifier.height(300.dp)) {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(5),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(24.dp),
                                    ) {
                                        items(MOOD_OPTIONS) { mood ->
                                            MoodBubbleItem(
                                                mood = mood,
                                                isSelected = mood.emoji == currentMood,
                                                onClick = { onMoodSelected(mood) }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                HorizontalDivider(color = colorOnSurfaceVariant.copy(alpha = 0.1f))
                                
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "$nombrePareja felt \"$estadoPareja\" $haceCuantoPareja",
                                    fontSize = 12.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = colorOnSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MoodBubbleItem(
    mood: MoodOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorBackground = Color(0xFFFFF8EF)
    val colorPrimary = Color(0xFF78555E)
    val colorPrimaryContainer = Color(0xFFFFD1DC)
    val colorOnSurfaceVariant = Color(0xFF4F4446)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, label = "Scale")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(if (isSelected) 56.dp else 48.dp)
                .neumorphicShadow(
                    shape = CircleShape,
                    offset = if (isSelected) 0.dp else 4.dp,
                    blur = if (isSelected) 8.dp else 8.dp,
                    alpha = if (isPressed) 0.5f else 1f
                )
                .then(
                    if (isSelected) {
                        Modifier.border(2.dp, colorPrimaryContainer.copy(alpha = 0.5f), CircleShape)
                    } else Modifier
                )
                .clip(CircleShape)
                .background(colorBackground),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = mood.emoji,
                fontSize = if (isSelected) 30.sp else 24.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = mood.label,
            fontSize = if (isSelected) 11.sp else 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) colorPrimary else colorOnSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp
        )
    }
}
