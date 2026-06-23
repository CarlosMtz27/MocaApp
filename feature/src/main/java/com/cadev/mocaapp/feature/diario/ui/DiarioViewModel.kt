package com.cadev.mocaapp.feature.diario.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.diario.domain.model.Comentario
import com.cadev.mocaapp.feature.diario.domain.model.Emocion
import com.cadev.mocaapp.feature.diario.domain.model.EntradaDiario
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import com.cadev.mocaapp.core.model.TipoEvento
import com.cadev.mocaapp.feature.diario.domain.repository.DiarioRepository
import com.cadev.mocaapp.feature.notificaciones.data.NotificacionRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ESTADO DE LA INFORMACIÓN DEL DIARIO
 * 
 * Qué hace
 * Guarda toda la información que se muestra en las pantallas del diario y el calendario. 
 * Controla si se está cargando información si hay errores los textos que escribe el usuario 
 * y las listas de fotos y comentarios.
 */
data class DiarioUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val entradas: List<EntradaDiario> = emptyList(),
    val diasConEntrada: Map<String, com.cadev.mocaapp.feature.diario.domain.model.DiaCalendarioInfo> = emptyMap(),
    val entradaCreada: Boolean = false,
    val titulo: String = "",
    val detalles: String = "",
    val etiqueta: String = "",
    val etiquetaPersonalizada: String = "",
    val emocionesSeleccionadas: List<Emocion> = emptyList(),
    val fotosSeleccionadas: List<String> = emptyList(),
    val videosSeleccionados: List<String> = emptyList(),
    val compartir: Boolean = false,
    val entradaActual: EntradaDiario? = null,
    val entradaActualizada: Boolean = false,
    val fotosAEliminar: List<String> = emptyList(),
    val videosAEliminar: List<String> = emptyList(),
    val entradaDetalle: EntradaDiario? = null,
    val comentarios: List<Comentario> = emptyList(),
    val nuevoComentario: String = "",
    val nombreUsuario: String = "",
    val ultimasEntradas: List<EntradaDiario> = emptyList()
)

/**
 * GESTOR DE DATOS DEL DIARIO COMPARTIDO
 * 
 * Qué hace:
 * Aquí es donde controlamos todo lo relacionado con los recuerdos. Nos encargamos 
 * de guardar los momentos, subir las fotos a internet, organizar el calendario 
 * y manejar los comentarios que nuestra pareja nos deja.
 * 
 * Cómo lo podemos modificar:
 * Si queremos que los recuerdos tengan un sistema de "votos" o "likes", debemos 
 * añadir esa lógica dentro de este ViewModel y avisar a la pantalla.
 */
