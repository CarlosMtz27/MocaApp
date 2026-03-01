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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.cadev.mocaapp.feature.auth.domain.model.Usuario
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel,
    usuarioId: String,
    parejaId: String?,
    onIrAjustes: () -> Unit,
    onVerPerfilPareja: (parejaId: String) -> Unit,
    onLogout: () -> Unit
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

    if (mostrarOpcionesFoto) {
        AlertDialog(
            onDismissRequest = { mostrarOpcionesFoto = false },
            title = { Text("Foto de perfil") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Elegir de galería") },
                        leadingContent = { Icon(Icons.Filled.PhotoLibrary, null) },
                        modifier = Modifier.clickable {
                            mostrarOpcionesFoto = false
                            launcherGaleria.launch("image/*")
                        }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Tomar foto") },
                        leadingContent = { Icon(Icons.Filled.PhotoCamera, null) },
                        modifier = Modifier.clickable {
                            mostrarOpcionesFoto = false
                            launcherPermiso.launch(android.Manifest.permission.CAMERA)
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
        HeaderPerfil(
            usuario = uiState.usuario,
            cargandoFoto = uiState.guardandoAjuste,
            onFotoClick = { mostrarOpcionesFoto = true },
            onAjustesClick = onIrAjustes
        )

        Spacer(Modifier.height(24.dp))

        SeccionEstadisticas(
            diasJuntos = uiState.diasJuntos,
            totalEntradas = uiState.totalEntradas,
            fechaRelacion = uiState.fechaRelacion
        )

        Spacer(Modifier.height(24.dp))

        if (uiState.pareja != null) {
            SeccionPareja(
                pareja = uiState.pareja!!,
                onVerPerfil = { onVerPerfilPareja(uiState.pareja!!.id) }
            )
            Spacer(Modifier.height(24.dp))
        }

        Button(
            onClick = onLogout,
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

@Composable
private fun HeaderPerfil(
    usuario: Usuario?,
    cargandoFoto: Boolean,
    onFotoClick: () -> Unit,
    onAjustesClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(top = 48.dp, bottom = 32.dp),
        contentAlignment = Alignment.Center
    ) {
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
                    Text(
                        text = usuario?.nombre?.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 40.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

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
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun SeccionEstadisticas(
    diasJuntos: Long,
    totalEntradas: Int,
    fechaRelacion: String?
) {
    val formatoFechaLegible = SimpleDateFormat("d 'de' MMMM, yyyy", Locale("es", "MX"))
    val fechaVisible = fechaRelacion?.let {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatoFechaLegible.format(sdf.parse(it)!!).replaceFirstChar { c -> c.uppercase() }
        } catch (e: Exception) {
            it
        }
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
            TarjetaStat(
                modifier = Modifier.weight(1f),
                emoji = "💕",
                valor = diasJuntos.toString(),
                etiqueta = "Días juntos"
            )
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
                    .background(MaterialTheme.colorScheme.secondaryContainer)
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
                    )
                    Text(
                        text = fechaVisible,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
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
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = emoji, fontSize = 28.sp)
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun SeccionPareja(
    pareja: Usuario,
    onVerPerfil: () -> Unit
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
                .clickable(onClick = onVerPerfil)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Foto
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                if (!pareja.fotoPerfil.isNullOrBlank()) {
                    AsyncImage(
                        model = pareja.fotoPerfil,
                        contentDescription = "Foto de perfil de pareja",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = pareja.nombre.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Nombre y email
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pareja.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = pareja.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}
