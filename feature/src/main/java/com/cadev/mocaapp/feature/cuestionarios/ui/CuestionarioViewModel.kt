package com.cadev.mocaapp.feature.cuestionarios.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.cuestionarios.domain.model.Cuestionario
import com.cadev.mocaapp.feature.cuestionarios.domain.model.EstadoCuestionario
import com.cadev.mocaapp.feature.cuestionarios.domain.model.ResultadoCuestionario
import com.cadev.mocaapp.feature.cuestionarios.domain.repository.CuestionarioRepository
import com.cadev.mocaapp.feature.notificaciones.data.NotificacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ESTADO DE LOS TESTS Y CUESTIONARIOS
 * 
 * Qué hace:
 * Almacena la lista de tests disponibles, el historial de los completados y el estado 
 * de cada uno (si falta por responder o si ya están los resultados). También guarda 
 * las respuestas temporales mientras el usuario está realizando un test.
 */
data class CuestionarioUiState(
    val cargando: Boolean = false,                      // Si estamos descargando datos
    val cuestionarios: List<Cuestionario> = emptyList(),// Todos los tests que podemos hacer
    val historial: List<Cuestionario> = emptyList(),    // Los tests que ya terminamos
    val estadosCuestionarios: Map<String, EstadoCuestionario> = emptyMap(), // Quién respondió qué
    val cuestionarioActual: Cuestionario? = null,       // El test que estamos haciendo ahora
    val preguntaActual: Int = 0,                        // En qué número de pregunta vamos
    val respuestas: Map<String, String> = emptyMap(),   // Lo que vamos contestando
    val comentarios: Map<String, String> = emptyMap(),  // Comentarios opcionales por pregunta
    val respuestasFoto: Map<String, String> = emptyMap(),// Las fotos que subimos como respuesta
    val subiendoFoto: Boolean = false,                  // Si una imagen se está enviando a la nube
    val enviando: Boolean = false,                      // Si estamos guardando todo el test
    val completado: Boolean = false,                    // Si ya terminamos de responder
    val resultado: ResultadoCuestionario? = null,       // Los puntos y match final
    val respuestasPareja: Map<String, String> = emptyMap(),    // Lo que contestó nuestro novio/a
    val respuestasFotoPareja: Map<String, String> = emptyMap(),// Las fotos que subió nuestro novio/a
    val error: String? = null,                          // Mensaje si algo sale mal
    val creando: Boolean = false,                       // Si estamos inventando un test nuevo
    val creadoExitoso: Boolean = false                  // Si el nuevo test se guardó bien
)

/**
 * GESTOR DE TESTS DE PAREJA
 * 
 * Qué hace:
 * Aquí controlamos todas las dinámicas para que la pareja se conozca mejor. Nos 
 * encargamos de cargar los tests, guardar las respuestas, subir las fotos 
 * y calcular automáticamente el nivel de compatibilidad.
 * 
 * Cómo lo podemos modificar:
 * Si queremos que los resultados se muestren con una animación especial, debemos 
 * avisar a la pantalla cambiando una variable aquí cuando `enviarRespuestas` sea exitoso.
 */
