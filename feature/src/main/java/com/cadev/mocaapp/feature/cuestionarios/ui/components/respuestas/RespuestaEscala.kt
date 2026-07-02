package com.cadev.mocaapp.feature.cuestionarios.ui.components.respuestas

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.ui.theme.*

@Composable
fun RespuestaEscala(
    valor: Int,
    onValorChanged: (Int) -> Unit
) {
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme
    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else MocaPrimary
    val colorOnSurface = if (isDark) Color(0xFFE7BBC6) else MocaOnSurface
    val colorOnSurfaceVariant = if (isDark) Color(0xFFD3C3C5) else MocaOnSurfaceVariant
    val colorChipUnselected = if (isDark) Color(0xFF333028) else MocaSurfaceContainerLow

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Scroll horizontal de Chips (1 a 10)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (i in 1..10) {
                val esSeleccionado = valor == i
                
                val scaleAnim by animateFloatAsState(
                    targetValue = if (esSeleccionado) 1.15f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "scale"
                )

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .scale(scaleAnim)
                        .clip(CircleShape)
                        .background(if (esSeleccionado) MocaAccentPink else colorChipUnselected)
                        .border(
                            width = if (esSeleccionado) 0.dp else 1.dp,
                            color = colorOnSurfaceVariant.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .clickable { onValorChanged(i) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = i.toString(),
                        fontSize = 18.sp,
                        fontWeight = if (esSeleccionado) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (esSeleccionado) Color.White else colorOnSurface
                    )
                }
            }
        }

        // Etiquetas de extremos
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text("Nada", fontSize = 12.sp, color = colorOnSurfaceVariant, fontWeight = FontWeight.Bold)
                Text("satisfecho", fontSize = 11.sp, color = colorOnSurfaceVariant.copy(alpha = 0.6f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Muy", fontSize = 12.sp, color = colorOnSurfaceVariant, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right)
                Text("seguro", fontSize = 11.sp, color = colorOnSurfaceVariant.copy(alpha = 0.6f), textAlign = TextAlign.Right)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AnimatedVisibility(
            visible = valor > 0,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = "Tu selección: $valor",
                style = MaterialTheme.typography.titleMedium,
                color = MocaAccentPink,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
