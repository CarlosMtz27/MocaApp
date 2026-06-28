package com.cadev.mocaapp.feature.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * COMPONENTE HEART TOGGLE FIEL AL HTML
 * Un interruptor romántico con corazones que laten y brillan.
 */
@Composable
fun HeartToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    // Animación de la posición de los corazones
    val heartOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 8.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy),
        label = "heartOffset"
    )

    // Animación de colores
    val trackBgStart by animateColorAsState(if (checked) Color(0xFFFFE7F1) else Color(0xFFFFFFFF), label = "bgStart")
    val trackBgEnd by animateColorAsState(if (checked) Color(0xFFFFD3E7) else Color(0xFFFDEFF5), label = "bgEnd")
    val heartColor by animateColorAsState(if (checked) Color(0xFFFF82B3) else Color(0xFFF7C8D7), label = "heartColor")
    
    // Animación de Latido (Beat)
    val infiniteTransition = rememberInfiniteTransition(label = "heartBeat")
    val beatScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 800
                1.0f at 0
                1.25f at 144 // 18%
                0.95f at 280 // 35%
                1.18f at 440 // 55%
                1.0f at 800
            }
        ),
        label = "scale"
    )

    // Animación de Brillo (Glow)
    val glowAlpha by animateFloatAsState(if (checked) 1f else 0f, tween(400), label = "glowAlpha")
    val glowPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glowPulse"
    )

    Box(
        modifier = modifier
            .width(80.dp)
            .height(40.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(trackBgStart, trackBgEnd)))
            .border(1.dp, Color(0xFFF5DCE6), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Efecto Glow
        if (checked) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scale(glowPulseScale)
                    .alpha(glowAlpha * (1f - (glowPulseScale - 0.7f) / 0.8f))
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFA0C3).copy(alpha = 0.45f), Color.Transparent)
                        ),
                        CircleShape
                    )
            )
        }

        // Corazones
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Corazón Izquierdo
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = heartColor,
                modifier = Modifier
                    .size(20.dp)
                    .offset(x = heartOffset)
                    .scale(if (checked) beatScale else 1f)
            )

            // Corazón Derecho
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = heartColor,
                modifier = Modifier
                    .size(20.dp)
                    .offset(x = -heartOffset)
                    .scale(if (checked) beatScale else 1f)
            )
        }
    }
}
