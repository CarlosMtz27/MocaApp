package com.cadev.mocaapp.feature.eventos.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cadev.mocaapp.core.model.TipoEvento
import com.cadev.mocaapp.core.ui.*
import com.cadev.mocaapp.feature.R
import com.cadev.mocaapp.feature.eventos.domain.model.Evento
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventoCard(
    evento: Evento,
    pasado: Boolean = false,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme
    val tipo = try { TipoEvento.valueOf(evento.tipo) } catch (e: Exception) { TipoEvento.OTRO }
    
    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else StitchPrimary
    val colorOnSurface = if (isDark) Color(0xFFE7BBC6) else StitchOnSurface
    val colorOnSurfaceVariant = if (isDark) Color(0xFFD3C3C5) else StitchOnSurfaceVariant
    val colorCardBackground = if (isDark) Color(0xFF2D2921).copy(alpha = 0.8f) else Color.White
    val colorBorder = if (isDark) Color.White.copy(alpha = 0.1f) else StitchTertiaryFixedDim.copy(alpha = 0.5f)

    val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatoLegible = SimpleDateFormat("d 'de' MMM", Locale.forLanguageTag("es-MX"))
    val fechaLegible = try {
        formatoLegible.format(formatoEntrada.parse(evento.fecha)!!)
    } catch (e: Exception) { evento.fecha }

    val diasRestantes = try {
        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val eventTime = formatoEntrada.parse(evento.fecha)!!.time
        ((eventTime - hoy) / (1000 * 60 * 60 * 24)).toInt()
    } catch (e: Exception) { 0 }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .padding(horizontal = 4.dp)
            .scale(scale)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorCardBackground
            ),
            border = BorderStroke(1.dp, colorBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Fondo decorativo dinámico (Visible en ambas pestañas)
                if (evento.fotoUrl.isNotBlank()) {
                    AsyncImage(
                        model = evento.fotoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(if (isDark) 0.45f else 0.55f)
                            .blur(8.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.eventos),
                        contentDescription = null,
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(if (isDark) 0.75f else 0.75f)
                            .blur(8.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isDark) colorPrimary.copy(alpha = 0.2f) else StitchPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tipo.icono,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = colorPrimary
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = evento.titulo,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = colorOnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = colorOnSurfaceVariant
                            )
                            Text(
                                text = "$fechaLegible · ${evento.hora}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorOnSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = colorOnSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }

        if (!pasado) {
            val badgeText = when {
                diasRestantes == 0 -> "¡Hoy!"
                diasRestantes == 1 -> "Mañana"
                diasRestantes > 1 -> "En $diasRestantes días"
                else -> null
            }

            if (badgeText != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-12).dp),
                    shape = CircleShape,
                    color = if (diasRestantes == 0) (if (isDark) colorPrimary else StitchPrimaryContainer) else (if (isDark) Color(0xFF374C37) else StitchTertiaryContainer),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = if (isDark) 0.2f else 1f))
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (diasRestantes == 0) (if (isDark) Color.Black else StitchOnPrimaryContainer) else (if (isDark) Color(0xFFB5CDB2) else StitchOnTertiaryContainer),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
