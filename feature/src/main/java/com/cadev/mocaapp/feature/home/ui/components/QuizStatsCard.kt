package com.cadev.mocaapp.feature.home.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.ui.theme.MocaPrimary
import com.cadev.mocaapp.feature.ui.theme.MocaPrimaryContainer

@Composable
fun QuizStatsCard(
    completados: Int,
    coincidencias: Int,
    onVerDetalles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorPrimary = MocaPrimary
    val colorTertiary = Color(0xFF4F644E)
    val colorTertiaryContainer = Color(0xFFCBE3C7)
    val colorOnSurfaceVariant = Color(0xFF4F4446)
    val colorSurfaceBright = Color(0xFFFFF8EF)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(MocaPrimaryContainer, colorTertiaryContainer)
                )
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Formas decorativas de fondo
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .size(150.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                .blur(40.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-40.dp), y = 40.dp)
                .size(180.dp)
                .background(colorSurfaceBright.copy(alpha = 0.3f), CircleShape)
                .blur(30.dp)
        )

        // Capa de cristal principal
        Surface(
            color = Color.White.copy(alpha = 0.4f),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = colorPrimary.copy(alpha = 0.5f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                    )
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¿Cuánto se conocen?",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Su historia en números",
                            fontSize = 15.sp,
                            color = colorOnSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Grid de estadísticas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatItem(
                        icon = Icons.Default.Checklist,
                        value = completados.toString(),
                        label = "Completados",
                        iconBg = MocaPrimaryContainer,
                        iconTint = colorPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        icon = Icons.Default.Group,
                        value = coincidencias.toString(),
                        label = "Coincidencias",
                        iconBg = colorTertiaryContainer,
                        iconTint = colorTertiary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botón CTA
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "ButtonScale")

                Button(
                    onClick = onVerDetalles,
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .scale(scale),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(colorPrimary, Color(0xFFA47081))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Ver detalles",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 1.1f else 1f, label = "IconScale")

    Surface(
        color = Color(0xFFFFF8EF).copy(alpha = 0.8f),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .border(1.dp, Color(0xFFE9E2D6), RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scale(scale)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = iconTint
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4F4446),
                letterSpacing = 1.sp
            )
        }
    }
}
