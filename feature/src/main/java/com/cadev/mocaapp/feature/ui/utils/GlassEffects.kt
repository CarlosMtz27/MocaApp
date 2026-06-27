package com.cadev.mocaapp.feature.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.cadev.mocaapp.feature.ui.theme.*

/**
 * FONDO MESH GRADIENT FIEL AL HTML
 * Recrea los gradientes radiales en las esquinas.
 */
@Composable
fun FondoMeshMoca(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MocaBackground)
            .drawBehind {
                // Gradiente superior izquierda (#fff8ef) - Ya es el color de fondo base
                
                // Gradiente superior derecha (#ffd1dc)
                dibujarGradienteRadial(
                    centro = Offset(size.width, 0f),
                    radio = size.width * 0.8f,
                    color = MocaPrimaryContainer.copy(alpha = 0.6f)
                )

                // Gradiente inferior derecha (#fff8ef)
                
                // Gradiente inferior izquierda (#ffd1dc)
                dibujarGradienteRadial(
                    centro = Offset(0f, size.height),
                    radio = size.width * 0.8f,
                    color = MocaPrimaryContainer.copy(alpha = 0.6f)
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
