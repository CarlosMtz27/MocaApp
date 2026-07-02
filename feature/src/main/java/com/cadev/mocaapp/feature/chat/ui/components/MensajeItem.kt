package com.cadev.mocaapp.feature.chat.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cadev.mocaapp.feature.chat.domain.model.Mensaje
import com.cadev.mocaapp.feature.chat.domain.model.TipoMensaje
import com.cadev.mocaapp.feature.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import com.cadev.mocaapp.core.utils.ThemeManager
import androidx.compose.material.icons.filled.Pause
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import kotlinx.coroutines.delay

@Composable
fun MensajeItem(
    mensaje: Mensaje,
    esMio: Boolean,
    fotoRemitente: String? = null,
    onFotoClick: (String) -> Unit = {}
) {
    val isDark = isSystemInDarkTheme() || ThemeManager.isDarkTheme
    val hora = SimpleDateFormat("h:mm a", Locale.getDefault()).format(mensaje.creadoEn.toDate())
    
    val bubbleColor = if (esMio) {
        if (isDark) Color(0xFF5E3E47) else MocaPrimaryContainer
    } else {
        if (isDark) Color(0xFF2D2921) else MocaSurfaceContainerLowest
    }
    
    val onBubbleColor = if (isDark) Color(0xFFEEE7DB) else MocaOnSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (esMio) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!esMio) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color.Black.copy(alpha = 0.3f) else MocaSurfaceVariant)
            ) {
                if (!fotoRemitente.isNullOrBlank()) {
                    AsyncImage(
                        model = fotoRemitente,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (esMio) Alignment.End else Alignment.Start
        ) {
            Surface(
                modifier = Modifier
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(
                            topStart = 24.dp,
                            topEnd = 24.dp,
                            bottomStart = if (esMio) 24.dp else 4.dp,
                            bottomEnd = if (esMio) 4.dp else 24.dp
                        )
                    )
                    .then(
                        if (!esMio) Modifier.border(
                            1.dp,
                            (if (isDark) Color.White.copy(alpha = 0.05f) else MocaTertiaryContainer.copy(alpha = 0.3f)),
                            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 4.dp)
                        ) else Modifier
                    ),
                shape = RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp,
                    bottomStart = if (esMio) 24.dp else 4.dp,
                    bottomEnd = if (esMio) 4.dp else 24.dp
                ),
                color = bubbleColor
            ) {
                Column(
                    modifier = Modifier.padding(if (mensaje.tipo == TipoMensaje.FOTO.name) 4.dp else 12.dp)
                ) {
                    when (mensaje.tipo) {
                        TipoMensaje.FOTO.name -> {
                            Column {
                                AsyncImage(
                                    model = mensaje.mediaUrl,
                                    contentDescription = "Imagen compartida",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(192.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onFotoClick(mensaje.mediaUrl) },
                                    contentScale = ContentScale.Crop
                                )
                                if (mensaje.texto.isNotBlank()) {
                                    Text(
                                        text = mensaje.texto,
                                        style = OrganicTypography.bodyMedium.copy(fontSize = 14.sp),
                                        modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp),
                                        color = onBubbleColor
                                    )
                                }
                            }
                        }
                        TipoMensaje.AUDIO.name, TipoMensaje.VOZ.name -> {
                            MensajeAudioInteractive(
                                url = mensaje.mediaUrl,
                                duracion = mensaje.duracionSegundos,
                                isDark = isDark
                            )
                        }
                        else -> {
                            Text(
                                text = mensaje.texto,
                                style = OrganicTypography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                ),
                                color = onBubbleColor
                            )
                        }
                    }
                }
            }
            Text(
                text = hora,
                style = OrganicTypography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = (if (isDark) Color(0xFFD3C3C5) else MocaOnSurfaceVariant.copy(alpha = 0.5f))
                ),
                modifier = Modifier.padding(top = 4.dp, start = if (esMio) 0.dp else 8.dp, end = if (esMio) 8.dp else 0.dp)
            )
        }
    }
}

@Composable
private fun MensajeAudioInteractive(
    url: String,
    duracion: Int,
    isDark: Boolean
) {
    val context = LocalContext.current
    val primaryColor = if (isDark) Color(0xFFE7BBC6) else MocaPrimary
    val onSurfaceColor = if (isDark) Color(0xFFEEE7DB) else MocaOnSurface
    
    var reproduciendo by remember { mutableStateOf(false) }
    var progreso by remember { mutableFloatStateOf(0f) }
    var duracionReal by remember { mutableIntStateOf(duracion) }
    
    val exoPlayer = remember {
        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
            setMediaItem(androidx.media3.common.MediaItem.fromUri(url))
            prepare()
        }
    }
    
    LaunchedEffect(reproduciendo) {
        while (reproduciendo) {
            delay(100)
            val duration = exoPlayer.duration.coerceAtLeast(1)
            progreso = exoPlayer.currentPosition.toFloat() / duration.toFloat()
            if (exoPlayer.playbackState == androidx.media3.common.Player.STATE_ENDED) {
                reproduciendo = false
                progreso = 0f
                exoPlayer.seekTo(0)
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(
            onClick = {
                if (reproduciendo) {
                    exoPlayer.pause()
                    reproduciendo = false
                } else {
                    exoPlayer.play()
                    reproduciendo = true
                }
            },
            modifier = Modifier
                .size(40.dp)
                .shadow(1.dp, CircleShape)
                .background(if (isDark) Color.Black.copy(alpha = 0.2f) else MocaSurfaceContainerLowest, CircleShape)
        ) {
            Icon(
                imageVector = if (reproduciendo) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (reproduciendo) "Pausar" else "Reproducir",
                tint = primaryColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(32.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val newProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        progreso = newProgress
                        exoPlayer.seekTo((newProgress * exoPlayer.duration).toLong())
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Ondas de audio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(30) { index ->
                    val height = remember { (6..20).random().dp }
                    val threshold = (index.toFloat() / 30f)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(height)
                            .clip(CircleShape)
                            .background(
                                if (progreso >= threshold) primaryColor 
                                else primaryColor.copy(alpha = 0.2f)
                            )
                    )
                }
            }
        }
        
        Text(
            text = "%d:%02d".format(duracionReal / 60, duracionReal % 60),
            style = OrganicTypography.labelMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor.copy(alpha = 0.8f)
            ),
            modifier = Modifier.padding(end = 4.dp)
        )
    }
}
