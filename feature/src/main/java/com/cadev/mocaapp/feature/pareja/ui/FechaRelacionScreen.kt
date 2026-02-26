package com.cadev.mocaapp.feature.pareja.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FechaRelacionScreen(
    viewModel: ParejaViewModel,
    relacionId: String,
    onFechaGuardada: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // DatePickerState maneja todo el estado del selector de fecha
    val datePickerState = rememberDatePickerState(
        // Por defecto selecciona hoy
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    val formatoFecha = SimpleDateFormat("d 'de' MMMM, yyyy", Locale("es", "MX"))

    // Navegar cuando la fecha se guarda exitosamente
    LaunchedEffect(uiState.fechaGuardada) {
        if (uiState.fechaGuardada) onFechaGuardada()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        //Encabezado
        Text(text = "🗓️", fontSize = 56.sp)
        Spacer(Modifier.height(12.dp))

        Text(
            text = "¿Cuándo empezó su historia?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Esta fecha será el punto de inicio\ndel contador de días juntos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        // Selector de fecha
        // Tarjeta contenedora para el DatePicker
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            DatePicker(
                state = datePickerState,
                // Oculta el campo de texto manual — solo calendario visual
                showModeToggle = false,
                title = null,
                headline = null,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))

        //Fecha seleccionada
        val fechaSeleccionada = datePickerState.selectedDateMillis
        if (fechaSeleccionada != null) {
            val fechaTexto = formatoFecha.format(Date(fechaSeleccionada))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💕 $fechaTexto",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Error
        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
        }

        //Botón confirmar
        Button(
            onClick = {
                val fecha = datePickerState.selectedDateMillis
                if (fecha != null) {
                    viewModel.guardarFechaInicio(relacionId, fecha)
                }
            },
            enabled = !uiState.cargando
                    && datePickerState.selectedDateMillis != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (uiState.cargando) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Confirmar fecha 💕")
            }
        }
    }
}