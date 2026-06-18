package com.cadev.mocaapp.feature.notas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.notas.domain.model.NotaActual
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotasScreen(
    viewModel: NotaViewModel,
    relacionId: String,
    usuarioId: String,
    nombreUsuario: String,
    parejaId: String?,
    onRegresar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var mostrarConfirmarEliminar by remember { mutableStateOf(false) }

    LaunchedEffect(relacionId, usuarioId, parejaId) {
        viewModel.iniciar(context, relacionId, usuarioId, parejaId)
        viewModel.limpiarBadge(usuarioId)
    }

    if (mostrarConfirmarEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarEliminar = false },
            title = { Text("Eliminar nota") },
            text = { Text("¿Seguro que quieres eliminar tu nota para tu pareja?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarNota(relacionId, usuarioId)
                    mostrarConfirmarEliminar = false
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmarEliminar = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notas de pareja", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // SECCIÓN 1: NOTA DE MI PAREJA (Lectura)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "💝 Nota de tu pareja para ti",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)) // Light Blue Post-it
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        if (uiState.notaPareja != null) {
                            Column {
                                Text(
                                    text = uiState.notaPareja?.texto ?: "",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Black
                                )
                                Spacer(Modifier.height(8.dp))
                                val fechaStr = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault())
                                    .format(uiState.notaPareja!!.actualizadaEn.toDate())
                                Text(
                                    text = "Actualizada: $fechaStr",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.DarkGray
                                )
                            }
                        } else {
                            Text(
                                text = "Tu pareja aún no te ha dejado una nota...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }

            Divider()

            // SECCIÓN 2: MI NOTA PARA MI PAREJA (Edición)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📝 Tu nota para tu pareja",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row {
                        if (uiState.miNota != null) {
                            IconButton(onClick = { mostrarConfirmarEliminar = true }) {
                                Icon(Icons.Filled.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        IconButton(
                            onClick = {
                                viewModel.guardarNota(context, relacionId, usuarioId, nombreUsuario, parejaId)
                            },
                            enabled = !uiState.guardando && uiState.borrador.isNotBlank() && uiState.borrador != uiState.miNota?.texto
                        ) {
                            if (uiState.guardando) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.Save, "Guardar", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)) // Post-it yellow
                ) {
                    TextField(
                        value = uiState.borrador,
                        onValueChange = { viewModel.actualizarBorrador(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Escribe algo especial para tu pareja...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
                
                if (uiState.miNota != null) {
                    val fechaStr = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault())
                        .format(uiState.miNota!!.actualizadaEn.toDate())
                    Text(
                        text = "Última vez guardada: $fechaStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}
