package com.cadev.mocaapp.feature.ui.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * ANIMACIÓN DE CORAZONES ORBITANDO (CORREGIDA Y CENTRADA)
 */
@Composable
fun CorazonesOrbitando(
    modifier: Modifier = Modifier
) {
    val rosa = Color(0xFFFFD1DC)
    val azul = Color(0xFFB3E5FC)
    
    val infiniteTransition = rememberInfiniteTransition(label = "orbita")
    val angulo by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angulo"
    )

    // El path del corazón se define una sola vez
    val pathCorazon = remember {
        Path().apply {
            moveTo(0f, 15f)
            cubicTo(-10f, 5f, -20f, 15f, -20f, 25f)
            cubicTo(-20f, 35f, 0f, 50f, 0f, 50f)
            cubicTo(0f, 50f, 20f, 35f, 20f, 25f)
            cubicTo(20f, 15f, 10f, 5f, 0f, 15f)
            close()
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radioOrbita = 25.dp.toPx()
        val escala = 3.2f // Más grandes como pidió el usuario

        // Corazón 1: Rosa
        dibujarCorazonIndividual(this, pathCorazon, angulo, cx, cy, radioOrbita, escala, rosa)
        
        // Corazón 2: Azul (Opuesto)
        dibujarCorazonIndividual(this, pathCorazon, angulo + 180f, cx, cy, radioOrbita, escala, azul)
    }
}

private fun dibujarCorazonIndividual(
    scope: DrawScope,
    path: Path,
    grados: Float,
    cx: Float,
    cy: Float,
    radio: Float,
    escala: Float,
    color: Color
) {
    val rad = grados * (PI.toFloat() / 180f)
    val x = cx + radio * cos(rad)
    val y = cy + radio * sin(rad)
    
    scope.withTransform({
        translate(x, y)
        scale(escala, escala, pivot = Offset.Zero)
        // Compensación para centrar el path (y va de 5 a 50, centro ~27.5)
        translate(0f, -27.5f)
    }) {
        scope.drawPath(path, color)
    }
}
