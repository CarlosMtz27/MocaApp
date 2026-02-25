package com.cadev.mocaapp.feature.pareja.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.pareja.domain.repository.ParejaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ParejaUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val vinculado: Boolean = false,
    val miCodigo: String = ""
)

class ParejaViewModel(
    private val repository: ParejaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParejaUiState())
    val uiState: StateFlow<ParejaUiState> = _uiState.asStateFlow()

    // Cargamos el codigo propio al entrar a la pantalla
    fun cargarMiCodigo(usuarioId: String) {
        viewModelScope.launch {
            repository.obtenerMiCodigo(usuarioId).fold(
                onSuccess = { codigo ->
                    _uiState.value = _uiState.value.copy(miCodigo = codigo)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        error = "No se pudo cargar tu código"
                    )
                }
            )
        }
    }

    fun vincularPorCodigo(codigo: String, miUsuarioId: String) {
        if (codigo.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Escribe el código de tu pareja"
            )
            return
        }
        if (codigo.length != 6) {
            _uiState.value = _uiState.value.copy(
                error = "El código debe tener 6 caracteres"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)

            repository.vincularPorCodigo(codigo, miUsuarioId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        vinculado = true
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = traducirError(error.message ?: "")
                    )
                }
            )
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun traducirError(mensaje: String): String = when {
        "no encontrado" in mensaje -> "Código incorrecto, verifica con tu pareja"
        "propio código" in mensaje -> "No puedes usar tu propio código"
        "ya fue usado" in mensaje  -> "Este código ya está vinculado a alguien"
        "network" in mensaje       -> "Sin conexión a internet"
        else                       -> "Error al vincular, intenta de nuevo"
    }
}