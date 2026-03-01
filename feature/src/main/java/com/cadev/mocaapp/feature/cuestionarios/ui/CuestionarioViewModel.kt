package com.cadev.mocaapp.feature.cuestionarios.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.cuestionarios.domain.model.Cuestionario
import com.cadev.mocaapp.feature.cuestionarios.domain.model.ResultadoCuestionario
import com.cadev.mocaapp.feature.cuestionarios.domain.repository.CuestionarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CuestionarioUiState(
    val cargando: Boolean = false,
    val cuestionarios: List<Cuestionario> = emptyList(),
    val historial: List<Cuestionario> = emptyList(),
    val cuestionarioActual: Cuestionario? = null,
    val preguntaActual: Int = 0,
    val respuestas: Map<String, String> = emptyMap(),
    val enviando: Boolean = false,
    val completado: Boolean = false,
    val parejaCompletó: Boolean = false,
    val resultado: ResultadoCuestionario? = null,
    val respuestasPareja: Map<String, String> = emptyMap(),
    val error: String? = null,
    // Crear cuestionario
    val creando: Boolean = false,
    val creadoExitoso: Boolean = false
)

class CuestionarioViewModel(
    private val repository: CuestionarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CuestionarioUiState())
    val uiState: StateFlow<CuestionarioUiState> = _uiState.asStateFlow()

    fun cargarCuestionarios(relacionId: String, usuarioId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)

            repository.obtenerCuestionarios(relacionId).fold(
                onSuccess = { lista ->
                    _uiState.value = _uiState.value.copy(
                        cuestionarios = lista,
                        cargando = false
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al cargar cuestionarios",
                        cargando = false
                    )
                }
            )

            repository.obtenerHistorial(relacionId, usuarioId).fold(
                onSuccess = { historial ->
                    _uiState.value = _uiState.value.copy(historial = historial)
                },
                onFailure = { }
            )
        }
    }

    fun iniciarCuestionario(cuestionarioId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            repository.obtenerCuestionario(cuestionarioId).fold(
                onSuccess = { cuestionario ->
                    _uiState.value = _uiState.value.copy(
                        cuestionarioActual = cuestionario,
                        preguntaActual = 0,
                        respuestas = emptyMap(),
                        completado = false,
                        resultado = null,
                        cargando = false
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al cargar cuestionario",
                        cargando = false
                    )
                }
            )
        }
    }

    fun responderPregunta(preguntaId: String, valor: String) {
        val nuevasRespuestas = _uiState.value.respuestas.toMutableMap()
        nuevasRespuestas[preguntaId] = valor
        _uiState.value = _uiState.value.copy(respuestas = nuevasRespuestas)
    }

    fun siguientePregunta() {
        val total = _uiState.value.cuestionarioActual?.preguntas?.size ?: 0
        val actual = _uiState.value.preguntaActual
        if (actual < total - 1) {
            _uiState.value = _uiState.value.copy(preguntaActual = actual + 1)
        }
    }

    fun preguntaAnterior() {
        val actual = _uiState.value.preguntaActual
        if (actual > 0) {
            _uiState.value = _uiState.value.copy(preguntaActual = actual - 1)
        }
    }

    fun enviarRespuestas(
        usuarioId: String,
        parejaId: String,
        relacionId: String
    ) {
        val cuestionario = _uiState.value.cuestionarioActual ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(enviando = true)

            repository.guardarRespuestas(
                cuestionario.id, usuarioId, _uiState.value.respuestas
            ).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(completado = true)

                    // Verificar si la pareja ya respondió
                    repository.parejaRespondio(cuestionario.id, parejaId).fold(
                        onSuccess = { yaRespondio ->
                            _uiState.value = _uiState.value.copy(
                                parejaCompletó = yaRespondio
                            )
                            if (yaRespondio) {
                                calcularResultado(
                                    cuestionario.id, relacionId,
                                    usuarioId, parejaId
                                )
                            }
                        },
                        onFailure = { }
                    )

                    _uiState.value = _uiState.value.copy(enviando = false)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al guardar respuestas",
                        enviando = false
                    )
                }
            )
        }
    }

    fun cargarResultado(
        cuestionarioId: String,
        usuarioId: String,
        parejaId: String
    ) {
        viewModelScope.launch {
            repository.obtenerResultado(cuestionarioId).fold(
                onSuccess = { resultado ->
                    _uiState.value = _uiState.value.copy(resultado = resultado)
                },
                onFailure = { }
            )
            repository.obtenerRespuestas(cuestionarioId, usuarioId).fold(
                onSuccess = { resp ->
                    _uiState.value = _uiState.value.copy(respuestas = resp)
                },
                onFailure = { }
            )
            repository.obtenerRespuestas(cuestionarioId, parejaId).fold(
                onSuccess = { resp ->
                    _uiState.value = _uiState.value.copy(respuestasPareja = resp)
                },
                onFailure = { }
            )
        }
    }

    private fun calcularResultado(
        cuestionarioId: String,
        relacionId: String,
        usuarioId: String,
        parejaId: String
    ) {
        viewModelScope.launch {
            repository.calcularResultado(
                cuestionarioId, relacionId, usuarioId, parejaId
            ).fold(
                onSuccess = { resultado ->
                    _uiState.value = _uiState.value.copy(resultado = resultado)
                },
                onFailure = { }
            )
        }
    }

    fun crearCuestionario(cuestionario: Cuestionario) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(creando = true)
            repository.crearCuestionario(cuestionario).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        creando = false,
                        creadoExitoso = true
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        creando = false,
                        error = "Error al crear cuestionario"
                    )
                }
            )
        }
    }

    fun poblarPredefinidos() {
        viewModelScope.launch {
            repository.poblarPredefinidos()
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetearCreacion() {
        _uiState.value = _uiState.value.copy(creadoExitoso = false)
    }
}