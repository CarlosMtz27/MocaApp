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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * COMPONENTE DE COMENTARIO MODERNO FIEL AL HTML
 * Incluye animación de entrada "Pop-in"
 */
@Composable
fun ModernCommentItem(
    comentario: Comentario,
    index: Int
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(index * 80L) // Delay escalonado como el HTML
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
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp) // Reducido vertical
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(12.dp)), // Reducido de 16.dp
            shape = RoundedCornerShape(12.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(10.dp), // Reducido de 12.dp
                horizontalArrangement = Arrangement.spacedBy(10.dp) 
            ) {
                // Avatar con Foto o Default
                Box(
                    modifier = Modifier
                        .size(36.dp) // Reducido de 40.dp
                        .clip(CircleShape)
                        .background(MocaPrimaryContainer.copy(alpha = 0.4f)), // Fondo rosa tenue por defecto
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
                            color = MocaAccentPink,
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
                            color = MocaOnSurface
                        )
                        Text(
                            text = tiempoHace,
                            style = TextStyle(fontSize = 10.sp, color = MocaOutline)
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = comentario.texto,
                        style = OrganicTypography.bodyMedium.copy(lineHeight = 18.sp, fontSize = 13.sp),
                        color = Color(0xFF374151)
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
        color = Color.White.copy(alpha = 0.9f),
        tonalElevation = 4.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar con Foto del Usuario o Default
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MocaPrimaryContainer.copy(alpha = 0.4f)), // Fondo rosa tenue
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
                        tint = MocaAccentPink, // Icono rosa
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Contenedor del Campo
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, MocaSurfaceVariant, CircleShape)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (nuevoComentario.isEmpty()) {
                    Text(
                        text = "Escribe un comentario...",
                        style = OrganicTypography.bodyMedium.copy(fontSize = 14.sp),
                        color = Color(0xFF9CA3AF)
                    )
                }
                
                BasicTextField(
                    value = nuevoComentario,
                    onValueChange = onTextoChange,
                    textStyle = OrganicTypography.bodyMedium.copy(fontSize = 14.sp, color = MocaOnSurface),
                    cursorBrush = SolidColor(MocaAccentPink),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { it() }
                )
            }
            
            // Botón de Envío Animado
            val isActive = nuevoComentario.isNotBlank()
            val buttonColor by animateColorAsState(if (isActive) MocaAccentPink else Color(0xFFF3F4F6))
            val contentColor by animateColorAsState(if (isActive) Color.White else Color(0xFF9CA3AF))
            
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
