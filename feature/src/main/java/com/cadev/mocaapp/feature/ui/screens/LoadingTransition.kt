package com.cadev.mocaapp.feature.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.ui.animations.CorazonesOrbitando

/**
 * PANTALLA DE CARGA / TRANSICIÓN
 * Fiel al diseño Zen proporcionado en HTML, mejorado con bordes animados.
 */
@Composable
fun LoadingTransition() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Diseño limpio sin manchas ni sombras de fondo
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp),
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Contenedor Circular Animado
            Box(
                modifier = Modifier.size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                // Borde Circular Animado (Carga)
                BordeCargaCircular()
                
                // Corazones centrados
                CorazonesOrbitando(modifier = Modifier.size(280.dp))
            }

            // Texto de Carga con pulso suave
            LoadingText(texto = "Un momento...")

            // Indicador de progreso minimalista
            ZenProgressBar()
        }
    }
}

@Composable
private fun BordeCargaCircular() {
    val colorEsquema = MaterialTheme.colorScheme
    val infiniteTransition = rememberInfiniteTransition(label = "bordeCarga")
    val rotacion by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotacionBorde"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val grosor = 3.dp.toPx()
        val primaryColor = colorEsquema.primary
        
        // Círculo de fondo (estático y suave)
        drawCircle(
            color = primaryColor.copy(alpha = 0.05f),
            style = Stroke(width = grosor)
        )

        // Arco de carga (animado)
        drawArc(
            color = primaryColor.copy(alpha = 0.3f),
            startAngle = rotacion,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = grosor, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun LoadingText(texto: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsoTexto")
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alphaPulso"
    )

    Text(
        text = texto,
        fontSize = 18.sp,
        fontFamily = FontFamily.Serif,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
        modifier = Modifier.graphicsLayer {
            translationY = (1f - alpha) * -2f
        }
    )
}

@Composable
private fun ZenProgressBar() {
    val infiniteTransition = rememberInfiniteTransition(label = "progresoZen")
    
    val desplazamiento by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "desplazamientoCarga"
    )

    Box(
        modifier = Modifier
            .width(64.dp)
            .height(2.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), CircleShape)
            .clip(CircleShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.33f)
                .align(Alignment.CenterStart)
                .graphicsLayer {
                    translationX = desplazamiento * 64.dp.toPx()
                }
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape)
        )
    }
}
