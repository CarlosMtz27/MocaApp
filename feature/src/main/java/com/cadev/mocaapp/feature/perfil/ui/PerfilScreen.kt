package com.cadev.mocaapp.feature.perfil.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel,
    usuarioId: String,
    parejaId: String?,
    onIrAjustes: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(usuarioId) {
        viewModel.cargarPerfil(usuarioId, parejaId)
    }

    var mostrarOpcionesFoto by remember { mutableStateOf(false) }
    var uriCameraTemp by remember { mutableStateOf<Uri?>(null) }

    val launcherGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.actualizarFotoPerfil(usuarioId, it.toString()) }
    }

    val launcherCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito) uriCameraTemp?.let {
            viewModel.actualizarFotoPerfil(usuarioId, it.toString())
        }
    }

    val launcherPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            // Ahora context está disponible porque lo capturamos arriba
            val dir = File(context.cacheDir, "camera").also { it.mkdirs() }
            val archivo = File(dir, "${UUID.randomUUID()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                archivo
            )
            uriCameraTemp = uri
            launcherCamara.launch(uri)
        }
    }

    // Diálogo opciones de foto
    if (mostrarOpcionesFoto) {
        AlertDialog(
            onDismissRequest = { mostrarOpcionesFoto = false },
            title = { Text("Foto de perfil") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Elegir de galería") },
                        leadingContent = {
                            Icon(Icons.Filled.PhotoLibrary, null)
                        },
                        modifier = Modifier.clickable {
                            mostrarOpcionesFoto = false
                            launcherGaleria.launch("image/*")
                        }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Tomar foto") },
                        leadingContent = {
                            Icon(Icons.Filled.PhotoCamera, null)
                        },
                        modifier = Modifier.clickable {
                            mostrarOpcionesFoto = false
                            launcherPermiso.launch(
                                android.Manifest.permission.CAMERA
                            )
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarOpcionesFoto = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        //Header con gradiente
        HeaderPerfil(
            usuario = uiState.usuario,
            cargandoFoto = uiState.guardandoAjuste,
            onFotoClick = { mostrarOpcionesFoto = true },
            onGaleriaClick = { launcherGaleria.launch("image/*") },
            onAjustesClick = onIrAjustes
        )

        Spacer(Modifier.height(24.dp))

        // Estadísticas
        SeccionEstadisticas(
            diasJuntos = uiState.diasJuntos,
            totalEntradas = uiState.totalEntradas,
            fechaRelacion = uiState.fechaRelacion
        )

        Spacer(Modifier.height(24.dp))

        //Info de pareja
        if (uiState.pareja != null) {
            SeccionPareja(pareja = uiState.pareja!!)
            Spacer(Modifier.height(24.dp))
        }

        //Botón logout
        Button(
            onClick = { viewModel.logout() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Icon(Icons.Filled.Logout, null)
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión")
        }

        Spacer(Modifier.height(32.dp))
    }
}

// Header
@Composable
private fun HeaderPerfil(
    usuario: com.cadev.mocaapp.feature.auth.domain.model.Usuario?,
    cargandoFoto: Boolean,
    onFotoClick: () -> Unit,
    onGaleriaClick: () -> Unit,
    onAjustesClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(top = 48.dp, bottom = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        // Botón ajustes arriba a la derecha
        IconButton(
            onClick = onAjustesClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Filled.Settings,
                contentDescription = "Ajustes",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Foto de perfil
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onFotoClick)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                if (cargandoFoto) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                } else if (!usuario?.fotoPerfil.isNullOrBlank()) {
                    AsyncImage(
                        model = usuario?.fotoPerfil,
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Inicial del nombre
                    Text(
                        text = usuario?.nombre?.firstOrNull()
                            ?.uppercase() ?: "?",
                        fontSize = 40.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Badge cámara
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = usuario?.nombre ?: "...",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = usuario?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                        .copy(alpha = 0.7f)
                )
            }
        }
    }
}

//Estadísticas

@Composable
private fun SeccionEstadisticas(
    diasJuntos: Long,
    totalEntradas: Int,
    fechaRelacion: String?
) {
    val formatoFechaLegible = SimpleDateFormat(
        "d 'de' MMMM, yyyy", Locale("es", "MX")
    )
    val fechaVisible = fechaRelacion?.let {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatoFechaLegible.format(sdf.parse(it)!!)
                .replaceFirstChar { c -> c.uppercase() }
        } catch (e: Exception) { it }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Nuestra historia",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Días juntos
            TarjetaStat(
                modifier = Modifier.weight(1f),
                emoji = "💕",
                valor = diasJuntos.toString(),
                etiqueta = "Días juntos"
            )

            // Total entradas
            TarjetaStat(
                modifier = Modifier.weight(1f),
                emoji = "📝",
                valor = totalEntradas.toString(),
                etiqueta = "Recuerdos"
            )
        }

        if (fechaVisible != null) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🗓️", fontSize = 20.sp)
                Column {
                    Text(
                        text = "Juntos desde",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                            .copy(alpha = 0.7f)
                    )
                    Text(
                        text = fechaVisible,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaStat(
    modifier: Modifier = Modifier,
    emoji: String,
    valor: String,
    etiqueta: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 28.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

//Info de pareja

@Composable
private fun SeccionPareja(
    pareja: com.cadev.mocaapp.feature.auth.domain.model.Usuario
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Mi pareja",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Foto o inicial de la pareja
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.secondary
                            .copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!pareja.fotoPerfil.isNullOrBlank()) {
                    AsyncImage(
                        model = pareja.fotoPerfil,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = pareja.nombre.firstOrNull()
                            ?.uppercase() ?: "?",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pareja.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = pareja.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                        .copy(alpha = 0.6f)
                )
            }

            Text("💕", fontSize = 24.sp)
        }
    }
}