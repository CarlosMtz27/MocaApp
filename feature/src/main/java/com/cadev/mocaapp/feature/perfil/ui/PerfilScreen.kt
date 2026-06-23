package com.cadev.mocaapp.feature.perfil.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.cadev.mocaapp.feature.auth.domain.model.Usuario
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * ESTA ES NUESTRA PANTALLA DE PERFIL
 * 
 * Qué hace:
 * Muestra nuestra información personal (foto, nombre y correo) y resume las 
 * estadísticas de nuestra relación, como los días que llevamos juntos. También 
 * nos permite cambiar nuestra foto y entrar a los ajustes.
 * 
 * Cómo lo podemos modificar:
 * Si queremos añadir un botón para "Editar intereses", debemos añadir una nueva 
 * función `PerfilOptionItem` en la lista inferior.
 */
@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel,
    usuarioId: String,
    parejaId: String?,
    onRegresar: () -> Unit,
    onIrAjustes: () -> Unit,
    onVerPerfilPareja: (parejaId: String) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    /**
     * Se cargan los datos del perfil al entrar en la pantalla
     */
    LaunchedEffect(usuarioId) {
        viewModel.cargarPerfil(usuarioId, parejaId)
    }

    var mostrarOpcionesFoto by remember { mutableStateOf(false) }
    var uriCameraTemp by remember { mutableStateOf<Uri?>(null) }

    /**
     * Lanzadores para elegir una foto nueva de la galería o de la cámara
     */
    val launcherGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.actualizarFotoPerfil(usuarioId, it.toString()) }
    }

    val launcherCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (exito) uriCameraTemp?.let { viewModel.actualizarFotoPerfil(usuarioId, it.toString()) }
    }

    val launcherPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) {
            val dir = File(context.cacheDir, "camera").also { it.mkdirs() }
            val archivo = File(dir, "${UUID.randomUUID()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
            uriCameraTemp = uri
            launcherCamara.launch(uri)
        }
    }

    /**
     * Diálogo para elegir entre galería o cámara al pulsar en la foto
     */
    if (mostrarOpcionesFoto) {
        DialogOpcionesFoto(
            onDismiss = { mostrarOpcionesFoto = false },
            onGallery = { launcherGaleria.launch("image/*") },
            onCamera = { launcherPermiso.launch(android.Manifest.permission.CAMERA) }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        /**
         * Fondo decorativo con degradado en la parte superior
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surface)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 100.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp).statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onRegresar,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), CircleShape)
                ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar al inicio") }
                
                IconButton(
                    onClick = onIrAjustes,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), CircleShape)
                ) { Icon(Icons.Default.Settings, null) }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    /**
                     * Espacio circular para mostrar la fotografía del usuario
                     */
                    Surface(
                        modifier = Modifier.size(120.dp).padding(4.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 8.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().clip(CircleShape).clickable { mostrarOpcionesFoto = true },
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.guardandoAjuste) {
                                CircularProgressIndicator(modifier = Modifier.size(40.dp))
                            } else if (!uiState.usuario?.fotoPerfil.isNullOrBlank()) {
                                AsyncImage(
                                    model = uiState.usuario?.fotoPerfil,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.primaryContainer)
                            }
                        }
                    }
                    /**
                     * Botón pequeño para abrir el selector de fotos rápidamente
                     */
                    FloatingActionButton(
                        onClick = { mostrarOpcionesFoto = true },
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        elevation = FloatingActionButtonDefaults.elevation(4.dp)
                    ) { Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp)) }
                }

                Spacer(Modifier.height(16.dp))
                Text(uiState.usuario?.nombre ?: "...", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Text(uiState.usuario?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }

            Spacer(Modifier.height(32.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                /**
                 * Muestra las tarjetas con los días de relación y el total de recuerdos
                 */
                SeccionEstadisticasMejorada(
                    diasJuntos = uiState.diasJuntos,
                    totalEntradas = uiState.totalEntradas,
                    fechaRelacion = uiState.fechaRelacion
                )

                /**
                 * Muestra el acceso directo al perfil de la pareja si está conectada
                 */
                if (uiState.pareja != null) {
                    CardParejaMejorada(
                        pareja = uiState.pareja!!,
                        onClick = { onVerPerfilPareja(uiState.pareja!!.id) }
                    )
                }

                /**
                 * Botón para cerrar la sesión actual de forma segura
                 */
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cuenta", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    PerfilOptionItem(Icons.Default.Logout, "Cerrar sesión", "Esperamos verte pronto", MaterialTheme.colorScheme.error, onLogout)
                }
            }
        }
    }
}

/**
 * Función que dibuja las tarjetas de resumen numérico sobre la relación
 */
@Composable
private fun SeccionEstadisticasMejorada(diasJuntos: Long, totalEntradas: Int, fechaRelacion: String?) {
    val formatoFecha = SimpleDateFormat("d 'de' MMMM, yyyy", Locale.forLanguageTag("es-MX"))
    val fechaLegible = try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        fechaRelacion?.let { formatoFecha.format(sdf.parse(it)!!) }
    } catch (e: Exception) { null }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(Modifier.weight(1f), Icons.Default.Favorite, "$diasJuntos", "Días Juntos", MaterialTheme.colorScheme.primaryContainer)
            StatCard(Modifier.weight(1f), Icons.Default.AutoStories, "$totalEntradas", "Recuerdos", MaterialTheme.colorScheme.secondaryContainer)
        }
        
        /**
         * Muestra la fecha en la que comenzó el viaje de la pareja
         */
        if (fechaLegible != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Event, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("Comenzaron su viaje el", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(fechaLegible, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Función auxiliar para crear tarjetas de datos numéricos con iconos
 */
@Composable
private fun StatCard(modifier: Modifier, icono: ImageVector, valor: String, etiqueta: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icono, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

/**
 * Función que crea la tarjeta informativa con el nombre y foto de la pareja
 */
@Composable
private fun CardParejaMejorada(pareja: Usuario, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Tu otra mitad", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.size(60.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)) {
                    if (!pareja.fotoPerfil.isNullOrBlank()) {
                        AsyncImage(model = pareja.fotoPerfil, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(pareja.nombre.firstOrNull()?.uppercase() ?: "?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(pareja.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Ver perfil completo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            }
        }
    }
}

/**
 * Función genérica para cada una de las opciones de la lista de perfil
 */
@Composable
private fun PerfilOptionItem(icon: ImageVector, title: String, subtitle: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(Modifier.size(40.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = color)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}

/**
 * Diálogo flotante para seleccionar el origen de la nueva foto de perfil
 */
@Composable
private fun DialogOpcionesFoto(onDismiss: () -> Unit, onGallery: () -> Unit, onCamera: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), tonalElevation = 8.dp) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Foto de perfil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                PerfilOptionItem(Icons.Default.PhotoLibrary, "Elegir de galería", "Busca en tus fotos", MaterialTheme.colorScheme.primary, { onGallery(); onDismiss() })
                PerfilOptionItem(Icons.Default.PhotoCamera, "Tomar foto", "Usa la cámara", MaterialTheme.colorScheme.primary, { onCamera(); onDismiss() })
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Cancelar") }
            }
        }
    }
}
