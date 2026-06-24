package com.cadev.mocaapp.feature.eventos.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.eventos.domain.model.Evento
import com.cadev.mocaapp.core.model.TipoEvento
import com.cadev.mocaapp.feature.eventos.domain.repository.EventoRepository
import com.cadev.mocaapp.feature.notification.EventoWorker
import com.cadev.mocaapp.feature.notificaciones.data.NotificacionRepository
import com.cadev.mocaapp.feature.widgets.eventos.EventoWidgetInfo
import com.cadev.mocaapp.feature.widgets.eventos.EventosWidget
import com.cadev.mocaapp.feature.widgets.eventos.EventosWidgetDataStore
import com.cadev.mocaapp.feature.widgets.eventos.EventosWidgetTransparent
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ESTADO DE LA INFORMACIÓN DE LOS EVENTOS
 * 
 * Qué hace
 * Almacena la lista de citas y planes de la pareja el evento que se está viendo o editando 
 * y los datos temporales del formulario de creación. También controla si la operación de 
 * guardado o borrado ha terminado bien.
 */
data class EventoUiState(
    val cargando: Boolean = false,
    val eventos: List<Evento> = emptyList(),
    val eventoActual: Evento? = null,
    val guardado: Boolean = false,
    val eliminado: Boolean = false,
    val error: String? = null,
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: String = "",
    val hora: String = "12:00",
    val tipo: String = TipoEvento.OTRO.name,
    val recordatorio: Boolean = true,
    val minutosAntes: Int = 60
)

/**
 * GESTOR DE EVENTOS Y CITAS COMPARTIDAS
 * 
 * Qué hace:
 * Aquí organizamos el calendario de nuestra relación. Nos encargamos de 
 * crear nuevos planes, modificar los que ya tenemos y programar recordatorios 
 * automáticos para que a ninguno se nos olvide la cita.
 * 
 * Cómo lo podemos modificar:
 * Si queremos que los recordatorios suenen de forma distinta, debemos modificar 
 * la forma en que lanzamos el `EventoWorker.programar`.
 */
