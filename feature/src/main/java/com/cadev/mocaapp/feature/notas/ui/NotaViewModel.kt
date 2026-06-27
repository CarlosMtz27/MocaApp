package com.cadev.mocaapp.feature.notas.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.notas.domain.model.NotaActual
import com.cadev.mocaapp.feature.notas.domain.repository.NotaRepository
import com.cadev.mocaapp.feature.notas.widget.NotaWidget
import com.cadev.mocaapp.feature.notas.widget.NotaWidgetDataStore
import com.cadev.mocaapp.feature.notificaciones.data.NotificacionRepository
import com.cadev.mocaapp.feature.notas.widget.NotaWidgetTransparent
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.glance.appwidget.updateAll

/**
 * ESTADO DE LAS NOTAS RÁPIDAS
 */
data class NotaUiState(
    val miNota: NotaActual? = null,
    val notaPareja: NotaActual? = null,
    val borrador: String = "",
    val guardando: Boolean = false,
    val error: String? = null,
    val colorTexto: String = "#4A4A4A" // Color por defecto
)

/**
 * GESTOR DE NOTAS COMPARTIDAS (POST-IT)
 * 
 * Qué hace:
 * Aquí controlamos todo lo relacionado con dejarnos mensajes rápidos. Nos 
 * encargamos de sincronizar las notas en tiempo real, actualizar el widget 
 * de la pantalla de inicio y enviar avisos al móvil de nuestra pareja.
 * 
 * Cómo lo podemos modificar:
 * Si queremos que solo se pueda escribir una nota al día, debemos añadir 
 * una validación de fecha dentro de la función `guardarNota`.
 */
class NotaViewModel(
    private val repository: NotaRepository,
    private val notificacionRepository: NotificacionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotaUiState())
    val uiState: StateFlow<NotaUiState> = _uiState.asStateFlow()

    private var miNotaJob: Job? = null
    private var notaParejaJob: Job? = null

    /**
     * Activa la escucha de las notas tanto propias como de la pareja al abrir la sección
     */
    fun iniciar(context: Context, relacionId: String, usuarioId: String, parejaId: String?) {
        if (miNotaJob != null) return

        /**
         * Se vigila si el usuario cambia su propia nota
         */
        miNotaJob = viewModelScope.launch {
            repository.escucharNota(relacionId, usuarioId).collect { nota ->
                _uiState.value = _uiState.value.copy(
                    miNota = nota,
                    borrador = if (!_uiState.value.guardando && _uiState.value.borrador == (_uiState.value.miNota?.texto ?: ""))
                        nota?.texto ?: ""
                    else
                        _uiState.value.borrador
                )
            }
        }

        /**
         * Se vigila si la pareja actualiza la nota que ha escrito
         */
        if (parejaId != null) {
            notaParejaJob = viewModelScope.launch {
                repository.escucharNota(relacionId, parejaId).collect { nota ->
                    _uiState.value = _uiState.value.copy(notaPareja = nota)
                    
                    /**
                     * Se actualiza el widget del escritorio del móvil con la nueva nota de la pareja
                     */
                    NotaWidgetDataStore.guardar(context, nota)
                    try {
                        NotaWidget().updateAll(context)
                        NotaWidgetTransparent().updateAll(context)
                    } catch (e: Exception) { }
                }
            }
        }

        // Cargamos el color del texto guardado
        viewModelScope.launch {
            NotaWidgetDataStore.obtener(context).collect { data ->
                _uiState.value = _uiState.value.copy(colorTexto = data.colorTexto)
            }
        }
    }

    /**
     * CAMBIAR COLOR:
     * Actualiza el color de las letras en el widget.
     */
    fun cambiarColor(context: Context, hex: String) {
        viewModelScope.launch {
            NotaWidgetDataStore.actualizarColor(context, hex)
            NotaWidget().updateAll(context)
            NotaWidgetTransparent().updateAll(context)
        }
    }

    /**
     * Limpia el globo rojo de notificación cuando el usuario lee la nota nueva
     */
    fun limpiarBadge(usuarioId: String) {
        viewModelScope.launch {
            notificacionRepository.limpiarBadge(usuarioId, "nota")
        }
    }

    /**
     * Actualiza el borrador temporal mientras el usuario va escribiendo su mensaje
     */
    fun actualizarBorrador(valor: String) {
        _uiState.value = _uiState.value.copy(borrador = valor)
    }

    /**
     * Guarda el mensaje de forma definitiva y envía un aviso push al teléfono de la pareja
     */
    fun guardarNota(
        context: Context,
        relacionId: String,
        usuarioId: String,
        nombreUsuario: String,
        parejaId: String?
    ) {
        val texto = _uiState.value.borrador
        if (texto == _uiState.value.miNota?.texto) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true)
            
            val nuevaNota = NotaActual(
                texto = texto,
                autorId = usuarioId,
                nombreAutor = nombreUsuario,
                actualizadaEn = Timestamp.now()
            )

            repository.actualizarNota(relacionId, usuarioId, nuevaNota).fold(
                onSuccess = {
                    /**
                     * Se avisa a la pareja de que tiene un mensaje nuevo en su rincón
                     */
                    if (parejaId != null) {
                        launch {
                            val usuarioActual = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                            val miFoto = usuarioActual?.photoUrl?.toString()

                            notificacionRepository.incrementarBadge(parejaId, "nota")
                            notificacionRepository.enviarPush(
                                parejaId = parejaId,
                                titulo = nombreUsuario,
                                cuerpo = texto.take(60),
                                deepLink = "main/notas",
                                tipo = "nota",
                                fotoUrl = miFoto,
                                remitenteId = usuarioId,
                                extraData = mapOf(
                                    "relacionId" to relacionId,
                                    "autorId" to usuarioId
                                )
                            )
                        }
                    }
                    _uiState.value = _uiState.value.copy(guardando = false)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        guardando = false,
                        error = "Error al guardar la nota"
                    )
                }
            )
        }
    }

    /**
     * Borra el contenido de tu propia nota dejando el rincón vacío
     */
    fun eliminarNota(relacionId: String, usuarioId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true)
            repository.eliminarNota(relacionId, usuarioId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(guardando = false, borrador = "")
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(guardando = false, error = "Error al eliminar")
                }
            )
        }
    }

    /**
     * Se detiene la vigilancia de cambios cuando el usuario cierra esta sección
     */
    override fun onCleared() {
        super.onCleared()
        miNotaJob?.cancel()
        notaParejaJob?.cancel()
    }
}
