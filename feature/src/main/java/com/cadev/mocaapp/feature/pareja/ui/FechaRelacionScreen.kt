package com.cadev.mocaapp.feature.pareja.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
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

/**
 * ESTA ES NUESTRA PANTALLA DE ANIVERSARIO
 * 
 * Qué hace:
 * Aquí elegimos el día exacto en que empezó nuestra historia. Es muy 
 * importante porque de aquí sacamos los días que llevamos juntos para 
 * mostrarlo en nuestra pantalla de inicio.
 * 
 * Cómo lo podemos modificar:
 * Si queremos que el calendario empiece en una fecha específica del pasado, 
 * debemos ajustar el `initialSelectedDateMillis`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FechaRelacionScreen(
    viewModel: ParejaViewModel,
    relacionId: String,
    onFechaGuardada: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    /**
     * El estado del calendario visual que por defecto marca el día de hoy
     */
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    val formatoFecha = SimpleDateFormat("d 'de' MMMM, yyyy", Locale.forLanguageTag("es-MX"))

    /**
     * Si la fecha se guarda correctamente se avisa a la aplicación para entrar a la pantalla principal
     */
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

        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary
        )
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

        /**
         * Contenedor visual para el selector de fechas integrado de Android
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                title = null,
                headline = null,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))

        /**
         * Muestra una previsualización de la fecha elegida con un icono de corazón
         */
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = fechaTexto,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
        }

        /**
         * Botón de confirmación para guardar definitivamente el día de aniversario
         */
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
                Text("Confirmar fecha")
            }
        }
    }
}
