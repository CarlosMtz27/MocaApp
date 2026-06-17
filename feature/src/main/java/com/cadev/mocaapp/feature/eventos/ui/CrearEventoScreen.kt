package com.cadev.mocaapp.feature.eventos.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.cadev.mocaapp.feature.eventos.domain.model.RecordatorioOpcion
import com.cadev.mocaapp.core.model.TipoEvento
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearEventoScreen(
    viewModel: EventoViewModel,
    usuarioId: String,
    parejaId: String,
    relacionId: String,
    onGuardado: () -> Unit,
    onRegresar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.guardado) {
        if (uiState.guardado) {
            viewModel.limpiarFormulario()
            onGuardado()
        }
    }

    // DatePicker state
    val datePickerState = rememberDatePickerState()
    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarTimePicker by remember { mutableStateOf(false) }
    var mostrarTipoPicker by remember { mutableStateOf(false) }
    var mostrarRecordatorioPicker by remember { mutableStateOf(false) }

    // Sincronizar fecha seleccionada
    LaunchedEffect(datePickerState.selectedDateMillis) {
        val millis = datePickerState.selectedDateMillis ?: return@LaunchedEffect
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        val fecha = "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        viewModel.actualizarFecha(fecha)
    }

    if (mostrarDatePicker) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (mostrarTimePicker) {
        val hora = uiState.hora.split(":").getOrNull(0)?.toIntOrNull() ?: 12
        val min  = uiState.hora.split(":").getOrNull(1)?.toIntOrNull() ?: 0
        TimePickerDialog(
            context, { _, h, m ->
                viewModel.actualizarHora("%02d:%02d".format(h, m))
                mostrarTimePicker = false
            }, hora, min, true
        ).show()
        mostrarTimePicker = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo evento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.guardarEvento(context, usuarioId, parejaId, relacionId)
                        },
                        enabled = uiState.titulo.isNotBlank() &&
                                uiState.fecha.isNotBlank() && !uiState.cargando
                    ) {
                        if (uiState.cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Guardar",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tipo de evento
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Tipo de evento",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    ExposedDropdownMenuBox(
                        expanded = mostrarTipoPicker,
                        onExpandedChange = { mostrarTipoPicker = it }
                    ) {
                        val tipoActual = try {
                            TipoEvento.valueOf(uiState.tipo)
                        } catch (e: Exception) { TipoEvento.OTRO }

                        OutlinedTextField(
                            value = "${tipoActual.emoji} ${tipoActual.etiqueta}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(mostrarTipoPicker)
                            },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = mostrarTipoPicker,
                            onDismissRequest = { mostrarTipoPicker = false }
                        ) {
                            TipoEvento.entries.forEach { tipo ->
                                DropdownMenuItem(
                                    text = { Text("${tipo.emoji} ${tipo.etiqueta}") },
                                    onClick = {
                                        viewModel.actualizarTipo(tipo.name)
                                        mostrarTipoPicker = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Info básica
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Información",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = uiState.titulo,
                        onValueChange = viewModel::actualizarTitulo,
                        label = { Text("Título *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = uiState.descripcion,
                        onValueChange = viewModel::actualizarDescripcion,
                        label = { Text("Descripción (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Fecha y hora
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Fecha y hora",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.fecha.ifBlank { "Seleccionar" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Fecha *") },
                            trailingIcon = {
                                Icon(Icons.Filled.CalendarMonth, null)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clickable { mostrarDatePicker = true },
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        OutlinedTextField(
                            value = uiState.hora,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hora") },
                            trailingIcon = { Icon(Icons.Filled.Schedule, null) },
                            modifier = Modifier
                                .weight(1f)
                                .clickable { mostrarTimePicker = true },
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Recordatorio
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recordatorio",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = uiState.recordatorio,
                            onCheckedChange = { viewModel.toggleRecordatorio() }
                        )
                    }

                    if (uiState.recordatorio) {
                        ExposedDropdownMenuBox(
                            expanded = mostrarRecordatorioPicker,
                            onExpandedChange = { mostrarRecordatorioPicker = it }
                        ) {
                            val opcionActual = RecordatorioOpcion.entries
                                .find { it.minutos == uiState.minutosAntes }
                                ?: RecordatorioOpcion.UNA_HORA

                            OutlinedTextField(
                                value = opcionActual.etiqueta,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Avisar") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        mostrarRecordatorioPicker
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = mostrarRecordatorioPicker,
                                onDismissRequest = { mostrarRecordatorioPicker = false }
                            ) {
                                RecordatorioOpcion.entries.forEach { opcion ->
                                    DropdownMenuItem(
                                        text = { Text(opcion.etiqueta) },
                                        onClick = {
                                            viewModel.actualizarMinutosAntes(opcion.minutos)
                                            mostrarRecordatorioPicker = false
                                        }
                                    )
                                }
                            }
                        }

                        Text(
                            "También se notificará a tu pareja al crear el evento.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            if (uiState.error != null) {
                Text(
                    uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}