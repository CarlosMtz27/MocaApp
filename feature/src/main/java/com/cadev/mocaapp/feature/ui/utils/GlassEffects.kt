package com.cadev.mocaapp.feature.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * FONDO MESH GRADIENT FIEL AL HTML (ADAPTADO A TEMAS)
 * Recrea los gradientes radiales en las esquinas usando el esquema de colores actual.
 */
@Composable
fun FondoMeshMoca(content: @Composable () -> Unit) {
    val colorEsquema = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorEsquema.background)
            .drawBehind {
                // Gradiente superior derecha
                dibujarGradienteRadial(
                    centro = Offset(size.width, 0f),
                    radio = size.width * 0.8f,
                    color = colorEsquema.primaryContainer.copy(alpha = 0.4f)
                )

                // Gradiente inferior izquierda
                dibujarGradienteRadial(
                    centro = Offset(0f, size.height),
                    radio = size.width * 0.8f,
                    color = colorEsquema.primaryContainer.copy(alpha = 0.4f)
                )
            }
    ) {
        content()
    }
}

private fun DrawScope.dibujarGradienteRadial(
    centro: Offset,
    radio: Float,
    color: Color
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, Color.Transparent),
            center = centro,
            radius = radio
        ),
        center = centro,
        radius = radio
    )
}
