package com.cadev.mocaapp.feature.auth.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cadev.mocaapp.feature.R

/**
 * COMPONENTE DEL LOGO CON ANIMACIÓN
 * 
 * Qué hace:
 * Muestra el icono de los dos corazones (ic_corazon) y le aplica una animación
 * de "latido" o pulsación constante.
 * 
 * Cómo lo podemos modificar:
 * Si queremos que lata más rápido, bajamos el número `1000` en `tween`.
 * Si queremos que crezca más, subimos el `1.2f` en `targetValue`.
 */
@Composable
fun HeartLogo(modifier: Modifier = Modifier, size: Dp = 80.dp) {
    // Creamos una transición infinita (que no para nunca)
    val infiniteTransition = rememberInfiniteTransition(label = "latido_corazon")
    
    // Animamos el tamaño de 1.0 (normal) a 1.2 (un poco más grande)
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escala"
    )

    Icon(
        painter = painterResource(id = R.drawable.ic_corazon),
        contentDescription = "Logo palpitando",
        tint = Color.Unspecified, // Usamos los colores originales del XML
        modifier = modifier
            .size(size)
            .graphicsLayer {
                // Aplicamos el crecimiento a lo ancho y a lo alto
                scaleX = scale
                scaleY = scale
            }
    )
}
