package com.cadev.mocaapp.feature.cuestionarios.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.cuestionarios.domain.model.CategoriaCuestionario
import com.cadev.mocaapp.feature.cuestionarios.domain.model.Cuestionario
import com.cadev.mocaapp.feature.cuestionarios.domain.model.EstadoCuestionario
import com.cadev.mocaapp.feature.ui.theme.*

@Composable
fun TarjetaCuestionarioMejorada(
    cuestionario: Cuestionario,
    estado: EstadoCuestionario,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme
    
    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else MocaPrimary
    val colorOnSurface = if (isDark) Color(0xFFFFFFFF) else MocaOnSurface
    val colorOnSurfaceVariant = if (isDark) Color(0xFFD3C3C5) else MocaOnSurfaceVariant
    val colorCardBackground = if (isDark) Color(0xFF2D2921) else MocaSurfaceContainerLowest
    val colorBadgeBackground = if (isDark) Color(0xFF5E3E47) else MocaPrimaryContainer.copy(alpha = 0.4f)
    val colorBadgeText = if (isDark) Color.White else MocaOnPrimaryContainer

    val categoria = try {
        CategoriaCuestionario.valueOf(cuestionario.categoria)
    } catch (e: Exception) {
        CategoriaCuestionario.PERSONALIZADO
    }

    val colorEstadoBg = when (estado) {
        EstadoCuestionario.YO_RESPONDÍ -> if (isDark) Color(0xFF423710) else Color(0xFFFFECB3)
        EstadoCuestionario.PAREJA_RESPONDIÓ -> if (isDark) Color(0xFF1B3D1B) else Color(0xFFC8E6C9)
        else -> Color.Transparent
    }

    val colorEstadoTexto = when (estado) {
        EstadoCuestionario.YO_RESPONDÍ -> if (isDark) Color(0xFFFFD54F) else Color(0xFFBF360C)
        EstadoCuestionario.PAREJA_RESPONDIÓ -> if (isDark) Color(0xFF81C784) else Color(0xFF1B5E20)
        else -> colorPrimary
    }

    val colorIconoAccion = when (estado) {
        EstadoCuestionario.YO_RESPONDÍ -> Color(0xFFFF9800)
        EstadoCuestionario.PAREJA_RESPONDIÓ -> Color(0xFF4CAF50)
        EstadoCuestionario.AMBOS -> MocaAccentPink
        else -> if (isDark) MocaAccentPink else Color(0xFFFF4081)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(
                elevation = if (isDark) 0.dp else 6.dp, 
                shape = RoundedCornerShape(28.dp), 
                spotColor = MocaAccentPink.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colorCardBackground),
        border = if (isDark) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)) else null
    ) {
        Column {
            Row(
                modifier = Modifier.padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono de Categoría
                Surface(
                    modifier = Modifier.size(68.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = colorPrimary.copy(alpha = if (isDark) 0.15f else 0.25f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = categoria.icono,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = colorPrimary
                        )
                    }
                }

                // Info Central
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cuestionario.titulo,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorOnSurface
                    )
                    
                    if (cuestionario.etiquetas.isNotEmpty()) {
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            cuestionario.etiquetas.take(2).forEach { etiqueta ->
                                Text(
                                    text = "#$etiqueta",
                                    fontSize = 10.sp,
                                    color = MocaAccentPink,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = cuestionario.descripcion,
                        fontSize = 13.sp,
                        color = colorOnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Surface(
                        color = colorBadgeBackground,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${cuestionario.preguntas.size} preguntas",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = colorBadgeText
                        )
                    }
                }

                // Botón Acción
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = colorIconoAccion.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (estado == EstadoCuestionario.AMBOS) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                            tint = colorIconoAccion
                        )
                    }
                }
            }

            // Barra de Estado Inferior
            if (estado == EstadoCuestionario.YO_RESPONDÍ || estado == EstadoCuestionario.PAREJA_RESPONDIÓ) {
                val textoEstado = if (estado == EstadoCuestionario.YO_RESPONDÍ) "⏳ Esperando a tu pareja..." else "✨ ¡Tu pareja ya respondió! Te toca"
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = colorEstadoBg
                ) {
                    Text(
                        text = textoEstado,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorEstadoTexto
                    )
                }
            }
        }
    }
}
