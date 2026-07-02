package com.cadev.mocaapp.feature.home.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun DaysCounterCard(
    diasJuntos: Int,
    fechaRelacion: String? = null,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme

    // Animación del contador de números
    var countToAnimate by remember { mutableIntStateOf(0) }
    val animatedCount by animateIntAsState(
        targetValue = countToAnimate,
        animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing),
        label = "CounterAnimation"
    )

    LaunchedEffect(diasJuntos) {
        delay(300.milliseconds) 
        countToAnimate = diasJuntos
    }

    // Animación de latido del corazón
    val infiniteTransition = rememberInfiniteTransition(label = "HeartbeatTransition")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1.3f,
        targetValue = 1.3f, 
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                1.3f at 0
                1.55f at 200 
                1.3f at 400
                1.55f at 600 
                1.3f at 800
                1.3f at 2000 
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "HeartScale"
    )

    val colorOnSurfaceVariant = if (isDark) Color(0xFFD3C3C5) else Color(0xFF4F4446)
    val colorError = if (isDark) Color(0xFFFFB4AB) else Color(0xFFBA1A1A)
    val textColor = if (isDark) Color.White else Color.Black
    
    val backgroundBrush = if (isDark) {
        Brush.horizontalGradient(
            colors = listOf(Color(0xFF5E3E47), Color(0xFF4B472B))
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(Color(0xFFFFD1DC), Color(0xFFFFF0E0))
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = if (isDark) Color.Black else Color(0xFFE0D9CE),
                ambientColor = if (isDark) Color.Transparent else Color.White
            ),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(backgroundBrush)
                .padding(vertical = 24.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = colorError,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 0.dp, y = (-4).dp)
                    .scale(heartScale)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Llevan juntos",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorOnSurfaceVariant,
                    letterSpacing = 2.sp
                )

                Text(
                    text = animatedCount.toString(),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    lineHeight = 72.sp,
                    letterSpacing = (-2).sp
                )

                Text(
                    text = "días creando una historia increíble",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorOnSurfaceVariant,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (!fechaRelacion.isNullOrBlank()) {
                    Text(
                        text = "Juntos desde: $fechaRelacion",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = colorOnSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }
    }
}