class CuestionarioViewModel(
    private val repository: CuestionarioRepository,
    private val notificacionRepository: NotificacionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CuestionarioUiState())
    val uiState: StateFlow<CuestionarioUiState> = _uiState.asStateFlow()

    /**
     * ESCUCHA EN TIEMPO REAL:
     * Se suscribe a los cuestionarios y sus estados para que si la pareja 
     * responde, aparezca el aviso al instante sin refrescar.
     */
    fun iniciarEscucha(relacionId: String, usuarioId: String, parejaId: String) {
        if (relacionId.isBlank()) return
        
        viewModelScope.launch {
            repository.obtenerCuestionariosFlow(relacionId).collect { lista ->
                _uiState.update { it.copy(cuestionarios = lista) }
                
                // Además escuchamos el estado de cada cuestionario de forma reactiva
                lista.forEach { cuestionario ->
                    launch {
                        repository.obtenerEstadoFlow(cuestionario.id, usuarioId, parejaId).collect { estado ->
                            _uiState.update { current ->
                                val nuevosEstados = current.estadosCuestionarios.toMutableMap()
                                nuevosEstados[cuestionario.id] = estado
                                current.copy(estadosCuestionarios = nuevosEstados)
                            }
                        }
                    }
                }
            }
        }

        // Historial
        viewModelScope.launch {
            repository.obtenerHistorial(relacionId, usuarioId).onSuccess { historial ->
                _uiState.update { it.copy(historial = historial) }
            }
        }
    }

    /**
     * Refresca el estado de los tests para saber si la pareja ha respondido alguno recientemente
     */
    fun refrescarEstados(usuarioId: String, parejaId: String) {
        val lista = _uiState.value.cuestionarios
        if (lista.isEmpty()) return
        viewModelScope.launch {
            repository.obtenerEstadosTodos(lista, usuarioId, parejaId).fold(
                onSuccess = { estados ->
                    _uiState.value = _uiState.value.copy(estadosCuestionarios = estados)
                },
                onFailure = { }
            )
        }
    }

    /**
     * Prepara un test para empezar a ser respondido reiniciando las variables temporales
     */
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

    /**
     * Guarda la respuesta del usuario para una pregunta concreta en la memoria temporal
     */
    fun responderPregunta(preguntaId: String, valor: String) {
        _uiState.update { current ->
            val map = current.respuestas.toMutableMap()
            map[preguntaId] = valor
            current.copy(respuestas = map)
        }
    }

    /**
     * Guarda un comentario opcional para una pregunta
     */
    fun guardarComentario(preguntaId: String, comentario: String) {
        _uiState.update { current ->
            val map = current.comentarios.toMutableMap()
            map[preguntaId] = comentario
            current.copy(comentarios = map)
        }
    }

    /**
     * Sube una imagen de respuesta y la asocia a una pregunta del test
     */
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

    /**
     * Avanza a la siguiente pregunta del cuestionario
     */
    fun siguientePregunta() {
        val total = _uiState.value.cuestionarioActual?.preguntas?.size ?: 0
        val actual = _uiState.value.preguntaActual
        if (actual < total - 1) _uiState.value = _uiState.value.copy(preguntaActual = actual + 1)
    }

    /**
     * Retrocede a la pregunta anterior por si el usuario quiere cambiar su respuesta
     */
    fun preguntaAnterior() {
        val actual = _uiState.value.preguntaActual
        if (actual > 0) _uiState.value = _uiState.value.copy(preguntaActual = actual - 1)
    }

    /**
     * Envía todas las respuestas finales a la base de datos y avisa a la pareja
     */
    fun enviarRespuestas(usuarioId: String, parejaId: String, relacionId: String) {
        val cuestionario = _uiState.value.cuestionarioActual ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(enviando = true) }
            repository.guardarRespuestas(
                cuestionarioId = cuestionario.id,
                usuarioId = usuarioId,
                respuestas = _uiState.value.respuestas,
                respuestasFoto = _uiState.value.respuestasFoto,
                comentarios = _uiState.value.comentarios
            ).fold(
                onSuccess = {
                    val estado = repository.obtenerEstado(
                        cuestionario.id, usuarioId, parejaId
                    ).getOrDefault(EstadoCuestionario.YO_RESPONDÍ)

                    _uiState.update { current ->
                        val estadosActualizados = current.estadosCuestionarios.toMutableMap()
                        estadosActualizados[cuestionario.id] = estado
                        current.copy(estadosCuestionarios = estadosActualizados)
                    }

                    /**
                     * Si ambos han terminado se calculan los resultados y se notifica a los dos
                     */
                    if (estado == EstadoCuestionario.AMBOS) {
                        repository.calcularResultado(
                            cuestionario.id, relacionId, usuarioId, parejaId
                        ).fold(
                            onSuccess = { resultado ->
                                _uiState.update { it.copy(resultado = resultado) }
                            },
                            onFailure = { }
                        )
                        val deepLink = "resultados_cuestionario/${cuestionario.id}"
                        launch {
                            notificacionRepository.incrementarBadge(parejaId, "cuestionarios")
                            notificacionRepository.enviarPush(
                                parejaId = parejaId,
                                titulo   = "¡Resultados listos!",
                                cuerpo   = "Ya puedes ver los resultados de \"${cuestionario.titulo}\"",
                                deepLink = deepLink,
                                tipo     = "cuestionario"
                            )
                        }
                        launch {
                            notificacionRepository.incrementarBadge(usuarioId, "cuestionarios")
                        }
                    } else {
                        /**
                         * Si solo el usuario ha respondido se avisa a la pareja para que también lo haga
                         */
                        launch {
                            notificacionRepository.incrementarBadge(parejaId, "cuestionarios")
                            notificacionRepository.enviarPush(
                                parejaId = parejaId,
                                titulo   = "¡Tu pareja ya respondió!",
                                cuerpo   = "Es tu turno de responder \"${cuestionario.titulo}\"",
                                deepLink = "responder_cuestionario/${cuestionario.id}",
                                tipo     = "cuestionario"
                            )
                        }
                    }

                    _uiState.update { it.copy(completado = true, enviando = false) }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            error = "Error al guardar respuestas",
                            enviando = false
                        )
                    }
                }
            )
        }
    }

    /**
     * Carga las respuestas de ambos y la comparación final para mostrarla en pantalla de forma reactiva
     */
    fun cargarResultado(cuestionarioId: String, usuarioId: String, parejaId: String) {
        // Primero nos aseguramos de tener la información del cuestionario (preguntas, etc)
        // sin resetear las respuestas que ya tengamos.
        viewModelScope.launch {
            if (_uiState.value.cuestionarioActual?.id != cuestionarioId) {
                repository.obtenerCuestionario(cuestionarioId).onSuccess { cuestionario ->
                    _uiState.update { it.copy(cuestionarioActual = cuestionario) }
                }
            }
        }

        // Escuchamos el resultado final (porcentaje, aciertos)
        viewModelScope.launch {
            repository.obtenerResultadoFlow(cuestionarioId).collect { resultado ->
                _uiState.update { it.copy(resultado = resultado) }
            }
        }

        // Escuchamos las respuestas del usuario
        viewModelScope.launch {
            repository.obtenerRespuestasFlow(cuestionarioId, usuarioId).collect { resp ->
                _uiState.update { it.copy(respuestas = resp) }
            }
        }

        // Escuchamos las respuestas de la pareja
        viewModelScope.launch {
            repository.obtenerRespuestasFlow(cuestionarioId, parejaId).collect { resp ->
                _uiState.update { it.copy(respuestasPareja = resp) }
            }
        }

        // Escuchamos las fotos del usuario
        viewModelScope.launch {
            repository.obtenerRespuestasFotoFlow(cuestionarioId, usuarioId).collect { fotos ->
                _uiState.update { current ->
                    val map = current.respuestasFoto.toMutableMap()
                    map.putAll(fotos)
                    current.copy(respuestasFoto = map)
                }
            }
        }

        // Escuchamos las fotos de la pareja
        viewModelScope.launch {
            repository.obtenerRespuestasFotoFlow(cuestionarioId, parejaId).collect { fotos ->
                _uiState.update { it.copy(respuestasFotoPareja = fotos) }
            }
        }
    }

    /**
     * Permite al usuario crear un nuevo test personalizado con sus propias preguntas
     */
    fun crearCuestionario(cuestionario: Cuestionario, parejaId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(creando = true)
            repository.crearCuestionario(cuestionario).fold(
                onSuccess = { nuevo ->
                    _uiState.value = _uiState.value.copy(
                        creando = false,
                        creadoExitoso = true,
                        cuestionarios = emptyList()
                    )
                    /**
                     * Se envía un aviso a la pareja invitándola a responder el test recién creado
                     */
                    launch {
                        notificacionRepository.incrementarBadge(parejaId, "cuestionarios")
                        notificacionRepository.enviarPush(
                            parejaId = parejaId,
                            titulo   = "Nuevo cuestionario",
                            cuerpo   = "Tu pareja creó \"${cuestionario.titulo}\". ¡Respóndelo!",
                            deepLink = "responder_cuestionario/${nuevo.id}",
                            tipo     = "cuestionario"
                        )
                    }
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

    /**
     * Sube una imagen decorativa para una pregunta que se está creando
     */
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

    /**
     * Asegura que siempre existan unos tests básicos de ejemplo en la aplicación
     */
    fun poblarPredefinidos() {
        viewModelScope.launch { repository.poblarPredefinidos() }
    }

    /**
     * Quita el mensaje de error de la pantalla de cuestionarios
     */
    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Reinicia el estado de éxito después de haber creado un test nuevo
     */
    fun resetearCreacion() {
        _uiState.value = _uiState.value.copy(creadoExitoso = false)
    }
}
