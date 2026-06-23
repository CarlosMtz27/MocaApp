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

/**
 * ESTA ES LA PANTALLA PARA CREAR PLANES
 * 
 * Qué hace:
 * Aquí permitimos que la pareja registre un nuevo evento en nuestro calendario 
 * compartido. Podemos elegir el tipo de cita, ponerle un título, definir la 
 * fecha y la hora, y activar un recordatorio para que la app nos avise.
 * 
 * Cómo lo podemos modificar:
 * Si queremos que por defecto el recordatorio sea de "2 horas antes", debemos 
 * cambiar el valor inicial en el `EventoUiState` del ViewModel.
 */
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

    /**
     * Si el evento se guarda bien se limpia el formulario y se vuelve atrás
     */
    LaunchedEffect(uiState.guardado) {
        if (uiState.guardado) {
            viewModel.limpiarFormulario()
            onGuardado()
        }
    }

    val datePickerState = rememberDatePickerState()
    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarTimePicker by remember { mutableStateOf(false) }
    var mostrarTipoPicker by remember { mutableStateOf(false) }
    var mostrarRecordatorioPicker by remember { mutableStateOf(false) }

    /**
     * Se actualiza la fecha en el gestor de datos cuando el usuario la elige en el calendario
     */
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

    /**
     * Diálogo para elegir el día en un calendario visual
     */
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

    /**
     * Diálogo para elegir la hora exacta de la cita
     */
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
                    /**
                     * Botón para enviar los datos del nuevo plan a la base de datos
                     */
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
            /**
             * Tarjeta para seleccionar qué tipo de evento es (Cita Viaje Cumpleaños etc)
             */
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
                            value = tipoActual.etiqueta,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo") },
                            leadingIcon = {
                                Icon(
                                    imageVector = tipoActual.icono,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
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
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = tipo.icono,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(tipo.etiqueta)
                                        }
                                    },
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

            /**
             * Sección para escribir el nombre del plan y una descripción opcional
             */
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

            /**
             * Botones para abrir los selectores de fecha y hora
             */
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

            /**
             * Configuración de la alarma de aviso automático
             */
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

                    /**
                     * Selector de cuánto tiempo antes se debe mostrar el aviso
                     */
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

            /**
             * Aviso de error si se intenta guardar sin título o fecha
             */
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
