package com.cadev.mocaapp.feature.cuestionarios.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.core.ui.meshGradientBackground
import com.cadev.mocaapp.feature.ui.theme.*

@Composable
fun ExitoCuestionario(
    onVerResumen: () -> Unit,
    onRegresarInicio: () -> Unit
) {
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme
    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else MocaPrimary
    val colorOnSurface = if (isDark) Color(0xFFE7BBC6) else MocaOnSurface
    val colorOnSurfaceVariant = if (isDark) Color(0xFFD3C3C5) else MocaOnSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .meshGradientBackground()
    ) {
        // Elementos de fondo decorativos
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-100).dp)
                .size(300.dp)
                .background(MocaPrimaryContainer.copy(alpha = if (isDark) 0.1f else 0.4f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .size(300.dp)
                .background(MocaTertiaryContainer.copy(alpha = if (isDark) 0.1f else 0.4f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono de éxito animado
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .padding(bottom = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "exito_anim")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "ripple"
                )

                // Ripple Effect
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MocaTertiaryContainer.copy(alpha = (if (isDark) 0.1f else 0.3f) * (2f - scale)))
                        .padding(12.dp)
                )

                // Círculo Central
                Surface(
                    modifier = Modifier.size(96.dp),
                    shape = CircleShape,
                    color = MocaTertiary,
                    shadowElevation = if (isDark) 0.dp else 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            Text(
                text = "¡Muchas gracias!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colorPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tu respuesta se ha enviado correctamente. Apreciamos mucho tu tiempo y sinceridad.",
                fontSize = 18.sp,
                color = colorOnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 300.dp),
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Botones de acción
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onVerResumen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(elevation = if (isDark) 0.dp else 8.dp, shape = CircleShape, spotColor = MocaPrimary.copy(alpha = 0.2f)),
                    colors = ButtonDefaults.buttonColors(containerColor = colorPrimary),
                    shape = CircleShape
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(Icons.Default.Description, null, tint = if (isDark) Color.Black else Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Ver Resumen", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.Black else Color.White)
                    }
                }

                OutlinedButton(
                    onClick = onRegresarInicio,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MocaOutline.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colorOnSurface)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(Icons.Default.Home, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Regresar al Inicio", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
