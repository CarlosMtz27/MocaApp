package com.cadev.mocaapp.feature.estadoanimo.ui

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.estadoanimo.domain.repository.EstadoAnimoRepository
import com.cadev.mocaapp.feature.notificaciones.data.NotificacionRepository
import com.cadev.mocaapp.feature.widgets.estadoanimo.EstadoAnimoWidget
import com.cadev.mocaapp.feature.widgets.estadoanimo.EstadoAnimoWidgetDataStore
import com.cadev.mocaapp.feature.widgets.estadoanimo.EstadoAnimoWidgetTransparent
import kotlinx.coroutines.Job
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

    private var jobEstados: Job? = null

    fun cargarEstados(context: Context, relacionId: String, uidPropio: String, nombrePareja: String) {
        if (relacionId.isBlank()) return
        
        _uiState.value = _uiState.value.copy(nombrePareja = nombrePareja)
        jobEstados?.cancel()
        
        jobEstados = viewModelScope.launch {
            repository.escucharEstados(relacionId, uidPropio).collect { (propio, pareja) ->
                _uiState.value = _uiState.value.copy(
                    emojiPropio = propio?.emoji ?: "",
                    emojiPareja = pareja?.emoji ?: ""
                )
                
                // Forzamos actualización de widgets locales con los nuevos datos
                try {
                    EstadoAnimoWidgetDataStore.guardar(
                        context, 
                        propio, 
                        "Yo", 
                        pareja, 
                        nombrePareja
                    )
                    EstadoAnimoWidget().updateAll(context)
                    EstadoAnimoWidgetTransparent().updateAll(context)
                } catch (e: Exception) { }
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
                
                // Actualización local inmediata
                val estados = repository.obtenerEstados(relacionId, uid)
                EstadoAnimoWidgetDataStore.guardar(context, estados.first, nombreUsuario, estados.second, _uiState.value.nombrePareja)
                EstadoAnimoWidget().updateAll(context)
                EstadoAnimoWidgetTransparent().updateAll(context)
                
                // Enviamos push con el ID de la relación para que el otro móvil sincronice
                notificacionRepository.incrementarBadge(parejaId, "estadoAnimo")
                notificacionRepository.enviarPush(
                    parejaId = parejaId,
                    titulo = "$nombreUsuario cambió su estado",
                    cuerpo = "Entra para ver cómo se siente",
                    deepLink = "main/home",
                    tipo = "estado_animo",
                    extraData = mapOf("relacionId" to relacionId)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.value = _uiState.value.copy(guardando = false)
            }
        }
    }
}
