package com.cadev.mocaapp.core.ui

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modificador para el fondo Mesh Gradient del diseño de Stitch
 * Versión adaptable a modo claro y oscuro
 */
fun Modifier.meshGradientBackground(isDark: Boolean = false) = this.drawBehind {
    val canvasWidth = size.width
    val canvasHeight = size.height
    
    // Color de fondo base
    drawRect(color = if (isDark) Color(0xFF000000) else Color(0xFFFFFFFF))
    
    val gradients = if (isDark) {
        listOf(
            Triple(Offset(0f, 0f), Color(0xFF2D141C), 0.8f),
            Triple(Offset(canvasWidth, 0f), Color(0xFF0C200E), 0.7f),
            Triple(Offset(canvasWidth * 0.9f, canvasHeight * 0.4f), Color(0xFF1F1C04), 0.6f),
            Triple(Offset(canvasWidth, canvasHeight), Color(0xFF2D141C).copy(alpha = 0.5f), 0.9f),
            Triple(Offset(0f, canvasHeight), Color(0xFF0C200E).copy(alpha = 0.4f), 0.8f)
        )
    } else {
        listOf(
            Triple(Offset(0f, 0f), Color(0xFFFFE5EC), 0.8f),
            Triple(Offset(canvasWidth, 0f), Color(0xFFE8F5E9), 0.7f),
            Triple(Offset(canvasWidth * 0.9f, canvasHeight * 0.4f), Color(0xFFFFF0E0), 0.6f),
            Triple(Offset(canvasWidth, canvasHeight), Color(0xFFFFD1DC).copy(alpha = 0.5f), 0.9f),
            Triple(Offset(0f, canvasHeight), Color(0xFFD1E9CD).copy(alpha = 0.4f), 0.8f)
        )
    }

    gradients.forEach { (offset, color, radiusScale) ->
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color, Color.Transparent),
                center = offset,
                radius = canvasWidth * radiusScale
            ),
            center = offset,
            radius = canvasWidth * radiusScale
        )
    }
}

/**
 * Estilo Neumórfico "Plano" mejorado con doble sombra (Luz y Sombra)
 * Versión más refinada para fondos claros
 */
fun Modifier.neuFlat(
    shape: Shape,
    elevation: Dp = 8.dp,
    containerColor: Color = Color.White
) = this.drawBehind {
    val shadowColor = Color(0xFF78555E).copy(alpha = 0.06f).toArgb()
    val lightColor = Color.White.toArgb()
    val blurRadius = elevation.toPx()

    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        val outline = shape.createOutline(size, layoutDirection, this)
        
        // Sombra Oscura (Efecto profundidad suave)
        frameworkPaint.color = shadowColor
        frameworkPaint.setShadowLayer(blurRadius, blurRadius / 1.5f, blurRadius / 1.5f, shadowColor)
        canvas.drawOutline(outline, paint)

        // Reflejo de Luz (Borde brillante)
        frameworkPaint.color = lightColor
        frameworkPaint.setShadowLayer(blurRadius / 2, -blurRadius / 3, -blurRadius / 3, lightColor)
        canvas.drawOutline(outline, paint)
    }
}.background(containerColor, shape)

/**
 * Estilo Neumórfico "Presionado" mejorado (Efecto Inset)
 */
fun Modifier.neuPressed(
    shape: Shape,
    containerColor: Color = Color(0xFFFFF8EF)
) = this.drawBehind {
    val shadowColor = Color(0xFF78555E).copy(alpha = 0.12f).toArgb()
    val lightColor = Color.White.toArgb()
    val blurRadius = 8.dp.toPx()

    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        val outline = shape.createOutline(size, layoutDirection, this)

        // Sombra Inset Oscura
        frameworkPaint.color = shadowColor
        frameworkPaint.setShadowLayer(blurRadius, blurRadius / 4, blurRadius / 4, shadowColor)
        canvas.drawOutline(outline, paint)
        
        // Sombra Inset Clara
        frameworkPaint.color = lightColor
        frameworkPaint.setShadowLayer(blurRadius, -blurRadius / 4, -blurRadius / 4, lightColor)
        canvas.drawOutline(outline, paint)
    }
}.background(containerColor, shape)
