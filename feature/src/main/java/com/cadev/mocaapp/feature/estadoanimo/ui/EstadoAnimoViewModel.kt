package com.cadev.mocaapp.feature.estadoanimo.ui

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.estadoanimo.domain.repository.EstadoAnimoRepository
import com.cadev.mocaapp.feature.notificaciones.data.NotificacionRepository
import com.cadev.mocaapp.feature.widgets.estadoanimo.EstadoAnimoWidget
import com.cadev.mocaapp.feature.widgets.estadoanimo.EstadoAnimoWidgetDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EstadoAnimoUiState(
    val emojiPropio: String = "",
    val emojiPareja: String = "",
    val nombrePareja: String = "Tu pareja",
    val guardando: Boolean = false
)

class EstadoAnimoViewModel(
    private val repository: EstadoAnimoRepository,
    private val notificacionRepository: NotificacionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EstadoAnimoUiState())
    val uiState: StateFlow<EstadoAnimoUiState> = _uiState.asStateFlow()

    fun cargarEstados(relacionId: String, uidPropio: String, nombrePareja: String) {
        _uiState.value = _uiState.value.copy(nombrePareja = nombrePareja)
        viewModelScope.launch {
            repository.escucharEstados(relacionId, uidPropio).collect { (propio, pareja) ->
                _uiState.value = _uiState.value.copy(
                    emojiPropio = propio?.emoji ?: "",
                    emojiPareja = pareja?.emoji ?: ""
                )
            }
        }
    }

    fun seleccionarEmoji(
        context: Context,
        relacionId: String,
        uid: String,
        nombreUsuario: String,
        parejaId: String,
        emoji: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true)
            try {
                repository.actualizarEstado(relacionId, uid, emoji)
                
                // Actualizar DataStore local para el widget
                val estados = repository.obtenerEstados(relacionId, uid)
                EstadoAnimoWidgetDataStore.guardar(
                    context, 
                    estados.first, 
                    nombreUsuario, 
                    estados.second, 
                    _uiState.value.nombrePareja
                )
                
                // Refrescar widget
                EstadoAnimoWidget().updateAll(context)
                
                // Notificar a la pareja
                notificacionRepository.incrementarBadge(parejaId, "estadoAnimo")
                notificacionRepository.enviarPush(
                    parejaId = parejaId,
                    titulo = "😊 $nombreUsuario cambió su estado",
                    cuerpo = emoji,
                    deepLink = "main/estado_animo",
                    tipo = "estado_animo"
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.value = _uiState.value.copy(guardando = false)
            }
        }
    }
}
