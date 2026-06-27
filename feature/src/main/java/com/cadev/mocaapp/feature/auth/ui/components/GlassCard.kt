package com.cadev.mocaapp.feature.auth.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cadev.mocaapp.feature.auth.ui.utils.glassmorphism

/**
 * TARJETA CON EFECTO CRISTAL (GLASSMORPHISM)
 * 
 * Qué hace:
 * Crea un contenedor redondeado con fondo translúcido y borde brillante.
 */
@Composable
fun TarjetaCristal(
    modificador: Modifier = Modifier,
    contenido: @Composable BoxScope.() -> Unit
) {
    val forma = RoundedCornerShape(32.dp)
    
    Box(
        modifier = modificador
            .glassmorphism(shape = forma, alpha = 0.5f)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.8f),
                shape = forma
            )
            .padding(28.dp)
    ) {
        contenido()
    }
}
