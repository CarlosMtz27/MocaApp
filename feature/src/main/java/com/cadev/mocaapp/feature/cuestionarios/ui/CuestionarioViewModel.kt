package com.cadev.mocaapp.feature.cuestionarios.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.cuestionarios.domain.model.Cuestionario
import com.cadev.mocaapp.feature.cuestionarios.domain.model.EstadoCuestionario
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
    val estadosCuestionarios: Map<String, EstadoCuestionario> = emptyMap(),
    val cuestionarioActual: Cuestionario? = null,
    val preguntaActual: Int = 0,
    val respuestas: Map<String, String> = emptyMap(),
    val respuestasFoto: Map<String, String> = emptyMap(),
    val subiendoFoto: Boolean = false,
    val enviando: Boolean = false,
    val completado: Boolean = false,
    val resultado: ResultadoCuestionario? = null,
    val respuestasPareja: Map<String, String> = emptyMap(),
    val respuestasFotoPareja: Map<String, String> = emptyMap(),
    val error: String? = null,
    val creando: Boolean = false,
    val creadoExitoso: Boolean = false
)

class CuestionarioViewModel(
    private val repository: CuestionarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CuestionarioUiState())
    val uiState: StateFlow<CuestionarioUiState> = _uiState.asStateFlow()

    fun cargarCuestionarios(
        relacionId: String,
        usuarioId: String,
        parejaId: String
    ) {
        // no recargar si ya tenemos datos
        if (_uiState.value.cuestionarios.isNotEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)

            // Cuestionarios e historial en paralelo
            val jobCuestionarios = launch {
                repository.obtenerCuestionarios(relacionId).fold(
                    onSuccess = { lista ->
                        _uiState.value = _uiState.value.copy(cuestionarios = lista)

                        // Estados también en paralelo (ya usa async internamente)
                        repository.obtenerEstadosTodos(
                            lista, usuarioId, parejaId
                        ).fold(
                            onSuccess = { estados ->
                                _uiState.value = _uiState.value.copy(
                                    estadosCuestionarios = estados
                                )
                            },
                            onFailure = { }
                        )
                    },
                    onFailure = {
                        _uiState.value = _uiState.value.copy(
                            error = "Error al cargar cuestionarios"
                        )
                    }
                )
            }

            val jobHistorial = launch {
                repository.obtenerHistorial(relacionId, usuarioId).fold(
                    onSuccess = { historial ->
                        _uiState.value = _uiState.value.copy(historial = historial)
                    },
                    onFailure = { }
                )
            }

            // Esperar ambos para quitar el loading
            jobCuestionarios.join()
            jobHistorial.join()

            _uiState.value = _uiState.value.copy(cargando = false)
        }
    }

    fun refrescarEstados(usuarioId: String, parejaId: String) {
        // Para refrescar estados sin mostrar loading (volver de responder)
        val lista = _uiState.value.cuestionarios
        if (lista.isEmpty()) return
        viewModelScope.launch {
            repository.obtenerEstadosTodos(lista, usuarioId, parejaId).fold(
                onSuccess = { estados ->
                    _uiState.value = _uiState.value.copy(
                        estadosCuestionarios = estados
                    )
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
                        respuestasFoto = emptyMap(),
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
        val map = _uiState.value.respuestas.toMutableMap()
        map[preguntaId] = valor
        _uiState.value = _uiState.value.copy(respuestas = map)
    }

    fun subirFotoRespuesta(preguntaId: String, rutaLocal: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(subiendoFoto = true)
            repository.subirFoto(rutaLocal).fold(
                onSuccess = { url ->
                    val fotoMap = _uiState.value.respuestasFoto.toMutableMap()
                    fotoMap[preguntaId] = url
                    val respMap = _uiState.value.respuestas.toMutableMap()
                    respMap[preguntaId] = url
                    _uiState.value = _uiState.value.copy(
                        respuestasFoto = fotoMap,
                        respuestas = respMap,
                        subiendoFoto = false
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al subir foto",
                        subiendoFoto = false
                    )
                }
            )
        }
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
                cuestionarioId = cuestionario.id,
                usuarioId = usuarioId,
                respuestas = _uiState.value.respuestas,
                respuestasFoto = _uiState.value.respuestasFoto
            ).fold(
                onSuccess = {
                    val estado = repository.obtenerEstado(
                        cuestionario.id, usuarioId, parejaId
                    ).getOrDefault(EstadoCuestionario.YO_RESPONDÍ)

                    if (estado == EstadoCuestionario.AMBOS) {
                        repository.calcularResultado(
                            cuestionario.id, relacionId, usuarioId, parejaId
                        ).fold(
                            onSuccess = { resultado ->
                                _uiState.value = _uiState.value.copy(
                                    resultado = resultado
                                )
                            },
                            onFailure = { }
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        completado = true,
                        enviando = false
                    )
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
            // Todas en paralelo
            launch {
                repository.obtenerResultado(cuestionarioId).fold(
                    onSuccess = { resultado ->
                        _uiState.value = _uiState.value.copy(resultado = resultado)
                    },
                    onFailure = { }
                )
            }
            launch {
                repository.obtenerRespuestas(cuestionarioId, usuarioId).fold(
                    onSuccess = { resp ->
                        _uiState.value = _uiState.value.copy(respuestas = resp)
                    },
                    onFailure = { }
                )
            }
            launch {
                repository.obtenerRespuestas(cuestionarioId, parejaId).fold(
                    onSuccess = { resp ->
                        _uiState.value = _uiState.value.copy(respuestasPareja = resp)
                    },
                    onFailure = { }
                )
            }
            launch {
                repository.obtenerRespuestasFoto(cuestionarioId, usuarioId).fold(
                    onSuccess = { fotos ->
                        val map = _uiState.value.respuestasFoto.toMutableMap()
                        map.putAll(fotos)
                        _uiState.value = _uiState.value.copy(respuestasFoto = map)
                    },
                    onFailure = { }
                )
            }
            launch {
                repository.obtenerRespuestasFoto(cuestionarioId, parejaId).fold(
                    onSuccess = { fotos ->
                        _uiState.value = _uiState.value.copy(
                            respuestasFotoPareja = fotos
                        )
                    },
                    onFailure = { }
                )
            }
        }
    }

    fun crearCuestionario(cuestionario: Cuestionario) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(creando = true)
            repository.crearCuestionario(cuestionario).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        creando = false,
                        creadoExitoso = true,
                        //Forzar recarga la próxima vez
                        cuestionarios = emptyList()
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

    fun subirFotoPregunta(rutaLocal: String, onUrl: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(subiendoFoto = true)
            repository.subirFoto(rutaLocal).fold(
                onSuccess = { url ->
                    _uiState.value = _uiState.value.copy(subiendoFoto = false)
                    onUrl(url)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        subiendoFoto = false,
                        error = "Error al subir imagen"
                    )
                }
            )
        }
    }

    fun poblarPredefinidos() {
        viewModelScope.launch { repository.poblarPredefinidos() }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetearCreacion() {
        _uiState.value = _uiState.value.copy(creadoExitoso = false)
    }
}