package com.cadev.mocaapp.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.ui.theme.MocaPrimary

@Composable
fun MoodAndNoteRow(
    miEmoji: String,
    parejaEmoji: String,
    notaPareja: String,
    nombrePareja: String,
    onMoodClick: () -> Unit,
    onNoteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme
    
    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else MocaPrimary
    val colorOnSurface = if (isDark) Color.White else Color(0xFF1E1B14)
    val colorOnSurfaceVariant = if (isDark) Color(0xFFD3C3C5) else Color(0xFF4F4446)
    
    // Tonos ajustados para modo oscuro
    val colorMoodBackground = if (isDark) Color(0xFF343B2D) else Color(0xFFFFF1E1) 
    val colorNoteBackground = if (isDark) Color(0xFF4D4926) else Color(0xFFFFF9C4)
    val shadowColor = if (isDark) Color.Black.copy(alpha = 0.5f) else Color(0xFFE0D9CE).copy(alpha = 0.5f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card de Mood (Lado izquierdo)
        Box(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .shadowWithBlur(offset = 8.dp, blur = 16.dp, color = shadowColor)
                .clip(RoundedCornerShape(24.dp))
                .background(colorMoodBackground)
                .clickable { onMoodClick() }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Estado de ánimo",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorOnSurfaceVariant,
                    letterSpacing = 0.7.sp
                )
                
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Mi Emoji (Burbuja de cristal con difuminado)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadowWithBlur(4.dp, blur = 12.dp, color = if (isDark) Color.Black.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.3f))
                            .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.4f), CircleShape)
                            .border(1.5.dp, Color.White.copy(alpha = if (isDark) 0.2f else 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = miEmoji, fontSize = 32.sp)
                    }

                    // Emoji Pareja (Más pequeño)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadowWithBlur(4.dp, blur = 12.dp, color = if (isDark) Color.Black.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.3f))
                            .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.4f), CircleShape)
                            .border(1.5.dp, Color.White.copy(alpha = if (isDark) 0.2f else 0.6f), CircleShape)
                            .padding(bottom = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = parejaEmoji, fontSize = 20.sp)
                    }
                }
            }
        }

        // Card de Nota (Lado derecho)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .shadowWithBlur(offset = 8.dp, blur = 16.dp, color = shadowColor)
                .clip(RoundedCornerShape(24.dp))
                .background(colorNoteBackground)
                .clickable { onNoteClick() }
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = null,
                tint = colorPrimary,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (-8).dp)
            )

            Column(
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(
                    text = "$nombrePareja dice:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Italic,
                    color = colorOnSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Text(
                    text = notaPareja,
                    fontSize = 15.sp,
                    color = colorOnSurface,
                    lineHeight = 20.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Función auxiliar para sombras suaves y difuminadas
 */
fun Modifier.shadowWithBlur(
    offset: androidx.compose.ui.unit.Dp = 4.dp,
    blur: androidx.compose.ui.unit.Dp = 12.dp,
    color: Color = Color.Black.copy(alpha = 0.05f)
) = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = Color.Transparent.toArgb()
        frameworkPaint.setShadowLayer(blur.toPx(), 0f, offset.toPx(), color.toArgb())
        canvas.drawOutline(CircleShape.createOutline(size, layoutDirection, this), paint)
    }
}
