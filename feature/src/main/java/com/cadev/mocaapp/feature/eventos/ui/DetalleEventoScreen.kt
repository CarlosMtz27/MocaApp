package com.cadev.mocaapp.feature.eventos.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.eventos.domain.model.RecordatorioOpcion
import com.cadev.mocaapp.core.model.TipoEvento
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleEventoScreen(
    viewModel: EventoViewModel,
    eventoId: String,
    usuarioId: String,
    onRegresar: () -> Unit,
    onEditar: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var mostrarConfirmarEliminar by remember { mutableStateOf(false) }

    LaunchedEffect(eventoId) {
        viewModel.cargarEvento(eventoId)
    }

    LaunchedEffect(uiState.eliminado) {
        if (uiState.eliminado) onRegresar()
    }

    val evento = uiState.eventoActual

    if (mostrarConfirmarEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarEliminar = false },
            title = { Text("Eliminar evento") },
            text = { Text("¿Seguro que quieres eliminar este evento?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmarEliminar = false
                        viewModel.eliminarEvento(context, eventoId)
                    }
                ) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmarEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                actions = {
                    if (evento?.creadoPor == usuarioId) {
                        IconButton(onClick = { onEditar(eventoId) }) {
                            Icon(Icons.Filled.Edit, "Editar")
                        }
                        IconButton(onClick = { mostrarConfirmarEliminar = true }) {
                            Icon(
                                Icons.Filled.Delete, "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.cargando || evento == null) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val tipo = try { TipoEvento.valueOf(evento.tipo) } catch (e: Exception) { TipoEvento.OTRO }

        val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatoLegible = SimpleDateFormat(
            "EEEE d 'de' MMMM, yyyy", Locale("es", "MX")
        )
        val fechaLegible = try {
            formatoLegible.format(formatoEntrada.parse(evento.fecha)!!)
                .replaceFirstChar { it.uppercase() }
        } catch (e: Exception) { evento.fecha }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Card(shape = RoundedCornerShape(20.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(tipo.emoji, fontSize = 52.sp)
                    Text(
                        evento.titulo,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        tipo.etiqueta,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Fecha y hora
            InfoRow(
                icon = { Icon(Icons.Filled.CalendarMonth, null) },
                label = "Fecha",
                valor = fechaLegible
            )
            InfoRow(
                icon = { Icon(Icons.Filled.Schedule, null) },
                label = "Hora",
                valor = evento.hora
            )

            // Descripción
            if (evento.descripcion.isNotBlank()) {
                InfoRow(
                    icon = { Icon(Icons.Filled.Notes, null) },
                    label = "Descripción",
                    valor = evento.descripcion
                )
            }

            // Recordatorio
            if (evento.recordatorio) {
                val opcion = RecordatorioOpcion.entries
                    .find { it.minutos == evento.minutosAntes }
                InfoRow(
                    icon = { Icon(Icons.Filled.Notifications, null) },
                    label = "Recordatorio",
                    valor = opcion?.etiqueta ?: "${evento.minutosAntes} minutos antes"
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: @Composable () -> Unit,
    label: String,
    valor: String
) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(valor, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}