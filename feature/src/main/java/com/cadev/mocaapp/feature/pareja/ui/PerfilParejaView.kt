package com.cadev.mocaapp.feature.pareja.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.cadev.mocaapp.feature.diario.domain.model.EntradaDiario
import com.cadev.mocaapp.feature.pareja.ui.components.BotonTeAmoAnimado
import com.cadev.mocaapp.feature.perfil.ui.PerfilViewModel
import com.cadev.mocaapp.feature.perfil.ui.components.RecuerdosCompartidos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilParejaScreen(
    viewModel: PerfilViewModel,
    parejaId: String,
    onRegresar: () -> Unit,
    onVerDetalleEntrada: (String) -> Unit,
    onVerDetalleDia: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pareja = uiState.pareja
    val scrollState = rememberScrollState()
    
    var mostrarModalFotos by remember { mutableStateOf(false) }
    var mostrarModalDias by remember { mutableStateOf(false) }

    if (mostrarModalFotos) {
        ModalFotos(
            entradas = uiState.todasLasEntradasPareja,
            onDismiss = { mostrarModalFotos = false },
            onFotoClick = { entrada -> 
                mostrarModalFotos = false
                onVerDetalleEntrada(entrada.id)
            }
        )
    }

    if (mostrarModalDias) {
        ModalDias(
            entradas = uiState.todasLasEntradasPareja,
            onDismiss = { mostrarModalDias = false },
            onDiaClick = { fecha ->
                mostrarModalDias = false
                onVerDetalleDia(fecha)
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Mi pareja",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = (-0.02).sp
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onRegresar) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(bottom = 28.dp)
            ) {
                // Header con Gradiente y Foto
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), MaterialTheme.colorScheme.background)
                            ),
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        modifier = Modifier
                            .offset(y = 64.dp)
                            .size(128.dp)
                            .shadow(30.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(4.dp, MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        if (!pareja?.fotoPerfil.isNullOrBlank()) {
                            AsyncImage(
                                model = pareja.fotoPerfil,
                                contentDescription = "Foto de Pareja",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                               Text(
                                   text = pareja?.nombre?.firstOrNull()?.uppercase()?.toString() ?: "?",
                                   fontSize = 40.sp,
                                   fontWeight = FontWeight.Bold,
                                   color = MaterialTheme.colorScheme.primary
                               )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))

                // Info de la pareja
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = pareja?.nombre ?: "Pareja",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.01).sp
                        )
                    )
                    Text(
                        text = pareja?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Widget de Distancia
                    Surface(
                        modifier = Modifier.shadow(15.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "A 4.2 KM DE TI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.03.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Fila de Estadísticas
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .shadow(30.dp, RoundedCornerShape(16.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(Modifier.weight(1f), uiState.totalDiasPareja.toString(), "DÍAS", onClick = { mostrarModalDias = true })
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))
                        StatItem(Modifier.weight(1f), uiState.totalFotosPareja.toString(), "FOTOS", onClick = { mostrarModalFotos = true })
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))
                        StatItem(Modifier.weight(1f), uiState.totalTestsPareja.toString(), "TESTS", onClick = { })
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Recuerdos Compartidos
                RecuerdosCompartidos(
                    entradas = uiState.todasLasEntradasPareja,
                    onVerTodos = { mostrarModalFotos = true },
                    onFotoClick = { entrada, _ -> onVerDetalleEntrada(entrada.id) }
                )

                Spacer(modifier = Modifier.height(40.dp))

                // BOTÓN DE ACCIÓN ANIMADO
                BotonTeAmoAnimado(
                    onEnviar = {
                        viewModel.enviarTeAmo(parejaId, uiState.usuario?.nombre ?: "Alguien")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun StatItem(modifier: Modifier, valor: String, etiqueta: String, onClick: () -> Unit) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.05.sp
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
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
