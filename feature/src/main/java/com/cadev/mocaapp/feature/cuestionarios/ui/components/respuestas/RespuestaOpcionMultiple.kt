package com.cadev.mocaapp.feature.cuestionarios.ui.components.respuestas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.ui.theme.*

@Composable
fun RespuestaOpcionMultiple(
    opciones: List<String>,
    respuestaSeleccionada: String,
    onRespuestaSelected: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme
    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else MocaPrimary
    val colorOnSurface = if (isDark) Color(0xFFE7BBC6) else MocaOnSurface
    val colorCardBg = if (isDark) Color(0xFF333028) else MocaSurfaceContainerLowest

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        opciones.forEach { opcion ->
            val esSeleccionada = respuestaSeleccionada == opcion
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (esSeleccionada) colorPrimary.copy(alpha = 0.1f) else colorCardBg)
                    .border(
                        width = if (esSeleccionada) 2.dp else 1.dp,
                        color = if (esSeleccionada) colorPrimary else MocaOutlineVariant.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onRespuestaSelected(opcion) }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = esSeleccionada,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = colorPrimary,
                            unselectedColor = MocaOutline
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = opcion,
                        fontSize = 16.sp,
                        color = if (esSeleccionada) colorPrimary else colorOnSurface,
                        fontWeight = if (esSeleccionada) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )

                    if (esSeleccionada) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colorPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
