package com.cadev.mocaapp.feature.perfil.ui

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.cadev.mocaapp.core.ui.NavRoutes
import com.cadev.mocaapp.feature.diario.domain.model.EntradaDiario
import com.cadev.mocaapp.feature.perfil.ui.components.RecuerdosCompartidos
import com.cadev.mocaapp.feature.ui.animations.CorazonesOrbitando
import java.io.File
import java.util.*

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel,
    usuarioId: String,
    parejaId: String?,
    onRegresar: () -> Unit,
    onIrAjustes: () -> Unit,
    onVerPerfilPareja: (parejaId: String) -> Unit,
    onLogout: () -> Unit,
    onVerDetalleEntrada: (String) -> Unit,
    onVerDetalleDia: (String) -> Unit,
    onIrATests: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var mostrarOpcionesFoto by remember { mutableStateOf(false) }
    var mostrarModalFotos by remember { mutableStateOf(false) }
    var mostrarModalDias by remember { mutableStateOf(false) }
    var uriCameraTemp by remember { mutableStateOf<Uri?>(null) }

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

    if (mostrarOpcionesFoto) {
        DialogOpcionesFoto(
            onDismiss = { mostrarOpcionesFoto = false },
            onGallery = { launcherGaleria.launch("image/*") },
            onCamera = { launcherPermiso.launch(android.Manifest.permission.CAMERA) }
        )
    }

    if (mostrarModalFotos) {
        ModalFotos(
            entradas = uiState.todasLasEntradas,
            onDismiss = { mostrarModalFotos = false },
            onFotoClick = { entrada -> 
                mostrarModalFotos = false
                onVerDetalleEntrada(entrada.id)
            }
        )
    }

    if (mostrarModalDias) {
        ModalDias(
            entradas = uiState.todasLasEntradas,
            onDismiss = { mostrarModalDias = false },
            onDiaClick = { fecha ->
                mostrarModalDias = false
                onVerDetalleDia(fecha)
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onIrAjustes,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 8.dp)
                    .navigationBarsPadding()
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Ajustes")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sección de Foto e Info
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.padding(bottom = 24.dp)) {
                        Surface(
                            modifier = Modifier.size(120.dp),
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(4.dp, MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().clip(CircleShape).clickable { mostrarOpcionesFoto = true },
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.guardandoAjuste) {
                                    CorazonesOrbitando(modifier = Modifier.size(40.dp))
                                } else if (!uiState.usuario?.fotoPerfil.isNullOrBlank()) {
                                    AsyncImage(
                                        model = uiState.usuario?.fotoPerfil,
                                        contentDescription = "Foto de perfil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.primaryContainer)
                                }
                            }
                        }
                        Surface(
                            onClick = { mostrarOpcionesFoto = true },
                            modifier = Modifier.size(36.dp).shadow(10.dp, CircleShape),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    Text(
                        text = uiState.usuario?.nombre ?: "Usuario",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = uiState.usuario?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Tarjeta de Relación
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.VolunteerActivism, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (uiState.pareja != null) "Vinculado con ${uiState.pareja?.nombre}" else "Buscando pareja...",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (uiState.pareja != null) {
                        Icon(Icons.Default.Verified, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Grid de Estadísticas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f), 
                    valor = uiState.totalDiasUsuario.toString(),
                    etiqueta = "DÍAS",
                    onClick = { mostrarModalDias = true }
                )
                StatCard(
                    modifier = Modifier.weight(1f), 
                    valor = uiState.totalFotos.toString(), 
                    etiqueta = "FOTOS",
                    onClick = { mostrarModalFotos = true }
                )
                StatCard(
                    modifier = Modifier.weight(1f), 
                    valor = uiState.totalTests.toString(), 
                    etiqueta = "TESTS",
                    onClick = onIrATests
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Recuerdos Compartidos
            RecuerdosCompartidos(
                entradas = uiState.todasLasEntradas,
                onVerTodos = { mostrarModalFotos = true },
                onFotoClick = { entrada, _ -> onVerDetalleEntrada(entrada.id) }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Botones de Acción
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (parejaId != null) {
                    Button(
                        onClick = { onVerPerfilPareja(parejaId) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ver Perfil de mi Pareja", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, valor: String, etiqueta: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.shadow(20.dp, RoundedCornerShape(16.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.03.sp
                )
            )
        }
    }
}

@Composable
private fun ModalFotos(
    entradas: List<EntradaDiario>,
    onDismiss: () -> Unit,
    onFotoClick: (EntradaDiario) -> Unit
) {
    val fotosConEntrada = entradas.flatMap { entrada -> 
        entrada.fotos.map { foto -> entrada to foto }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Fotos del Diario", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(fotosConEntrada) { (entrada, fotoUrl) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(
                                model = fotoUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onFotoClick(entrada) }
                            )
                            Text(
                                text = entrada.fecha,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModalDias(
    entradas: List<EntradaDiario>,
    onDismiss: () -> Unit,
    onDiaClick: (String) -> Unit
) {
    val entradasPorFecha = entradas.sortedByDescending { it.creadaEn }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Historial de Días", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(entradasPorFecha) { entrada ->
                        Surface(
                            onClick = { onDiaClick(entrada.fecha) },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(entrada.fecha, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(entrada.titulo.ifBlank { "Sin título" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogOpcionesFoto(onDismiss: () -> Unit, onGallery: () -> Unit, onCamera: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), tonalElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Foto de perfil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                OpcionFotoItem(Icons.Default.PhotoLibrary, "Elegir de galería", onGallery)
                OpcionFotoItem(Icons.Default.PhotoCamera, "Tomar foto", onCamera)
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { 
                    Text("Cancelar", color = MaterialTheme.colorScheme.primary) 
                }
            }
        }
    }
}

@Composable
private fun OpcionFotoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}
