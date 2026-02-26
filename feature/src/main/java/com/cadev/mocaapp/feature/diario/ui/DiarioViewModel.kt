package com.cadev.mocaapp.feature.diario.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.diario.domain.model.EntradaDiario
import com.cadev.mocaapp.feature.diario.domain.model.Emocion
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import com.cadev.mocaapp.feature.diario.domain.repository.DiarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DiarioUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val entradas: List<EntradaDiario> = emptyList(),
    val diasConEntrada: Map<String, List<String>> = emptyMap(),
    val entradaCreada: Boolean = false,
    // Para el formulario de nueva entrada
    val titulo: String = "",
    val detalles: String = "",
    val emocionesSeleccionadas: List<Emocion> = emptyList(),
    val fotosSeleccionadas: List<String> = emptyList(),
    val compartir: Boolean = false
)

class DiarioViewModel(
    private val repository: DiarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiarioUiState())
    val uiState: StateFlow<DiarioUiState> = _uiState.asStateFlow()

    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun cargarMes(usuarioId: String, parejaId: String?, anio: Int, mes: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)

            repository.obtenerDiasConEntrada(
                usuarioId, parejaId, anio, mes
            ).fold(
                onSuccess = { dias ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        diasConEntrada = dias
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = "No se pudo cargar el calendario"
                    )
                }
            )
        }
    }

    fun cargarEntradasDelDia(
        usuarioId: String,
        parejaId: String?,
        fecha: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)

            repository.obtenerEntradasDelDia(usuarioId, parejaId, fecha).fold(
                onSuccess = { entradas ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        entradas = entradas
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = "No se pudieron cargar las entradas"
                    )
                }
            )
        }
    }

    // Formulario de nueva entrada

    fun actualizarTitulo(valor: String) {
        _uiState.value = _uiState.value.copy(titulo = valor)
    }

    fun actualizarDetalles(valor: String) {
        _uiState.value = _uiState.value.copy(detalles = valor)
    }

    fun toggleEmocion(emocion: Emocion) {
        val actuales = _uiState.value.emocionesSeleccionadas.toMutableList()
        if (actuales.contains(emocion)) {
            actuales.remove(emocion)
        } else {
            actuales.add(emocion)
        }
        _uiState.value = _uiState.value.copy(emocionesSeleccionadas = actuales)
    }

    fun agregarFoto(rutaLocal: String) {
        val fotos = _uiState.value.fotosSeleccionadas.toMutableList()
        fotos.add(rutaLocal)
        _uiState.value = _uiState.value.copy(fotosSeleccionadas = fotos)
    }

    fun toggleCompartir() {
        _uiState.value = _uiState.value.copy(
            compartir = !_uiState.value.compartir
        )
    }

    fun guardarEntrada(
        usuarioId: String,
        parejaId: String?,
        fecha: String,
        tipo: String = TipoEntrada.MI_DIA.name
    ) {
        val estado = _uiState.value

        if (estado.titulo.isBlank()) {
            _uiState.value = estado.copy(error = "Agrega un título al día")
            return
        }

        viewModelScope.launch {
            _uiState.value = estado.copy(cargando = true, error = null)

            val entrada = EntradaDiario(
                usuarioId = usuarioId,
                fecha = fecha,
                tipo = tipo,
                titulo = estado.titulo,
                detalles = estado.detalles,
                emociones = estado.emocionesSeleccionadas.map { it.name },
                compartida = estado.compartir,
                parejaId = if (estado.compartir) parejaId else null,
                creadaEn = Date()
            )

            repository.crearEntrada(entrada, estado.fotosSeleccionadas).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        entradaCreada = true
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = "No se pudo guardar la entrada"
                    )
                }
            )
        }
    }

    fun limpiarFormulario() {
        _uiState.value = _uiState.value.copy(
            titulo = "",
            detalles = "",
            emocionesSeleccionadas = emptyList(),
            fotosSeleccionadas = emptyList(),
            compartir = false,
            entradaCreada = false,
            error = null
        )
    }

    // Devuelve la fecha de hoy formateada
    fun fechaHoy(): String = formatoFecha.format(Date())
}