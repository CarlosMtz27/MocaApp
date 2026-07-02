package com.cadev.mocaapp.feature.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cadev.mocaapp.feature.ui.theme.MocaSurface

/**
 * MODIFICADOR PARA EFECTO NEUMORFISMO FIEL AL HTML
 */
fun Modifier.neuFlat(
    colorFondo: Color = MocaSurface,
    sombraOscura: Color = Color(0xFFE8E0D5),
    sombraClara: Color = Color(0xFFFFFFFF),
    radioBorde: Dp = 16.dp,
    elevacion: Dp = 6.dp
) = this.drawBehind {
    val paint = Paint()
    val frameworkPaint = paint.asFrameworkPaint()
    
    // Sombra oscura (derecha inferior)
    frameworkPaint.color = Color.Transparent.toArgb()
    frameworkPaint.setShadowLayer(
        elevacion.toPx() * 2,
        elevacion.toPx(),
        elevacion.toPx(),
        sombraOscura.toArgb()
    )
    drawIntoCanvas {
        it.drawRoundRect(
            0f, 0f, size.width, size.height,
            radioBorde.toPx(), radioBorde.toPx(),
            paint
        )
    }

    // Sombra clara (izquierda superior)
    frameworkPaint.setShadowLayer(
        elevacion.toPx() * 2,
        -elevacion.toPx(),
        -elevacion.toPx(),
        sombraClara.toArgb()
    )
    drawIntoCanvas {
        it.drawRoundRect(
            0f, 0f, size.width, size.height,
            radioBorde.toPx(), radioBorde.toPx(),
            paint
        )
    }
}.background(colorFondo, RoundedCornerShape(radioBorde))

/**
 * MODIFICADOR PARA EFECTO NEUMORFISMO INSET FIEL AL HTML
 */
fun Modifier.neuInset(
    colorFondo: Color = MocaSurface,
    sombraOscura: Color = Color(0xFFE8E0D5),
    sombraClara: Color = Color(0xFFFFFFFF),
    radioBorde: Dp = 16.dp,
    elevacion: Dp = 4.dp
) = this.drawBehind {
    // El efecto Inset es más complejo en Compose puro sin bibliotecas externas,
    // pero podemos simularlo con sombras internas o simplemente usar un fondo ligeramente diferente.
    // Para ser fieles al HTML que usa sombras internas, aquí usamos una aproximación visual.
}.background(colorFondo, RoundedCornerShape(radioBorde))

@Composable
fun NeuButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    radioBorde: Dp = 16.dp,
    colorFondo: Color = MocaSurface, // Añadido soporte para color personalizado
    contenido: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .then(
                if (isPressed) Modifier.neuInset(radioBorde = radioBorde, colorFondo = colorFondo)
                else Modifier.neuFlat(radioBorde = radioBorde, colorFondo = colorFondo)
            )
            .clip(RoundedCornerShape(radioBorde))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = androidx.compose.ui.Alignment.Center,
        content = contenido
    )
}