class DiarioViewModel(
    private val repository: DiarioRepository,
    private val notificacionRepository: NotificacionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiarioUiState())
    val uiState: StateFlow<DiarioUiState> = _uiState.asStateFlow()

    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val entradasVistasSet = mutableSetOf<String>()

    /**
     * Marca un recuerdo como leído para que no aparezca el aviso de nuevo contenido
     */
    fun marcarEntradaVista(entradaId: String) {
        entradasVistasSet.add(entradaId)
    }

    /**
     * Comprueba si el usuario ya ha visto un recuerdo específico
     */
    fun esEntradaVista(entradaId: String): Boolean {
        return entradaId in entradasVistasSet
    }

    /**
     * Recupera el nombre del usuario desde la base de datos para mostrarlo en los comentarios
     */
    fun cargarNombreUsuario(usuarioId: String) {
        viewModelScope.launch {
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(usuarioId)
                    .get()
                    .await()
                _uiState.value = _uiState.value.copy(
                    nombreUsuario = doc.getString("nombre") ?: "Usuario"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(nombreUsuario = "Usuario")
            }
        }
    }

    /**
     * Carga todos los días de un mes concreto que tienen alguna anotación en el diario
     */
    fun cargarMes(usuarioId: String, parejaId: String?, anio: Int, mes: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            repository.obtenerDiasConEntrada(usuarioId, parejaId, anio, mes).fold(
                onSuccess = { dias ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false, diasConEntrada = dias
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

    /**
     * Recupera todos los recuerdos escritos en una fecha determinada
     */
    fun cargarEntradasDelDia(usuarioId: String, parejaId: String?, fecha: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            repository.obtenerEntradasDelDia(usuarioId, parejaId, fecha).fold(
                onSuccess = { entradas ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false, entradas = entradas
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

    /**
     * Obtiene los recuerdos más recientes para mostrarlos en la pantalla de inicio
     */
    fun cargarUltimaActividad(usuarioId: String, parejaId: String?) {
        viewModelScope.launch {
            repository.obtenerUltimasEntradas(usuarioId, parejaId, 5).fold(
                onSuccess = { lista ->
                    _uiState.value = _uiState.value.copy(ultimasEntradas = lista)
                },
                onFailure = { }
            )
        }
    }

    /**
     * Actualiza el texto del título en el formulario de creación
     */
    fun actualizarTitulo(valor: String) {
        _uiState.value = _uiState.value.copy(titulo = valor)
    }

    /**
     * Actualiza la descripción o detalles en el formulario
     */
    fun actualizarDetalles(valor: String) {
        _uiState.value = _uiState.value.copy(detalles = valor)
    }

    /**
     * Actualiza la categoría o etiqueta seleccionada para el recuerdo
     */
    fun actualizarEtiqueta(valor: String) {
        _uiState.value = _uiState.value.copy(etiqueta = valor)
    }

    /**
     * Guarda el texto de una etiqueta personalizada cuando se elige la opción otros
     */
    fun actualizarEtiquetaPersonalizada(valor: String) {
        _uiState.value = _uiState.value.copy(etiquetaPersonalizada = valor)
    }

    /**
     * Añade o quita una emoción de la lista de sentimientos del día
     */
    fun toggleEmocion(emocion: Emocion) {
        val actuales = _uiState.value.emocionesSeleccionadas.toMutableList()
        if (actuales.contains(emocion)) actuales.remove(emocion)
        else actuales.add(emocion)
        _uiState.value = _uiState.value.copy(emocionesSeleccionadas = actuales)
    }

    /**
     * Añade una foto de la galería al recuerdo que se está creando
     */
    fun agregarFoto(rutaLocal: String) {
        val fotos = _uiState.value.fotosSeleccionadas.toMutableList()
        fotos.add(rutaLocal)
        _uiState.value = _uiState.value.copy(fotosSeleccionadas = fotos)
    }

    /**
     * Quita una foto antes de guardar el recuerdo
     */
    fun eliminarFoto(ruta: String) {
        val fotos = _uiState.value.fotosSeleccionadas.toMutableList()
        fotos.remove(ruta)
        _uiState.value = _uiState.value.copy(fotosSeleccionadas = fotos)
    }

    /**
     * Añade un vídeo de la galería al nuevo recuerdo
     */
    fun agregarVideo(rutaLocal: String) {
        val videos = _uiState.value.videosSeleccionados.toMutableList()
        videos.add(rutaLocal)
        _uiState.value = _uiState.value.copy(videosSeleccionados = videos)
    }

    /**
     * Quita un vídeo antes de guardar
     */
    fun eliminarVideo(ruta: String) {
        val videos = _uiState.value.videosSeleccionados.toMutableList()
        videos.remove(ruta)
        _uiState.value = _uiState.value.copy(videosSeleccionados = videos)
    }

    /**
     * Cambia si el recuerdo será privado o se compartirá con la pareja
     */
    fun toggleCompartir() {
        _uiState.value = _uiState.value.copy(compartir = !_uiState.value.compartir)
    }

    /**
     * Envía toda la información del nuevo recuerdo a la base de datos y sube los archivos
     */
    fun guardarEntrada(
        usuarioId: String,
        parejaId: String?,
        fecha: String,
        tipo: String = TipoEntrada.MI_DIA.name
    ) {
        val estado = _uiState.value
        if (estado.titulo.isBlank()) {
            _uiState.value = estado.copy(error = "Agrega un título")
            return
        }

        val etiquetaFinal = if (estado.etiqueta == TipoEvento.OTRO.name) {
            estado.etiquetaPersonalizada
        } else {
            estado.etiqueta
        }

        viewModelScope.launch {
            _uiState.value = estado.copy(cargando = true, error = null)

            val entrada = EntradaDiario(
                usuarioId  = usuarioId,
                fecha      = fecha,
                tipo       = tipo,
                etiqueta   = etiquetaFinal,
                titulo     = estado.titulo,
                detalles   = estado.detalles,
                emociones  = estado.emocionesSeleccionadas.map { it.name },
                compartida = estado.compartir,
                parejaId   = if (estado.compartir) parejaId else null,
                creadaEn   = Date()
            )

            repository.crearEntrada(
                entrada,
                estado.fotosSeleccionadas,
                estado.videosSeleccionados
            ).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        cargando     = false,
                        entradaCreada = true
                    )
                    /**
                     * Si se comparte se envía un aviso al teléfono de la pareja
                     */
                    if (estado.compartir && !parejaId.isNullOrBlank()) {
                        launch {
                            notificacionRepository.incrementarBadge(parejaId, "diario")
                            notificacionRepository.enviarPush(
                                parejaId = parejaId,
                                titulo   = "Nuevo recuerdo compartido",
                                cuerpo   = estado.titulo,
                                deepLink = "main/calendario",
                                tipo     = "diario"
                            )
                        }
                    }
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error    = "No se pudo guardar la entrada"
                    )
                }
            )
        }
    }

    /**
     * Limpia todos los campos del formulario para dejarlo listo para un nuevo uso
     */
    fun limpiarFormulario() {
        _uiState.value = _uiState.value.copy(
            titulo = "", detalles = "", etiqueta = "",
            etiquetaPersonalizada = "",
            emocionesSeleccionadas = emptyList(),
            fotosSeleccionadas = emptyList(),
            videosSeleccionados = emptyList(),
            compartir = false,
            entradaCreada = false,
            entradaActualizada = false,
            entradaActual = null,
            fotosAEliminar = emptyList(),
            videosAEliminar = emptyList(),
            error = null
        )
    }

    /**
     * Carga un recuerdo existente para poder modificar sus textos fotos o privacidad
     */
    fun cargarEntradaParaEditar(entradaId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            repository.obtenerEntradaPorId(entradaId).fold(
                onSuccess = { entrada ->
                    val emociones = entrada.emociones.mapNotNull { nombre ->
                        try { Emocion.valueOf(nombre) } catch (e: Exception) { null }
                    }
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        entradaActual = entrada,
                        titulo = entrada.titulo,
                        detalles = entrada.detalles,
                        etiqueta = entrada.etiqueta,
                        emocionesSeleccionadas = emociones,
                        compartir = entrada.compartida,
                        entradaActualizada = false
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = "No se pudo cargar la entrada"
                    )
                }
            )
        }
    }

    /**
     * Marca una foto que ya estaba guardada en internet para borrarla definitivamente
     */
    fun eliminarFotoExistente(url: String) {
        val entrada = _uiState.value.entradaActual ?: return
        _uiState.value = _uiState.value.copy(
            entradaActual = entrada.copy(fotos = entrada.fotos.filter { it != url }),
            fotosAEliminar = _uiState.value.fotosAEliminar + url
        )
    }

    /**
     * Marca un vídeo existente para ser borrado del servidor
     */
    fun eliminarVideoExistente(url: String) {
        val entrada = _uiState.value.entradaActual ?: return
        _uiState.value = _uiState.value.copy(
            entradaActual = entrada.copy(videos = entrada.videos.filter { it != url }),
            videosAEliminar = _uiState.value.videosAEliminar + url
        )
    }

    /**
     * Guarda todos los cambios realizados en un recuerdo que ya existía
     */
    fun guardarEdicion(parejaId: String?) {
        val estado = _uiState.value
        val entradaOriginal = estado.entradaActual ?: return
        if (estado.titulo.isBlank()) {
            _uiState.value = estado.copy(error = "Agrega un título")
            return
        }

        val etiquetaFinal = if (estado.etiqueta == TipoEvento.OTRO.name) {
            estado.etiquetaPersonalizada
        } else {
            estado.etiqueta
        }

        viewModelScope.launch {
            _uiState.value = estado.copy(cargando = true, error = null)
            val entradaActualizada = entradaOriginal.copy(
                titulo = estado.titulo,
                detalles = estado.detalles,
                etiqueta = etiquetaFinal,
                emociones = estado.emocionesSeleccionadas.map { it.name },
                compartida = estado.compartir,
                parejaId = if (estado.compartir) parejaId else null
            )
            repository.actualizarEntrada(
                entrada = entradaActualizada,
                fotosNuevas = estado.fotosSeleccionadas,
                videosNuevos = estado.videosSeleccionados,
                fotosEliminar = estado.fotosAEliminar,
                videosEliminar = estado.videosAEliminar
            ).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        cargando = false, entradaActualizada = true
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = "No se pudo guardar los cambios"
                    )
                }
            )
        }
    }

    /**
     * Recupera toda la información de un recuerdo incluyendo sus comentarios
     */
    fun cargarDetalle(entradaId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                cargando = true, comentarios = emptyList(), entradaDetalle = null
            )
            repository.obtenerEntradaPorId(entradaId).fold(
                onSuccess = { entrada ->
                    _uiState.value = _uiState.value.copy(
                        entradaDetalle = entrada, cargando = false
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        cargando = false, error = "No se pudo cargar la entrada"
                    )
                }
            )
            cargarComentarios(entradaId)
        }
    }

    /**
     * Busca y organiza todos los comentarios que ha recibido un recuerdo específico
     */
    fun cargarComentarios(entradaId: String) {
        viewModelScope.launch {
            repository.obtenerComentarios(entradaId).fold(
                onSuccess = { comentarios ->
                    val usuariosUnicos = comentarios.map { it.usuarioId }.distinct()
                    val nombresMap = mutableMapOf<String, String>()
                    usuariosUnicos.forEach { uid ->
                        try {
                            val doc = FirebaseFirestore.getInstance()
                                .collection("usuarios").document(uid).get().await()
                            nombresMap[uid] = doc.getString("nombre") ?: "Usuario"
                        } catch (e: Exception) { nombresMap[uid] = "Usuario" }
                    }
                    val comentariosConNombre = comentarios.map { c ->
                        if (c.nombreUsuario.isBlank())
                            c.copy(nombreUsuario = nombresMap[c.usuarioId] ?: "Usuario")
                        else c
                    }
                    _uiState.value = _uiState.value.copy(
                        comentarios = comentariosConNombre
                    )
                },
                onFailure = { }
            )
        }
    }

    /**
     * Actualiza el texto que el usuario está escribiendo en el cuadro de comentarios
     */
    fun actualizarNuevoComentario(valor: String) {
        _uiState.value = _uiState.value.copy(nuevoComentario = valor)
    }

    /**
     * Publica un nuevo comentario en el recuerdo actual
     */
    fun publicarComentario(usuarioId: String, nombreUsuario: String) {
        val texto = _uiState.value.nuevoComentario.trim()
        val entradaId = _uiState.value.entradaDetalle?.id ?: return
        if (texto.isBlank()) return

        viewModelScope.launch {
            val comentario = Comentario(
                entradaId = entradaId,
                usuarioId = usuarioId,
                nombreUsuario = nombreUsuario,
                texto = texto,
                creadoEn = Date()
            )
            repository.agregarComentario(comentario).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(nuevoComentario = "")
                    cargarComentarios(entradaId)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        error = "No se pudo publicar el comentario"
                    )
                }
            )
        }
    }

    /**
     * Borra un comentario que el usuario ha escrito previamente
     */
    fun eliminarComentario(comentarioId: String) {
        val entradaId = _uiState.value.entradaDetalle?.id ?: return
        viewModelScope.launch {
            repository.eliminarComentario(comentarioId).fold(
                onSuccess = { cargarComentarios(entradaId) },
                onFailure = { }
            )
        }
    }

    /**
     * Función que devuelve la fecha actual en un formato que la base de datos entiende
     */
    fun fechaHoy(): String = formatoFecha.format(Date())
}
