package com.cadev.mocaapp.feature.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cadev.mocaapp.feature.diario.domain.model.Comentario
import com.cadev.mocaapp.feature.ui.theme.*
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * COMPONENTE DE COMENTARIO MODERNO FIEL AL HTML
 * Adaptado para soportar Modo Oscuro.
 */
@Composable
fun ModernCommentItem(
    comentario: Comentario,
    index: Int
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(index * 80L) 
        visible = true
    }

    val tiempoHace = remember(comentario.creadoEn) {
        val date = comentario.creadoEn.toDate()
        val diff = Date().time - date.time
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        when {
            hours < 1 -> "Ahora mismo"
            hours < 24 -> "Hace $hours h"
            else -> SimpleDateFormat("d 'de' MMM", Locale.forLanguageTag("es-MX")).format(date)
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.8f,
            animationSpec = spring(dampingRatio = 0.64f, stiffness = Spring.StiffnessLow)
        ) + fadeIn() + slideInVertically { 20 },
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp) 
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (comentario.fotoUsuario.isNotBlank()) {
                        AsyncImage(
                            model = comentario.fotoUsuario,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = comentario.nombreUsuario.take(1).uppercase(),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = comentario.nombreUsuario,
                            style = OrganicTypography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = tiempoHace,
                            style = TextStyle(fontSize = 10.sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = comentario.texto,
                        style = OrganicTypography.bodyMedium.copy(lineHeight = 18.sp, fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * BARRA DE ENTRADA MODERNA CON ANIMACIÓN
 */
@Composable
fun ModernCommentInput(
    nuevoComentario: String,
    fotoUsuario: String,
    onTextoChange: (String) -> Unit,
    onEnviar: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                if (fotoUsuario.isNotBlank()) {
                    AsyncImage(
                        model = fotoUsuario,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Campo de texto
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (nuevoComentario.isEmpty()) {
                    Text(
                        text = "Escribe un comentario...",
                        style = OrganicTypography.bodyMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                
                BasicTextField(
                    value = nuevoComentario,
                    onValueChange = onTextoChange,
                    textStyle = OrganicTypography.bodyMedium.copy(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { it() }
                )
            }
            
            // Botón Envío
            val isActive = nuevoComentario.isNotBlank()
            val buttonColor by animateColorAsState(if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            val contentColor by animateColorAsState(if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            
            IconButton(
                onClick = onEnviar,
                enabled = isActive,
                modifier = Modifier
                    .size(40.dp)
                    .background(buttonColor, CircleShape)
                    .shadow(if (isActive) 4.dp else 0.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
