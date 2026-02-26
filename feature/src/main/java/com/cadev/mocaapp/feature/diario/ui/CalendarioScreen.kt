package com.cadev.mocaapp.feature.diario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarioScreen(
    viewModel: DiarioViewModel,
    usuarioId: String,
    parejaId: String?,
    onDiaSeleccionado: (fecha: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var calendario by remember {
        mutableStateOf(Calendar.getInstance())
    }

    val anio = calendario.get(Calendar.YEAR)
    val mes = calendario.get(Calendar.MONTH) + 1

    LaunchedEffect(anio, mes) {
        viewModel.cargarMes(usuarioId, parejaId, anio, mes)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        EncabezadoMes(
            calendario = calendario,
            onMesAnterior = {
                val nuevo = calendario.clone() as Calendar
                nuevo.add(Calendar.MONTH, -1)
                calendario = nuevo
            },
            onMesSiguiente = {
                val nuevo = calendario.clone() as Calendar
                nuevo.add(Calendar.MONTH, 1)
                calendario = nuevo
            }
        )

        Spacer(Modifier.height(16.dp))

        DiasDelaSemana()

        Spacer(Modifier.height(8.dp))

        if (uiState.cargando) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            CuadriculaMes(
                calendario = calendario,
                diasConEntrada = uiState.diasConEntrada,
                onDiaClick = { fecha -> onDiaSeleccionado(fecha) }
            )
        }

        Spacer(Modifier.height(16.dp))

        Leyenda()
    }
}

//Encabezado del mes

@Composable
private fun EncabezadoMes(
    calendario: Calendar,
    onMesAnterior: () -> Unit,
    onMesSiguiente: () -> Unit
) {
    val formatoMes = SimpleDateFormat("MMMM yyyy", Locale("es", "MX"))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMesAnterior) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Mes anterior",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = formatoMes.format(calendario.time)
                .replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        IconButton(onClick = onMesSiguiente) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Mes siguiente",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Dias de la semana

@Composable
private fun DiasDelaSemana() {
    val dias = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
    Row(modifier = Modifier.fillMaxWidth()) {
        dias.forEach { dia ->
            Text(
                text = dia,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

// Cuadrícula del mes

@Composable
private fun CuadriculaMes(
    calendario: Calendar,
    diasConEntrada: Map<String, List<String>>,   // ← tipo correcto
    onDiaClick: (String) -> Unit
) {
    val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val hoy = formatoFecha.format(Date())

    val calMes = calendario.clone() as Calendar
    calMes.set(Calendar.DAY_OF_MONTH, 1)
    val primerDiaSemana = calMes.get(Calendar.DAY_OF_WEEK) - 1
    val totalDias = calMes.getActualMaximum(Calendar.DAY_OF_MONTH)
    val anio = calendario.get(Calendar.YEAR)
    val mes = calendario.get(Calendar.MONTH)

    val celdas = List(primerDiaSemana) { null } +
            List(totalDias) { it + 1 }

    val semanas = celdas.chunked(7)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        semanas.forEach { semana ->
            Row(modifier = Modifier.fillMaxWidth()) {
                val semanaCompleta = semana + List(7 - semana.size) { null }
                semanaCompleta.forEach { dia ->
                    Box(modifier = Modifier.weight(1f)) {
                        if (dia != null) {
                            val fechaCelda = Calendar.getInstance().apply {
                                set(anio, mes, dia)
                            }
                            val fechaStr = formatoFecha.format(fechaCelda.time)
                            val tipos = diasConEntrada[fechaStr]
                                ?: emptyList()   // ← lista de tipos del día
                            val esHoy = fechaStr == hoy

                            CeldaDia(
                                dia = dia,
                                esHoy = esHoy,
                                tipos = tipos,   // ← parámetro correcto
                                onClick = { onDiaClick(fechaStr) }
                            )
                        }
                    }
                }
            }
        }
    }
}

//Celda de un día

@Composable
private fun CeldaDia(
    dia: Int,
    esHoy: Boolean,
    tipos: List<String>,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (esHoy) MaterialTheme.colorScheme.primaryContainer
                    else Color.Transparent
                )
        ) {
            Text(
                text = dia.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (esHoy) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(3.dp))

        // Un punto por cada tipo único — máximo 3
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            tipos.distinct().take(3).forEach { tipoNombre ->
                val tipo = try {
                    TipoEntrada.valueOf(tipoNombre)
                } catch (e: Exception) {
                    TipoEntrada.MI_DIA
                }
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(
                            Color(
                                android.graphics.Color.parseColor(
                                    "#${tipo.colorHex}"
                                )
                            )
                        )
                )
            }
        }
    }
}

//Leyenda

@Composable
private fun Leyenda() {
    // Dividimos los 4 tipos en 2 filas de 2
    val tiposFilas = TipoEntrada.entries.chunked(2)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        tiposFilas.forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                fila.forEach { tipo ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    Color(
                                        android.graphics.Color.parseColor(
                                            "#${tipo.colorHex}"
                                        )
                                    )
                                )
                        )
                        Text(
                            text = "${tipo.emoji} ${tipo.etiqueta}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                                .copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}