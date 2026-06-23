package com.cadev.mocaapp.feature.auth.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ESTE ES EL GESTOR DE IDENTIFICACIÓN
 * 
 * Qué hace:
 * Aquí controlamos todo lo relacionado con entrar en la aplicación o crear una cuenta nueva. 
 * Nos comunicamos con el almacén de datos para comprobar si todo es correcto y avisamos a la pantalla.
 * 
 * Cómo lo podemos modificar:
 * Si queremos añadir una validación extra (ej: que el nombre sea largo), debemos hacerlo dentro de la 
 * función `registrar` antes de lanzar la petición al servidor.
 */
data class AuthUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val exitoso: Boolean = false
)

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    /**
     * Esta variable guarda el estado actual de la pantalla y avisa cuando algo cambia
     */
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Esta función sirve para que un usuario entre en su cuenta usando su correo y clave
     */
    fun login(email: String, password: String) {
        /**
         * Se comprueba que no haya dejado ningún campo vacío antes de continuar
         */
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "Completa todos los campos")
            return
        }

        /**
         * Se inicia la petición de entrada de forma segura
         */
        viewModelScope.launch {
            _uiState.value = AuthUiState(cargando = true)

            val resultado = repository.login(email, password)

            resultado.fold(
                onSuccess = {
                    /**
                     * Si los datos son correctos se marca como exitoso
                     */
                    _uiState.value = AuthUiState(exitoso = true)
                },
                onFailure = { error ->
                    /**
                     * Si algo falla se traduce el mensaje técnico a uno que se entienda bien
                     */
                    _uiState.value = AuthUiState(
                        error = traducirError(error.message ?: "Error desconocido")
                    )
                }
            )
        }
    }

    /**
     * Esta función permite crear una cuenta nueva con nombre correo y contraseña
     */
    fun registrar(email: String, password: String, nombre: String) {
        /**
         * Se verifica que todos los datos necesarios estén escritos
         */
        if (email.isBlank() || password.isBlank() || nombre.isBlank()) {
            _uiState.value = AuthUiState(error = "Completa todos los campos")
            return
        }
        /**
         * Por seguridad se exige que la contraseña tenga una longitud mínima
         */
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

    /**
     * Esta función convierte los mensajes de error técnicos en frases sencillas
     */
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
        else -> "Ocurrió un error intenta de nuevo"
    }

    /**
     * Esta función reinicia el estado para que no aparezcan errores de intentos anteriores
     */
    fun limpiarEstado() {
        _uiState.value = AuthUiState()
    }
}
