package com.cadev.mocaapp.feature.pareja.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.pareja.domain.repository.ParejaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ESTADO DE LA VINCULACIÓN DE PAREJA
 * 
 * Qué hace:
 * Guarda toda la información que necesitamos mientras nos unimos: nuestro 
 * código, si ya estamos vinculados, el ID de la relación y los mensajes de error.
 */
data class ParejaUiState(
    val cargando: Boolean = false,      // Si estamos procesando la unión
    val error: String? = null,          // Mensaje si algo sale mal
    val vinculado: Boolean = false,     // ¡Listo! Ya estamos conectados
    val miCodigo: String = "",          // Nuestro código de 6 letras
    val relacionId: String = "",        // ID de nuestra sala compartida
    val fechaGuardada: Boolean = false  // Si ya elegimos aniversario
)


/**
 * GESTOR DE CONEXIÓN DE PAREJA
 * 
 * Qué hace:
 * Aquí controlamos todo el proceso de unir dos cuentas. Nos encargamos de 
 * mostrar nuestro código, procesar el código de nuestra pareja y guardar 
 * el día exacto en que empezamos para el contador de días.
 * 
 * Cómo lo podemos modificar:
 * Si queremos que el código de vinculación caduque, debemos añadir una 
 * comprobación de tiempo dentro de `vincularPorCodigo`.
 */
class ParejaViewModel(
    private val repository: ParejaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParejaUiState())
    val uiState: StateFlow<ParejaUiState> = _uiState.asStateFlow()

    /**
     * Recupera el código único del usuario para que pueda compartirlo con su pareja
     */
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

    /**
     * Intenta unir a los dos usuarios usando el código secreto proporcionado
     */
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
                onSuccess = { relacionId ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        vinculado = true,
                        relacionId = relacionId
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

    /**
     * Quita el mensaje de error de la pantalla de vinculación
     */
    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Convierte los avisos técnicos del proceso de unión en frases claras para el usuario
     */
    private fun traducirError(mensaje: String): String = when {
        "no encontrado" in mensaje -> "Código incorrecto, verifica con tu pareja"
        "propio código" in mensaje -> "No puedes usar tu propio código"
        "ya fue usado" in mensaje  -> "Este código ya pertenece a una pareja vinculada"
        "network" in mensaje       -> "Sin conexión a internet"
        else                       -> "Error al vincular, intenta de nuevo"
    }

    /**
     * Guarda en la base de datos el día exacto en que comenzó la historia de amor
     */
    fun guardarFechaInicio(relacionId: String, fechaMillis: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)

            repository.guardarFechaInicio(relacionId, fechaMillis).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        fechaGuardada = true
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = "No se pudo guardar la fecha intenta de nuevo"
                    )
                }
            )
        }
    }
}
