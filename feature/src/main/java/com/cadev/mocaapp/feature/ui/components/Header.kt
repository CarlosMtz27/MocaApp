package com.cadev.mocaapp.feature.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

/**
 * BARRA SUPERIOR (HEADER) GLASSMORPHIC ACTUALIZADA
 * Incluye cambio de tema y avatares entrelazados.
 */
@Composable
fun MocaHeader(
    titulo: String,
    nombreUsuario: String,
    nombrePareja: String,
    urlAvatarUsuario: String,
    urlAvatarPareja: String,
    esModoOscuro: Boolean,
    alHacerClickEnTema: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorEsquema = MaterialTheme.colorScheme
    val colorSurface = colorEsquema.surface.copy(alpha = 0.2f)
    val colorPrimary = colorEsquema.primary
    val colorPrimaryContainer = colorEsquema.primaryContainer

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(80.dp)
            .background(colorSurface)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // BLOQUE IZQUIERDA: Título
        Box(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // BLOQUE CENTRO: Nombres (Centrado absoluto)
        if (nombrePareja.isNotBlank()) {
            Text(
                text = "$nombreUsuario & $nombrePareja",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorPrimary.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.wrapContentWidth()
            )
        }

        // BLOQUE DERECHA: Iconos y Avatares
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            // Botón de Tema (Modo Claro/Oscuro)
            IconButton(
                onClick = alHacerClickEnTema,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(colorPrimaryContainer.copy(alpha = 0.3f))
            ) {
                Icon(
                    imageVector = if (esModoOscuro) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                    contentDescription = "Cambiar tema",
                    tint = colorPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Avatares entrelazados (Miniatura)
            Box(
                modifier = Modifier.wrapContentSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(urlAvatarUsuario)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Mi perfil",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, colorEsquema.surface, CircleShape)
                        .background(colorEsquema.surfaceVariant),
                    contentScale = ContentScale.Crop
                )

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(urlAvatarPareja)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Pareja",
                    modifier = Modifier
                        .padding(start = 22.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, colorEsquema.surface, CircleShape)
                        .background(colorEsquema.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
