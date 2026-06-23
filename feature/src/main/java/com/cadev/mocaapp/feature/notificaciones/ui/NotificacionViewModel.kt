package com.cadev.mocaapp.feature.notificaciones.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.notificaciones.data.ContadoresBadge
import com.cadev.mocaapp.feature.notificaciones.data.NotificacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * GESTOR DE NOVEDADES
 * 
 * Qué hace:
 * Controla los números rojos (globos de notificación) que vemos en el menú 
 * inferior. Nos avisa en tiempo real cuántas cosas nuevas hay por ver y se 
 * encarga de limpiar esos números cuando entramos a una sección.
 */
class NotificacionViewModel(
    private val repository: NotificacionRepository
) : ViewModel() {

    private val _contadores = MutableStateFlow(ContadoresBadge())
    val contadores: StateFlow<ContadoresBadge> = _contadores.asStateFlow()

    fun iniciar(usuarioId: String) {
        viewModelScope.launch {
            repository.escucharNoLeidos(usuarioId).collect { contadores ->
                _contadores.value = contadores
            }
        }
    }

    fun limpiarChat(usuarioId: String) {
        viewModelScope.launch {
            repository.limpiarBadge(usuarioId, "chat")
        }
    }

    fun limpiarDiario(usuarioId: String) {
        viewModelScope.launch {
            repository.limpiarBadge(usuarioId, "diario")
        }
    }

    fun limpiarCuestionarios(usuarioId: String) {
        viewModelScope.launch {
            repository.limpiarBadge(usuarioId, "cuestionarios")
        }
    }

    fun limpiarNota(usuarioId: String) {
        viewModelScope.launch {
            repository.limpiarBadge(usuarioId, "nota")
        }
    }
}