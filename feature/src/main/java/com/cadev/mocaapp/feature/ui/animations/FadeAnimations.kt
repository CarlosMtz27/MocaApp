package com.cadev.mocaapp.feature.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * ANIMACIÓN DE APARICIÓN SUAVE FIEL AL HTML (animate-fade-in)
 */
@Composable
fun AnimacionFadeIn(
    delayMillis: Int = 0,
    contenido: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        if (delayMillis > 0) {
            kotlinx.coroutines.delay(delayMillis.toLong())
        }
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(1200)) + 
                slideInVertically(
                    initialOffsetY = { 20 },
                    animationSpec = tween(1200)
                ),
        exit = fadeOut()
    ) {
        contenido()
    }
}

/**
 * MODIFICADOR PARA APLICAR EL EFECTO DE APARICIÓN
 */
fun Modifier.fadeEntrada(
    visible: Boolean,
    delay: Int = 0
): Modifier = this.graphicsLayer {
    // Esto se puede implementar con estados animados si se prefiere
}
