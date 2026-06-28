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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.cadev.mocaapp.feature.eventos.domain.model.Evento
import com.cadev.mocaapp.feature.eventos.domain.repository.EventoRepository

enum class OrdenListado { RECIENTE, ANTIGUO }
enum class FiltroListado { TODOS, RECUERDOS, EVENTOS, DIAS }

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
    val fotoUsuario: String = "", // Añadido para mostrar en comentarios
    val relacionId: String = "",
    val ultimasEntradas: List<EntradaDiario> = emptyList(),
    val eventos: List<Evento> = emptyList(),
    val verComoLista: Boolean = false,
    val orden: OrdenListado = OrdenListado.RECIENTE,
    val filtro: FiltroListado = FiltroListado.TODOS
)

class DiarioViewModel(
    private val repository: DiarioRepository,
    private val notificacionRepository: NotificacionRepository,
    private val eventoRepository: EventoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiarioUiState())
    val uiState: StateFlow<DiarioUiState> = _uiState.asStateFlow()

    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val entradasVistasSet = mutableSetOf<String>()

    fun iniciarEscucha(usuarioId: String, parejaId: String?, relacionId: String) {
        _uiState.value = _uiState.value.copy(relacionId = relacionId)
        viewModelScope.launch {
            repository.obtenerEntradasFlow(usuarioId, parejaId).collect { lista ->
                val diasMap = generarDiasConEntrada(lista)
                _uiState.value = _uiState.value.copy(
                    entradas = lista,
                    ultimasEntradas = lista.take(5),
                    diasConEntrada = combinarConEventos(diasMap, _uiState.value.eventos)
                )
            }
        }

        if (relacionId.isNotBlank()) {
            viewModelScope.launch {
                eventoRepository.obtenerEventosFlow(relacionId).collect { lista ->
                    val diasMap = generarDiasConEntrada(_uiState.value.entradas)
                    _uiState.value = _uiState.value.copy(
                        eventos = lista,
                        diasConEntrada = combinarConEventos(diasMap, lista)
                    )
                }
            }
        }
    }

    private fun combinarConEventos(
        diasDiario: Map<String, com.cadev.mocaapp.feature.diario.domain.model.DiaCalendarioInfo>,
        eventos: List<Evento>
    ): Map<String, com.cadev.mocaapp.feature.diario.domain.model.DiaCalendarioInfo> {
        val mapa = diasDiario.toMutableMap()
        eventos.forEach { evento ->
            val info = mapa[evento.fecha] ?: com.cadev.mocaapp.feature.diario.domain.model.DiaCalendarioInfo()
            mapa[evento.fecha] = info.copy(
                tipos = (info.tipos + "EVENTO_${evento.tipo}").distinct()
            )
        }
        return mapa
    }

    private fun generarDiasConEntrada(entradas: List<EntradaDiario>): Map<String, com.cadev.mocaapp.feature.diario.domain.model.DiaCalendarioInfo> {
        val mapa = mutableMapOf<String, com.cadev.mocaapp.feature.diario.domain.model.DiaCalendarioInfo>()
        entradas.forEach { entrada ->
            val info = mapa[entrada.fecha] ?: com.cadev.mocaapp.feature.diario.domain.model.DiaCalendarioInfo()
            mapa[entrada.fecha] = info.copy(
                tipos = (info.tipos + entrada.tipo).distinct(),
                primeraFoto = info.primeraFoto ?: entrada.fotos.firstOrNull(),
                autores = info.autores + entrada.usuarioId
            )
        }
        return mapa
    }

    fun marcarEntradaVista(entradaId: String) {
        entradasVistasSet.add(entradaId)
    }

    fun esEntradaVista(entradaId: String): Boolean {
        return entradaId in entradasVistasSet
    }

    fun actualizarEventosEnCalendario(eventos: List<Evento>) {
        val diasActualizados = generarDiasConEntrada(_uiState.value.entradas).toMutableMap()
        eventos.forEach { evento ->
            val info = diasActualizados[evento.fecha] ?: com.cadev.mocaapp.feature.diario.domain.model.DiaCalendarioInfo()
            diasActualizados[evento.fecha] = info.copy(
                tipos = (info.tipos + "EVENTO_${evento.tipo}").distinct()
            )
        }
        _uiState.value = _uiState.value.copy(
            eventos = eventos,
            diasConEntrada = diasActualizados
        )
    }

    fun cargarNombreUsuario(usuarioId: String) {
        viewModelScope.launch {
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(usuarioId)
                    .get()
                    .await()
                _uiState.value = _uiState.value.copy(
                    nombreUsuario = doc.getString("nombre") ?: "Usuario",
                    fotoUsuario = doc.getString("fotoPerfil") ?: ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(nombreUsuario = "Usuario")
            }
        }
    }

    fun cargarMes(usuarioId: String, parejaId: String?, relacionId: String, anio: Int, mes: Int) { }

    fun toggleVista() {
        _uiState.value = _uiState.value.copy(verComoLista = !_uiState.value.verComoLista)
    }

    fun cambiarOrden(nuevoOrden: OrdenListado) {
        _uiState.value = _uiState.value.copy(orden = nuevoOrden)
    }

    fun cambiarFiltro(nuevoFiltro: FiltroListado) {
        _uiState.value = _uiState.value.copy(filtro = nuevoFiltro)
    }

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

    fun cargarUltimaActividad(usuarioId: String, parejaId: String?) {
        viewModelScope.launch {
            repository.obtenerUltimasEntradas(usuarioId, parejaId, 5).fold(
                onSuccess = { lista ->
                    _uiState.value = _uiState.value.copy(ultimasEntradas = lista)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        error = "No se pudieron cargar los comentarios"
                    )
                }
            )
        }
    }

    fun actualizarTitulo(valor: String) {
        _uiState.value = _uiState.value.copy(titulo = valor)
    }

    fun actualizarDetalles(valor: String) {
        _uiState.value = _uiState.value.copy(detalles = valor)
    }

    fun actualizarEtiqueta(valor: String) {
        _uiState.value = _uiState.value.copy(etiqueta = valor)
    }

    fun actualizarEtiquetaPersonalizada(valor: String) {
        _uiState.value = _uiState.value.copy(etiquetaPersonalizada = valor)
    }

    fun toggleEmocion(emocion: Emocion) {
        val actuales = _uiState.value.emocionesSeleccionadas.toMutableList()
        if (actuales.contains(emocion)) actuales.remove(emocion)
        else actuales.add(emocion)
        _uiState.value = _uiState.value.copy(emocionesSeleccionadas = actuales)
    }

    fun agregarFoto(rutaLocal: String) {
        val fotos = _uiState.value.fotosSeleccionadas.toMutableList()
        fotos.add(rutaLocal)
        _uiState.value = _uiState.value.copy(fotosSeleccionadas = fotos)
    }

    fun eliminarFoto(ruta: String) {
        val fotos = _uiState.value.fotosSeleccionadas.toMutableList()
        fotos.remove(ruta)
        _uiState.value = _uiState.value.copy(fotosSeleccionadas = fotos)
    }

    fun agregarVideo(rutaLocal: String) {
        val videos = _uiState.value.videosSeleccionados.toMutableList()
        videos.add(rutaLocal)
        _uiState.value = _uiState.value.copy(videosSeleccionados = videos)
    }

    fun eliminarVideo(ruta: String) {
        val videos = _uiState.value.videosSeleccionados.toMutableList()
        videos.remove(ruta)
        _uiState.value = _uiState.value.copy(videosSeleccionados = videos)
    }

    fun toggleCompartir() {
        _uiState.value = _uiState.value.copy(compartir = !_uiState.value.compartir)
    }

    fun guardarEntrada(
        usuarioId: String,
        parejaId: String?,
        fecha: String,
        tipo: String = TipoEntrada.MI_DIA.name
    ) {
        val estado = _uiState.value

        // VALIDACIÓN: No se puede publicar en el futuro
        val hoy = fechaHoy()
        if (fecha > hoy) {
            _uiState.value = estado.copy(error = "No puedes publicar momentos en el futuro")
            return
        }

        // VALIDACIÓN DE CAMPOS OBLIGATORIOS
        if (estado.titulo.isBlank()) {
            _uiState.value = estado.copy(error = "El título es obligatorio")
            return
        }
        if (estado.detalles.isBlank()) {
            _uiState.value = estado.copy(error = "Los detalles son obligatorios para guardar el recuerdo")
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
                creadaEn   = Timestamp.now()
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

    fun cargarEntradaParaEditar(entradaId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            repository.escucharEntrada(entradaId).collect { entrada ->
                if (entrada != null) {
                    val emociones = entrada.emociones.mapNotNull { nombre ->
                        try {
                            Emocion.valueOf(nombre)
                        } catch (e: Exception) {
                            null
                        }
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
                } else {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = "No se pudo cargar la entrada"
                    )
                }
            }
        }
    }

    fun eliminarFotoExistente(url: String) {
        val entrada = _uiState.value.entradaActual ?: return
        _uiState.value = _uiState.value.copy(
            entradaActual = entrada.copy(fotos = entrada.fotos.filter { it != url }),
            fotosAEliminar = _uiState.value.fotosAEliminar + url
        )
    }

    fun eliminarVideoExistente(url: String) {
        val entrada = _uiState.value.entradaActual ?: return
        _uiState.value = _uiState.value.copy(
            entradaActual = entrada.copy(videos = entrada.videos.filter { it != url }),
            videosAEliminar = _uiState.value.videosAEliminar + url
        )
    }

    fun guardarEdicion(parejaId: String?) {
        val estado = _uiState.value
        val entradaOriginal = estado.entradaActual ?: return
        
        // VALIDACIÓN DE CAMPOS OBLIGATORIOS
        if (estado.titulo.isBlank()) {
            _uiState.value = estado.copy(error = "El título es obligatorio")
            return
        }
        if (estado.detalles.isBlank()) {
            _uiState.value = estado.copy(error = "Los detalles son obligatorios")
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
                    if (estado.compartir && !parejaId.isNullOrBlank()) {
                        launch {
                            notificacionRepository.enviarPush(
                                parejaId = parejaId,
                                titulo   = "Recuerdo editado",
                                cuerpo   = "Se ha actualizado: ${estado.titulo}",
                                deepLink = "main/calendario",
                                tipo     = "diario"
                            )
                        }
                    }
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

    fun cargarDetalle(entradaId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                cargando = true, comentarios = emptyList(), entradaDetalle = null
            )
            
            // Escuchar cambios en la entrada en tiempo real
            launch {
                repository.escucharEntrada(entradaId).collect { entrada ->
                    _uiState.value = _uiState.value.copy(
                        entradaDetalle = entrada, 
                        cargando = false
                    )
                }
            }
            
            // Escuchar cambios en los comentarios en tiempo real
            launch {
                cargarComentarios(entradaId)
            }
        }
    }

    fun cargarComentarios(entradaId: String) {
        viewModelScope.launch {
            repository.escucharComentarios(entradaId).collect { comentarios ->
                val usuariosUnicos = comentarios.map { it.usuarioId }.distinct()
                val userDataMap = mutableMapOf<String, Pair<String, String>>() // Nombre y Foto
                usuariosUnicos.forEach { uid ->
                    try {
                        val doc = FirebaseFirestore.getInstance()
                            .collection("usuarios").document(uid).get().await()
                        userDataMap[uid] = Pair(
                            doc.getString("nombre") ?: "Usuario",
                            doc.getString("fotoPerfil") ?: ""
                        )
                    } catch (e: Exception) {
                        userDataMap[uid] = Pair("Usuario", "")
                    }
                }
                val comentariosConData = comentarios.map { c ->
                    val data = userDataMap[c.usuarioId]
                    c.copy(
                        nombreUsuario = if (c.nombreUsuario.isBlank()) data?.first ?: "Usuario" else c.nombreUsuario,
                        fotoUsuario = data?.second ?: ""
                    )
                }
                _uiState.value = _uiState.value.copy(
                    comentarios = comentariosConData
                )
            }
        }
    }

    fun actualizarNuevoComentario(valor: String) {
        _uiState.value = _uiState.value.copy(nuevoComentario = valor)
    }

    fun publicarComentario(usuarioId: String, nombreUsuario: String, parejaId: String?) {
        val texto = _uiState.value.nuevoComentario.trim()
        val entradaId = _uiState.value.entradaDetalle?.id ?: return
        if (texto.isBlank()) return

        _uiState.value = _uiState.value.copy(error = null)

        viewModelScope.launch {
            val comentario = Comentario(
                entradaId = entradaId,
                usuarioId = usuarioId,
                nombreUsuario = nombreUsuario,
                texto = texto,
                relacionId = _uiState.value.relacionId,
                creadoEn = Timestamp.now()
            )
            repository.agregarComentario(comentario).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(nuevoComentario = "")
                    // Notificación push para la pareja
                    if (!parejaId.isNullOrBlank() && usuarioId != parejaId) {
                        launch {
                            notificacionRepository.enviarPush(
                                parejaId = parejaId,
                                titulo   = "Nuevo comentario",
                                cuerpo   = "$nombreUsuario comentó en un recuerdo",
                                deepLink = "main/calendario",
                                tipo     = "diario"
                            )
                        }
                    }
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        error = "No se pudo publicar el comentario"
                    )
                }
            )
        }
    }

    fun eliminarComentario(comentarioId: String) {
        val entradaId = _uiState.value.entradaDetalle?.id ?: return
        viewModelScope.launch {
            repository.eliminarComentario(entradaId, comentarioId).fold(
                onSuccess = { cargarComentarios(entradaId) },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        error = "No se pudieron cargar los comentarios"
                    )
                }
            )
        }
    }

    fun fechaHoy(): String = formatoFecha.format(Date())
}
