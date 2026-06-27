package com.cadev.mocaapp.feature.auth.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * DECORACIÓN DE FONDO ZEN
 */
@Composable
fun DecoracionFondoZen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopStart)
                .offset(x = (-150).dp, y = (-100).dp)
                .background(Brush.radialGradient(listOf(Color(0xFFFFDAB9).copy(alpha = 0.5f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 150.dp, y = 100.dp)
                .background(Brush.radialGradient(listOf(Color(0xFFFFD1DC).copy(alpha = 0.5f), Color.Transparent)))
        )
    }
}
