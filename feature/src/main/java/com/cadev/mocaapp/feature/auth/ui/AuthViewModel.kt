package com.cadev.mocaapp.feature.auth.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estado de la pantalla — todo lo que la UI necesita saber
data class AuthUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val exitoso: Boolean = false
)

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    // StateFlow es como una variable que la UI puede "observar"
    // Cuando cambia, la pantalla se redibuja automáticamente
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        // Validaciones básicas antes de llamar a Firebase
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "Completa todos los campos")
            return
        }

        // viewModelScope: el trabajo se cancela si el usuario
        // sale de la pantalla — evita memory leaks
        viewModelScope.launch {
            _uiState.value = AuthUiState(cargando = true)

            val resultado = repository.login(email, password)

            resultado.fold(
                onSuccess = {
                    _uiState.value = AuthUiState(exitoso = true)
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState(
                        error = traducirError(error.message ?: "Error desconocido")
                    )
                }
            )
        }
    }

    fun registrar(email: String, password: String, nombre: String) {
        if (email.isBlank() || password.isBlank() || nombre.isBlank()) {
            _uiState.value = AuthUiState(error = "Completa todos los campos")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState(error = "La contraseña debe tener al menos 6 caracteres")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(cargando = true)

            val resultado = repository.registrar(email, password, nombre)

            resultado.fold(
                onSuccess = {
                    _uiState.value = AuthUiState(exitoso = true)
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState(
                        error = traducirError(error.message ?: "Error desconocido")
                    )
                }
            )
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // Traduce los errores técnicos de Firebase a mensajes amigables
    private fun traducirError(mensaje: String): String = when {
        "email address is already in use" in mensaje ->
            "Este correo ya está registrado"
        "password is invalid" in mensaje ||
                "no user record" in mensaje ->
            "Correo o contraseña incorrectos"
        "email address is badly formatted" in mensaje ->
            "El formato del correo no es válido"
        "network error" in mensaje ->
            "Sin conexión a internet"
        else -> "Ocurrió un error, intenta de nuevo"
    }

    fun limpiarEstado() {
        _uiState.value = AuthUiState()
    }
}