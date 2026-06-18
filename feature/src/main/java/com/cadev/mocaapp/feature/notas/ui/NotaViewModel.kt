package com.cadev.mocaapp.feature.notas.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.notas.domain.model.NotaActual
import com.cadev.mocaapp.feature.notas.domain.repository.NotaRepository
import com.cadev.mocaapp.feature.notas.widget.NotaWidget
import com.cadev.mocaapp.feature.notas.widget.NotaWidgetDataStore
import com.cadev.mocaapp.feature.notificaciones.data.NotificacionRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.glance.appwidget.updateAll

data class NotaUiState(
    val miNota: NotaActual? = null,
    val notaPareja: NotaActual? = null,
    val borrador: String = "",
    val guardando: Boolean = false,
    val error: String? = null
)

class NotaViewModel(
    private val repository: NotaRepository,
    private val notificacionRepository: NotificacionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotaUiState())
    val uiState: StateFlow<NotaUiState> = _uiState.asStateFlow()

    private var miNotaJob: Job? = null
    private var notaParejaJob: Job? = null

    fun iniciar(context: Context, relacionId: String, usuarioId: String, parejaId: String?) {
        if (miNotaJob != null) return

        // Escuchar mi propia nota (la que yo escribo para mi pareja)
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

        // Escuchar la nota de mi pareja (la que mi pareja escribe para mí)
        if (parejaId != null) {
            notaParejaJob = viewModelScope.launch {
                repository.escucharNota(relacionId, parejaId).collect { nota ->
                    _uiState.value = _uiState.value.copy(notaPareja = nota)
                    
                    // IMPORTANTE: El widget siempre debe mostrar la nota de mi PAREJA
                    NotaWidgetDataStore.guardar(context, nota)
                    try {
                        NotaWidget().updateAll(context)
                    } catch (e: Exception) { }
                }
            }
        }
    }

    fun limpiarBadge(usuarioId: String) {
        viewModelScope.launch {
            notificacionRepository.limpiarBadge(usuarioId, "nota")
        }
    }

    fun actualizarBorrador(valor: String) {
        _uiState.value = _uiState.value.copy(borrador = valor)
    }

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
                    // Notificar pareja
                    if (parejaId != null) {
                        launch {
                            notificacionRepository.incrementarBadge(parejaId, "nota")
                            notificacionRepository.enviarPush(
                                parejaId = parejaId,
                                titulo = "📝 Nota actualizada por $nombreUsuario",
                                cuerpo = texto.take(60),
                                deepLink = "main/notas",
                                tipo = "nota",
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

    override fun onCleared() {
        super.onCleared()
        miNotaJob?.cancel()
        notaParejaJob?.cancel()
    }
}