class EventoViewModel(
    private val repository: EventoRepository,
    private val notificacionRepository: NotificacionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventoUiState())
    val uiState: StateFlow<EventoUiState> = _uiState.asStateFlow()

    /**
     * ESCUCHA EN TIEMPO REAL:
     * Se suscribe a los cambios en la base de datos para reflejar nuevos planes al instante.
     */
    fun iniciarEscucha(relacionId: String) {
        if (relacionId.isBlank()) return
        viewModelScope.launch {
            repository.obtenerEventosFlow(relacionId).collect { lista ->
                _uiState.value = _uiState.value.copy(eventos = lista)
            }
        }
    }

    private val hoy: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    /**
     * Descarga todos los eventos que pertenecen a la relación de pareja
     */
    fun cargarEventos(context: Context, relacionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            repository.obtenerEventos(relacionId).fold(
                onSuccess = { eventos ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        eventos = eventos
                    )
                    actualizarWidgets(context)
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

    private fun actualizarWidgets(context: Context) {
        viewModelScope.launch {
            val proximos = eventosProximos().map { 
                EventoWidgetInfo(it.titulo, it.fecha, it.hora, it.id)
            }
            EventosWidgetDataStore.guardarEventos(context, proximos)
            try {
                EventosWidget().updateAll(context)
                EventosWidgetTransparent().updateAll(context)
            } catch (e: Exception) { }
        }
    }

    /**
     * Recupera la información completa de un solo evento por su identificador
     */
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

    /**
     * Registra un nuevo plan en la base de datos y activa la alarma de aviso si es necesario
     */
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
                    /**
                     * Se programa el aviso en el teléfono del usuario
                     */
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
                    /**
                     * Se avisa a la pareja de que hay un nuevo plan compartido
                     */
                    launch {
                        notificacionRepository.enviarPush(
                            parejaId = parejaId,
                            titulo   = "Nuevo evento: ${nuevo.titulo}",
                            cuerpo   = "${nuevo.fecha} a las ${nuevo.hora}",
                            deepLink = "detalle_evento/${nuevo.id}",
                            tipo     = "evento"
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        guardado = true
                    )
                    actualizarWidgets(context)
                    actualizarWidgets(context)
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

    /**
     * Guarda los cambios de un evento ya existente y actualiza su alarma de aviso
     */
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
                    /**
                     * Se borra el aviso viejo y se crea uno nuevo con los datos actualizados
                     */
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
                    actualizarWidgets(context)
                    actualizarWidgets(context)
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

    /**
     * Borra definitivamente un evento y cancela su alarma de aviso
     */
    fun eliminarEvento(context: Context, eventoId: String) {
        viewModelScope.launch {
            repository.eliminarEvento(eventoId).fold(
                onSuccess = {
                    EventoWorker.cancelar(context, eventoId)
                    _uiState.value = _uiState.value.copy(eliminado = true)
                    actualizarWidgets(context)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(error = "Error al eliminar")
                }
            )
        }
    }

    /**
     * Marca un evento como convertido en recuerdo
     */
    fun marcarComoRecuerdo(eventoId: String) {
        viewModelScope.launch {
            repository.obtenerEvento(eventoId).onSuccess { evento ->
                repository.actualizarEvento(evento.copy(convertidoEnRecuerdo = true))
            }
        }
    }

    /**
     * Pospone un evento a una nueva fecha
     */
    fun posponerEvento(eventoId: String, nuevaFecha: String, nuevaHora: String) {
        viewModelScope.launch {
            repository.obtenerEvento(eventoId).onSuccess { evento ->
                val actualizado = evento.copy(
                    pospuesto = true,
                    fechaOriginal = evento.fechaOriginal ?: evento.fecha,
                    fecha = nuevaFecha,
                    hora = nuevaHora
                )
                repository.actualizarEvento(actualizado).onSuccess {
                    cargarEvento(eventoId) // Recargar para actualizar UI
                }
            }
        }
    }

    /**
     * Funciones para actualizar los datos temporales del formulario mientras el usuario escribe
     */
    fun actualizarTitulo(v: String)       { _uiState.value = _uiState.value.copy(titulo = v) }
    fun actualizarDescripcion(v: String)  { _uiState.value = _uiState.value.copy(descripcion = v) }
    fun actualizarFecha(v: String)        { _uiState.value = _uiState.value.copy(fecha = v) }
    fun actualizarHora(v: String)         { _uiState.value = _uiState.value.copy(hora = v) }
    fun actualizarTipo(v: String)         { _uiState.value = _uiState.value.copy(tipo = v) }
    fun toggleRecordatorio()              { _uiState.value = _uiState.value.copy(recordatorio = !_uiState.value.recordatorio) }
    fun actualizarMinutosAntes(v: Int)    { _uiState.value = _uiState.value.copy(minutosAntes = v) }

    /**
     * Devuelve los eventos que todavía no han pasado comparándolos con la fecha de hoy
     */
    fun eventosProximos(): List<Evento> =
        _uiState.value.eventos.filter { it.fecha >= hoy }

    /**
     * Devuelve los eventos antiguos ordenados del más reciente al más viejo
     */
    fun eventosPassados(): List<Evento> =
        _uiState.value.eventos.filter { it.fecha < hoy }.reversed()

    /**
     * Borra todos los textos del formulario para que esté vacío la próxima vez que se use
     */
    fun limpiarFormulario() {
        _uiState.value = _uiState.value.copy(
            titulo = "", descripcion = "", fecha = "", hora = "12:00",
            tipo = TipoEvento.OTRO.name, recordatorio = true,
            minutosAntes = 60, guardado = false, eliminado = false,
            eventoActual = null, error = null
        )
    }

    /**
     * Quita el mensaje de error de la pantalla
     */
    fun limpiarError() { _uiState.value = _uiState.value.copy(error = null) }
}
