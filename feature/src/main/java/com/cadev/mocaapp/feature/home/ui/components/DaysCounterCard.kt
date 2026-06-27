package com.cadev.mocaapp.feature.home.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun DaysCounterCard(
    diasJuntos: Int,
    modifier: Modifier = Modifier
) {
    // Animación del contador de números
    var countToAnimate by remember { mutableIntStateOf(0) }
    val animatedCount by animateIntAsState(
        targetValue = countToAnimate,
        animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing),
        label = "CounterAnimation"
    )

    LaunchedEffect(diasJuntos) {
        delay(300) // Pequeña espera antes de empezar
        countToAnimate = diasJuntos
    }

    // Animación de latido del corazón
    val infiniteTransition = rememberInfiniteTransition(label = "HeartbeatTransition")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1.3f,
        targetValue = 1.3f, // El valor base es 1.3f
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                1.3f at 0 with LinearOutSlowInEasing
                1.55f at 200 // Primer pulso
                1.3f at 400
                1.55f at 600 // Segundo pulso
                1.3f at 800
                1.3f at 2000 // Pausa
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "HeartScale"
    )

    val colorOnSurfaceVariant = Color(0xFF4F4446)
    val colorError = Color(0xFFBA1A1A)
    
    // Gradiente del fondo
    val backgroundBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFD1DC), Color(0xFFFFF0E0))
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color(0xFFE0D9CE),
                ambientColor = Color.White
            ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(
            modifier = Modifier
                .background(backgroundBrush)
                .padding(vertical = 32.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Corazón animado en la esquina superior derecha
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = colorError,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 0.dp, y = (-8).dp) // Ajuste para posición absoluta en HTML
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
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = animatedCount.toString(),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    lineHeight = 72.sp,
                    letterSpacing = (-2).sp
                )

                Text(
                    text = "días",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorOnSurfaceVariant,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun DaysCounterCardPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        DaysCounterCard(diasJuntos = 1248)
    }
}
