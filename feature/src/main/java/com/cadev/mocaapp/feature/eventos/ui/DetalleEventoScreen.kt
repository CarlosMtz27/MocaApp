package com.cadev.mocaapp.feature.eventos.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import com.cadev.mocaapp.feature.eventos.domain.model.RecordatorioOpcion
import com.cadev.mocaapp.core.model.TipoEvento
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleEventoScreen(
    viewModel: EventoViewModel,
    eventoId: String,
    usuarioId: String,
    onRegresar: () -> Unit,
    onEditar: (String) -> Unit,
    onConvertirEnRecuerdo: (String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var mostrarConfirmarEliminar by remember { mutableStateOf(false) }
    var mostrarPosponerDialog by remember { mutableStateOf(false) }

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
                TextButton(onClick = { mostrarConfirmarEliminar = false }) { Text("Cancelar") }
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
                            Icon(Icons.Filled.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
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
        val formatoLegible = SimpleDateFormat("EEEE d 'de' MMMM, yyyy", Locale.forLanguageTag("es-MX"))
        val fechaLegible = try {
            formatoLegible.format(formatoEntrada.parse(evento.fecha)!!).replaceFirstChar { it.uppercase() }
        } catch (e: Exception) { evento.fecha }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(shape = RoundedCornerShape(20.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = tipo.icono, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(evento.titulo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(tipo.etiqueta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    
                    if (evento.pospuesto && evento.fechaOriginal != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = "Pospuesto (era el ${evento.fechaOriginal})",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            InfoRow(icon = { Icon(Icons.Filled.CalendarMonth, null) }, label = "Fecha", valor = fechaLegible)
            InfoRow(icon = { Icon(Icons.Filled.Schedule, null) }, label = "Hora", valor = evento.hora)

            if (evento.descripcion.isNotBlank()) {
                InfoRow(icon = { Icon(Icons.Filled.Notes, null) }, label = "Descripción", valor = evento.descripcion)
            }

            val ahora = Calendar.getInstance().time
            val formatoCompleto = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val fechaEvento = try { formatoCompleto.parse("${evento.fecha} ${evento.hora}") } catch (e: Exception) { null }
            val esPasado = fechaEvento?.before(ahora) ?: false

            if (esPasado) {
                if (evento.convertidoEnRecuerdo) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Este evento ya pasó y se guardó como recuerdo.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { mostrarPosponerDialog = true },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Text("Posponer", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { 
                                viewModel.marcarComoRecuerdo(eventoId)
                                onConvertirEnRecuerdo(evento.fecha, evento.titulo) 
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Recuerdo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (mostrarPosponerDialog) {
        PosponerDialog(
            actualFecha = evento?.fecha ?: "",
            actualHora = evento?.hora ?: "12:00",
            onDismiss = { mostrarPosponerDialog = false },
            onConfirm = { nuevaFecha, nuevaHora ->
                viewModel.posponerEvento(eventoId, nuevaFecha, nuevaHora)
                mostrarPosponerDialog = false
            }
        )
    }
}

@Composable
fun PosponerDialog(
    actualFecha: String,
    actualHora: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val context = LocalContext.current
    var fecha by remember { mutableStateOf(actualFecha) }
    var hora by remember { mutableStateOf(actualHora) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Posponer evento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Elige la nueva fecha y hora para vuestro plan.")
                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(context, { _, y, m, d ->
                            fecha = "%04d-%02d-%02d".format(y, m + 1, d)
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, null)
                    Spacer(Modifier.width(8.dp))
                    Text(fecha)
                }
                OutlinedButton(
                    onClick = {
                        val split = hora.split(":")
                        val h = split.getOrNull(0)?.toIntOrNull() ?: 12
                        val m = split.getOrNull(1)?.toIntOrNull() ?: 0
                        TimePickerDialog(context, { _, selectedH, selectedM ->
                            hora = "%02d:%02d".format(selectedH, selectedM)
                        }, h, m, true).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AccessTime, null)
                    Spacer(Modifier.width(8.dp))
                    Text(hora)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(fecha, hora) }) { Text("Confirmar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun InfoRow(icon: @Composable () -> Unit, label: String, valor: String) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            icon()
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Text(valor, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
