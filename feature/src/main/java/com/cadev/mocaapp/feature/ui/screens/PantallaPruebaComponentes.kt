package com.cadev.mocaapp.feature.ui.screens

import androidx.compose.runtime.*
import com.cadev.mocaapp.feature.diario.ui.DiarioViewModel

/**
 * PANTALLA DE PRUEBA PARA NUEVOS COMPONENTES
 * Aquí iremos montando y probando las pantallas de la Sección 4.
 */
@Composable
fun PantallaPruebaComponentes(
    viewModel: DiarioViewModel,
    usuarioId: String,
    alVolver: () -> Unit
) {
    var pantallaActual by remember { mutableStateOf("calendario") }
    var fechaSeleccionada by remember { mutableStateOf("") }
    var tipoSeleccionado by remember { mutableStateOf("MI_DIA") }
    var entradaIdSeleccionada by remember { mutableStateOf("") }

    when (pantallaActual) {
        "detalle_dia" -> {
            DayDetailList(
                viewModel = viewModel,
                usuarioId = usuarioId,
                parejaId = null,
                fecha = fechaSeleccionada,
                onRegresar = { pantallaActual = "calendario" },
                onIrAlCalendario = { pantallaActual = "calendario" },
                onCrearEntrada = { fecha, tipo ->
                    fechaSeleccionada = fecha
                    tipoSeleccionado = tipo
                    pantallaActual = "crear"
                },
                onVerDetalleEntrada = { id ->
                    entradaIdSeleccionada = id
                    pantallaActual = "lectura"
                }
            )
        }
        "crear" -> {
            CreateEntryScreen(
                viewModel = viewModel,
                usuarioId = usuarioId,
                parejaId = "test_pareja",
                fecha = fechaSeleccionada,
                tipo = tipoSeleccionado,
                onEntradaGuardada = { pantallaActual = "detalle_dia" },
                onRegresar = { pantallaActual = "detalle_dia" }
            )
        }
        "lectura" -> {
            EntryDetailView(
                viewModel = viewModel,
                entradaId = entradaIdSeleccionada,
                usuarioId = usuarioId,
                onRegresar = { pantallaActual = "detalle_dia" }
            )
        }
        "historial" -> {
            TimelineScreen(
                viewModel = viewModel,
                usuarioId = usuarioId,
                parejaId = "test_pareja",
                relacionId = "test_relacion",
                onVerDetalleEntrada = { id ->
                    entradaIdSeleccionada = id
                    pantallaActual = "lectura"
                },
                onVerDetalleEvento = { /* Navegar a detalle evento */ },
                onIrAAjustes = { /* Navegar a ajustes */ },
                onRegresar = { pantallaActual = "calendario" }
            )
        }
        else -> {
            CalendarView(
                viewModel = viewModel,
                usuarioId = usuarioId,
                parejaId = null,
                relacionId = "test_relacion",
                onRegresar = alVolver,
                onDiaSeleccionado = { fecha ->
                    fechaSeleccionada = fecha
                    pantallaActual = "detalle_dia"
                },
                onVerListado = { pantallaActual = "historial" },
                onVerEventos = { /* Probar eventos */ }
            )
        }
    }
}
