package com.cadev.mocaapp.feature.auth.ui.utils

import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cadev.mocaapp.core.ui.ZenGlassBorder
import com.cadev.mocaapp.core.ui.ZenGlassSurface

/**
 * MODIFICADOR DE CRISTAL (GLASSMORPHISM)
 * 
 * Qué hace:
 * Aplica un fondo translúcido con desenfoque y un borde sutil que simula cristal.
 * Nota: El desenfoque (blur) funciona en Android 12+ (API 31+). 
 * En versiones anteriores se verá translúcido pero sin desenfoque.
 */
fun Modifier.glassmorphism(
    shape: Shape,
    alpha: Float = 0.45f,
    blurRadius: Dp = 0.dp
): Modifier = this
    .clip(shape)
    .blur(blurRadius)
    .drawBehind {
        drawRect(
            color = ZenGlassSurface.copy(alpha = alpha)
        )
    }
    .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
            colors = listOf(
                ZenGlassBorder.copy(alpha = 0.3f),
                ZenGlassBorder.copy(alpha = 0.1f)
            )
        ),
        shape = shape
    )
