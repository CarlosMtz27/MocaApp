package com.cadev.mocaapp.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun HomeHeader(
    nombreUsuario: String,
    nombrePareja: String,
    urlAvatarUsuario: String,
    urlAvatarPareja: String,
    alHacerClickEnTema: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorSurface = Color(0xFFFFF8EF)
    val colorOnSurface = Color(0xFF1E1B14)
    val colorPrimary = Color(0xFF78555E)
    val colorPrimaryContainer = Color(0xFFFFD1DC)
    val colorSurfaceContainerHigh = Color(0xFFEEE7DB)

    Surface(
        color = colorSurface,
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .zIndex(40f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Saludo y estado de conexión
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Hola, $nombreUsuario",
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    color = colorOnSurface,
                    lineHeight = 30.sp
                )
                Text(
                    text = "Conectado con $nombrePareja",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorPrimary.copy(alpha = 0.6f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botón de Tema (Modo Claro/Oscuro)
                IconButton(
                    onClick = alHacerClickEnTema,
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape,
                            ambientColor = colorPrimaryContainer,
                            spotColor = colorPrimaryContainer
                        )
                        .clip(CircleShape)
                        .background(colorPrimaryContainer.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LightMode,
                        contentDescription = "Cambiar modo de luz",
                        tint = colorPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Avatares entrelazados
                Box(
                    modifier = Modifier.wrapContentSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Avatar del usuario actual (fondo)
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(urlAvatarUsuario)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Tu avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(2.dp, colorSurface, CircleShape)
                            .background(colorSurfaceContainerHigh),
                        contentScale = ContentScale.Crop
                    )

                    // Avatar de la pareja (superpuesto con margen negativo)
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(urlAvatarPareja)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar de $nombrePareja",
                        modifier = Modifier
                            .padding(start = 32.dp) // Solapamiento de 16dp (48-16=32)
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(2.dp, colorSurface, CircleShape)
                            .background(colorSurfaceContainerHigh),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeHeaderPreview() {
    Box(modifier = Modifier.background(Color(0xFFFFF8EF))) {
        HomeHeader(
            nombreUsuario = "Alex",
            nombrePareja = "Sam",
            urlAvatarUsuario = "",
            urlAvatarPareja = "",
            alHacerClickEnTema = {}
        )
    }
}
