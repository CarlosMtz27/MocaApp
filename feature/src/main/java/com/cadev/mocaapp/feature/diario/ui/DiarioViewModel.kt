package com.cadev.mocaapp.feature.diario.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.diario.domain.model.Emocion
import com.cadev.mocaapp.feature.diario.domain.model.EntradaDiario
import com.cadev.mocaapp.feature.diario.domain.model.EtiquetaDiaEspecial
import com.cadev.mocaapp.feature.diario.domain.model.EtiquetaEvento
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import com.cadev.mocaapp.feature.diario.domain.repository.DiarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DiarioUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val entradas: List<EntradaDiario> = emptyList(),
    val diasConEntrada: Map<String, List<String>> = emptyMap(),
    val entradaCreada: Boolean = false,
    // ── Formulario ────────────────────────────────────────────
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
    // Fotos/videos que el usuario eliminó (para borrarlos de Storage)
    val fotosAEliminar: List<String> = emptyList(),
    val videosAEliminar: List<String> = emptyList()
)

class DiarioViewModel(
    private val repository: DiarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiarioUiState())
    val uiState: StateFlow<DiarioUiState> = _uiState.asStateFlow()

    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    //Calendario

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

            repository.obtenerEntradasDelDia(
                usuarioId, parejaId, fecha
            ).fold(
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

    //Formulario

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
            _uiState.value = estado.copy(error = "Agrega un título")
            return
        }

        // Si eligió "Otra", usar el texto personalizado
        val etiquetaFinal = if (
            estado.etiqueta == EtiquetaEvento.PERSONALIZADA.etiqueta ||
            estado.etiqueta == EtiquetaDiaEspecial.PERSONALIZADA.etiqueta
        ) {
            estado.etiquetaPersonalizada
        } else {
            estado.etiqueta
        }

        viewModelScope.launch {
            _uiState.value = estado.copy(cargando = true, error = null)

            val entrada = EntradaDiario(
                usuarioId = usuarioId,
                fecha = fecha,
                tipo = tipo,
                etiqueta = etiquetaFinal,
                titulo = estado.titulo,
                detalles = estado.detalles,
                emociones = estado.emocionesSeleccionadas.map { it.name },
                compartida = estado.compartir,
                parejaId = if (estado.compartir) parejaId else null,
                creadaEn = Date()
            )

            repository.crearEntrada(
                entrada,
                estado.fotosSeleccionadas,
                estado.videosSeleccionados
            ).fold(
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

    //Edición

    fun cargarEntradaParaEditar(entradaId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)

            repository.obtenerEntradaPorId(entradaId).fold(
                onSuccess = { entrada ->
                    // Pre-cargar el formulario con los datos existentes
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
                        // Las fotos/videos existentes van en el estado
                        // pero como URLs remotas, no rutas locales
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

    fun eliminarFotoExistente(url: String) {
        val entrada = _uiState.value.entradaActual ?: return
        // Quitar de la entrada actual y marcar para eliminar de Storage
        _uiState.value = _uiState.value.copy(
            entradaActual = entrada.copy(
                fotos = entrada.fotos.filter { it != url }
            ),
            fotosAEliminar = _uiState.value.fotosAEliminar + url
        )
    }

    fun eliminarVideoExistente(url: String) {
        val entrada = _uiState.value.entradaActual ?: return
        _uiState.value = _uiState.value.copy(
            entradaActual = entrada.copy(
                videos = entrada.videos.filter { it != url }
            ),
            videosAEliminar = _uiState.value.videosAEliminar + url
        )
    }

    fun guardarEdicion(parejaId: String?) {
        val estado = _uiState.value
        val entradaOriginal = estado.entradaActual ?: return

        if (estado.titulo.isBlank()) {
            _uiState.value = estado.copy(error = "Agrega un título")
            return
        }

        val etiquetaFinal = if (
            estado.etiqueta == EtiquetaEvento.PERSONALIZADA.etiqueta ||
            estado.etiqueta == EtiquetaDiaEspecial.PERSONALIZADA.etiqueta
        ) estado.etiquetaPersonalizada else estado.etiqueta

        viewModelScope.launch {
            _uiState.value = estado.copy(cargando = true, error = null)

            // Entrada con los cambios del formulario
            val entradaActualizada = entradaOriginal.copy(
                titulo = estado.titulo,
                detalles = estado.detalles,
                etiqueta = etiquetaFinal,
                emociones = estado.emocionesSeleccionadas.map { it.name },
                compartida = estado.compartir,
                parejaId = if (estado.compartir) parejaId else null
                // fotos y videos se manejan en el repositorio
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
                        cargando = false,
                        entradaActualizada = true
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

    fun limpiarFormulario() {
        _uiState.value = _uiState.value.copy(
            titulo = "",
            detalles = "",
            etiqueta = "",
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

    fun fechaHoy(): String = formatoFecha.format(Date())
}