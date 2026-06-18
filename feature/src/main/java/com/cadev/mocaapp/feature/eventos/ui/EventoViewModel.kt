package com.cadev.mocaapp.feature.eventos.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.eventos.domain.model.Evento
import com.cadev.mocaapp.core.model.TipoEvento
import com.cadev.mocaapp.feature.eventos.domain.repository.EventoRepository
import com.cadev.mocaapp.feature.notification.EventoWorker
import com.cadev.mocaapp.feature.notificaciones.data.NotificacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class EventoUiState(
    val cargando: Boolean = false,
    val eventos: List<Evento> = emptyList(),
    val eventoActual: Evento? = null,
    val guardado: Boolean = false,
    val eliminado: Boolean = false,
    val error: String? = null,
    // Formulario
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: String = "",
    val hora: String = "12:00",
    val tipo: String = TipoEvento.OTRO.name,
    val recordatorio: Boolean = true,
    val minutosAntes: Int = 60
)

class EventoViewModel(
    private val repository: EventoRepository,
    private val notificacionRepository: NotificacionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventoUiState())
    val uiState: StateFlow<EventoUiState> = _uiState.asStateFlow()

    private val hoy: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    fun cargarEventos(relacionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            repository.obtenerEventos(relacionId).fold(
                onSuccess = { eventos ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        eventos = eventos
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = "Error al cargar eventos"
                    )
                }
            )
        }
    }

    fun cargarEvento(eventoId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            repository.obtenerEvento(eventoId).fold(
                onSuccess = { evento ->
                    _uiState.value = _uiState.value.copy(
                        cargando    = false,
                        eventoActual = evento,
                        titulo      = evento.titulo,
                        descripcion = evento.descripcion,
                        fecha       = evento.fecha,
                        hora        = evento.hora,
                        tipo        = evento.tipo,
                        recordatorio = evento.recordatorio,
                        minutosAntes = evento.minutosAntes
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = "Error al cargar evento"
                    )
                }
            )
        }
    }

    fun guardarEvento(
        context: Context,
        usuarioId: String,
        parejaId: String,
        relacionId: String
    ) {
        val estado = _uiState.value
        if (estado.titulo.isBlank() || estado.fecha.isBlank()) {
            _uiState.value = estado.copy(error = "Título y fecha son obligatorios")
            return
        }

        viewModelScope.launch {
            _uiState.value = estado.copy(cargando = true)

            val evento = Evento(
                titulo       = estado.titulo,
                descripcion  = estado.descripcion,
                fecha        = estado.fecha,
                hora         = estado.hora,
                tipo         = estado.tipo,
                creadoPor    = usuarioId,
                relacionId   = relacionId,
                recordatorio = estado.recordatorio,
                minutosAntes = estado.minutosAntes
            )

            repository.crearEvento(evento).fold(
                onSuccess = { nuevo ->
                    // Programar notificación local
                    if (nuevo.recordatorio) {
                        EventoWorker.programar(
                            context      = context,
                            eventoId     = nuevo.id,
                            titulo       = nuevo.titulo,
                            descripcion  = nuevo.descripcion,
                            fecha        = nuevo.fecha,
                            hora         = nuevo.hora,
                            minutosAntes = nuevo.minutosAntes
                        )
                    }
                    // Notificar a la pareja
                    launch {
                        notificacionRepository.enviarPush(
                            parejaId = parejaId,
                            titulo   = "📅 Nuevo evento: ${nuevo.titulo}",
                            cuerpo   = "${nuevo.fecha} a las ${nuevo.hora}",
                            deepLink = "detalle_evento/${nuevo.id}",
                            tipo     = "evento"
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        guardado = true
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = "Error al guardar evento"
                    )
                }
            )
        }
    }

    fun actualizarEvento(context: Context) {
        val estado = _uiState.value
        val original = estado.eventoActual ?: return

        viewModelScope.launch {
            _uiState.value = estado.copy(cargando = true)
            val actualizado = original.copy(
                titulo       = estado.titulo,
                descripcion  = estado.descripcion,
                fecha        = estado.fecha,
                hora         = estado.hora,
                tipo         = estado.tipo,
                recordatorio = estado.recordatorio,
                minutosAntes = estado.minutosAntes
            )
            repository.actualizarEvento(actualizado).fold(
                onSuccess = {
                    EventoWorker.cancelar(context, original.id)
                    if (actualizado.recordatorio) {
                        EventoWorker.programar(
                            context      = context,
                            eventoId     = actualizado.id,
                            titulo       = actualizado.titulo,
                            descripcion  = actualizado.descripcion,
                            fecha        = actualizado.fecha,
                            hora         = actualizado.hora,
                            minutosAntes = actualizado.minutosAntes
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        guardado = true
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = "Error al actualizar"
                    )
                }
            )
        }
    }

    fun eliminarEvento(context: Context, eventoId: String) {
        viewModelScope.launch {
            repository.eliminarEvento(eventoId).fold(
                onSuccess = {
                    EventoWorker.cancelar(context, eventoId)
                    _uiState.value = _uiState.value.copy(eliminado = true)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(error = "Error al eliminar")
                }
            )
        }
    }

    // Formulario
    fun actualizarTitulo(v: String)       { _uiState.value = _uiState.value.copy(titulo = v) }
    fun actualizarDescripcion(v: String)  { _uiState.value = _uiState.value.copy(descripcion = v) }
    fun actualizarFecha(v: String)        { _uiState.value = _uiState.value.copy(fecha = v) }
    fun actualizarHora(v: String)         { _uiState.value = _uiState.value.copy(hora = v) }
    fun actualizarTipo(v: String)         { _uiState.value = _uiState.value.copy(tipo = v) }
    fun toggleRecordatorio()              { _uiState.value = _uiState.value.copy(recordatorio = !_uiState.value.recordatorio) }
    fun actualizarMinutosAntes(v: Int)    { _uiState.value = _uiState.value.copy(minutosAntes = v) }

    fun eventosProximos(): List<Evento> =
        _uiState.value.eventos.filter { it.fecha >= hoy }

    fun eventosPassados(): List<Evento> =
        _uiState.value.eventos.filter { it.fecha < hoy }.reversed()

    fun limpiarFormulario() {
        _uiState.value = _uiState.value.copy(
            titulo = "", descripcion = "", fecha = "", hora = "12:00",
            tipo = TipoEvento.OTRO.name, recordatorio = true,
            minutosAntes = 60, guardado = false, eliminado = false,
            eventoActual = null, error = null
        )
    }

    fun limpiarError() { _uiState.value = _uiState.value.copy(error = null) }
}