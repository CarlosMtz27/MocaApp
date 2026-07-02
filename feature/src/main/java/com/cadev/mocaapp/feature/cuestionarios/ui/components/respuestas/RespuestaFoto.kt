package com.cadev.mocaapp.feature.cuestionarios.ui.components.respuestas

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.cadev.mocaapp.feature.ui.theme.*
import java.io.File

@Composable
fun RespuestaFoto(
    fotoUrl: String?,
    subiendo: Boolean,
    onFotoSeleccionada: (String) -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else MocaPrimary
    val colorCardBg = if (isDark) Color(0xFF333028) else MocaSurfaceContainerLowest
    val colorOnSurface = if (isDark) Color(0xFFE7BBC6) else MocaOnSurface

    // Launcher para Cámara
    val launcherCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempUri != null) {
            onFotoSeleccionada(tempUri.toString())
        }
    }

    // Launcher para Galería
    val launcherGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onFotoSeleccionada(it.toString()) }
    }

    fun launchCamera() {
        val file = File(context.cacheDir, "temp_camera_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        tempUri = uri
        launcherCamara.launch(uri)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Contenedor de la Imagen o Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colorCardBg)
                .border(
                    width = 1.dp,
                    color = colorOnSurface.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Fondo decorativo sutil animado
            val infiniteTransition = rememberInfiniteTransition(label = "bg_pulse")
            val alphaAnim by infiniteTransition.animateFloat(
                initialValue = 0.05f,
                targetValue = 0.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(MocaTertiaryFixed.copy(alpha = alphaAnim), Color.Transparent)
                        )
                    )
            )

            if (fotoUrl != null) {
                AsyncImage(
                    model = fotoUrl,
                    contentDescription = "Foto respuesta",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                if (subiendo) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = colorPrimary)
                        Text("Subiendo tu foto...", color = colorPrimary, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MocaSecondaryContainer.copy(alpha = if (isDark) 0.2f else 1f),
                        shadowElevation = if (isDark) 0.dp else 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = if (isDark) colorPrimary else MocaOnSecondaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Botones debajo de la imagen
        if (!subiendo) {
            Column(
                modifier = Modifier.widthIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { launchCamera() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MocaAccentPink),
                    shape = CircleShape,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isDark) 0.dp else 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(20.dp))
                        Text(if (fotoUrl == null) "Tomar Foto" else "Cambiar Foto", fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                }

                OutlinedButton(
                    onClick = { launcherGaleria.launch("image/*") },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(2.dp, MocaAccentPink.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MocaAccentPink)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(20.dp))
                        Text("Elegir de Galería", fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
