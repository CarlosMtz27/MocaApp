package com.cadev.mocaapp.feature.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * COMPONENTE DE TARJETA CON EFECTO GLASSMORPHISM Y REBOTE
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    bordeRedondeado: Dp = 32.dp,
    colorFondo: Color = Color.White.copy(alpha = 0.4f),
    colorBorde: Color = Color.White.copy(alpha = 0.2f),
    alHacerClick: (() -> Unit)? = null,
    contenido: @Composable BoxScope.() -> Unit
) {
    val fuenteInteraccion = remember { MutableInteractionSource() }
    val estaPresionado by fuenteInteraccion.collectIsPressedAsState()
    
    // Animación con rebote (Spring) para simular el .card-bounce del CSS
    val escala by animateFloatAsState(
        targetValue = if (estaPresionado) 0.96f else 1.0f,
        animationSpec = if (estaPresionado) {
            tween(100)
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "escalaRebote"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
            }
            .clip(RoundedCornerShape(bordeRedondeado))
            .background(colorFondo)
            .border(1.dp, colorBorde, RoundedCornerShape(bordeRedondeado))
            .then(
                if (alHacerClick != null) {
                    Modifier.clickable(
                        interactionSource = fuenteInteraccion,
                        indication = null,
                        onClick = alHacerClick
                    )
                } else Modifier
            ),
        content = contenido
    )
}
