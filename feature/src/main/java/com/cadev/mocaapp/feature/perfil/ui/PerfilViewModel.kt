package com.cadev.mocaapp.feature.perfil.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.auth.domain.model.Usuario
import com.cadev.mocaapp.feature.perfil.domain.repository.PerfilRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class PerfilUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val exitoso: String? = null,
    val usuario: Usuario? = null,
    val pareja: Usuario? = null,
    val totalEntradas: Int = 0,
    val diasJuntos: Long = 0,
    val fechaRelacion: String? = null,
    //Ajustes
    val guardandoAjuste: Boolean = false,
    val ajusteExitoso: Boolean = false,
    val entradasPareja: Int = 0
)

class PerfilViewModel(
    private val repository: PerfilRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    private val formatoFecha = SimpleDateFormat(
        "yyyy-MM-dd", Locale.getDefault()
    )

    fun cargarPerfil(usuarioId: String, parejaId: String?) {
        //Si ya están cargados, no volver a cargar
        if (_uiState.value.usuario != null &&
            _uiState.value.usuario?.id == usuarioId) return

        android.util.Log.d("PerfilVM", "cargarPerfil uid=$usuarioId parejaId=$parejaId")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)

            //Todas las consultas en paralelo con async
            val deferredUsuario = async { repository.obtenerUsuario(usuarioId) }
            val deferredPareja = if (parejaId != null) {
                async { repository.obtenerPareja(parejaId) }
            } else null
            val deferredEntradas = async { repository.contarEntradas(usuarioId) }
            val deferredEntradasPareja = if (parejaId != null) {
                async { repository.contarEntradas(parejaId) }
            } else null
            val deferredFecha = async { repository.obtenerFechaRelacion(usuarioId) }

            // Esperar resultados
            deferredUsuario.await().fold(
                onSuccess = { usuario ->
                    _uiState.value = _uiState.value.copy(usuario = usuario)
                },
                onFailure = { }
            )

            deferredPareja?.await()?.fold(
                onSuccess = { pareja ->
                    _uiState.value = _uiState.value.copy(pareja = pareja)
                },
                onFailure = { }
            )

            deferredEntradas.await().fold(
                onSuccess = { count ->
                    _uiState.value = _uiState.value.copy(totalEntradas = count)
                },
                onFailure = { }
            )

            deferredEntradasPareja?.await()?.fold(
                onSuccess = { count ->
                    _uiState.value = _uiState.value.copy(entradasPareja = count)
                },
                onFailure = { }
            )

            deferredFecha.await().fold(
                onSuccess = { fecha ->
                    val dias = calcularDiasJuntos(fecha)
                    _uiState.value = _uiState.value.copy(
                        fechaRelacion = fecha,
                        diasJuntos = dias,
                        cargando = false
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(cargando = false)
                }
            )
        }
    }

    fun actualizarNombre(usuarioId: String, nuevoNombre: String) {
        if (nuevoNombre.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "El nombre no puede estar vacío"
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardandoAjuste = true)

            repository.actualizarNombre(usuarioId, nuevoNombre).fold(
                onSuccess = {
                    val usuarioActualizado = _uiState.value.usuario
                        ?.copy(nombre = nuevoNombre)
                    _uiState.value = _uiState.value.copy(
                        guardandoAjuste = false,
                        ajusteExitoso = true,
                        usuario = usuarioActualizado
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        guardandoAjuste = false,
                        error = "No se pudo actualizar el nombre"
                    )
                }
            )
        }
    }

    fun actualizarEmail(
        usuarioId: String,
        nuevoEmail: String,
        passwordActual: String
    ) {
        if (nuevoEmail.isBlank() || passwordActual.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Completa todos los campos"
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardandoAjuste = true)

            repository.actualizarEmail(
                usuarioId, nuevoEmail, passwordActual
            ).fold(
                onSuccess = {
                    val usuarioActualizado = _uiState.value.usuario
                        ?.copy(email = nuevoEmail)
                    _uiState.value = _uiState.value.copy(
                        guardandoAjuste = false,
                        ajusteExitoso = true,
                        usuario = usuarioActualizado
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        guardandoAjuste = false,
                        error = traducirError(it.message ?: "")
                    )
                }
            )
        }
    }

    fun actualizarPassword(
        emailActual: String,
        passwordActual: String,
        nuevoPassword: String,
        confirmarPassword: String
    ) {
        if (nuevoPassword != confirmarPassword) {
            _uiState.value = _uiState.value.copy(
                error = "Las contraseñas no coinciden"
            )
            return
        }
        if (nuevoPassword.length < 6) {
            _uiState.value = _uiState.value.copy(
                error = "La contraseña debe tener al menos 6 caracteres"
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardandoAjuste = true)

            repository.actualizarPassword(
                emailActual, passwordActual, nuevoPassword
            ).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        guardandoAjuste = false,
                        ajusteExitoso = true
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        guardandoAjuste = false,
                        error = traducirError(it.message ?: "")
                    )
                }
            )
        }
    }

    fun actualizarFotoPerfil(usuarioId: String, rutaLocal: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardandoAjuste = true)

            repository.actualizarFotoPerfil(usuarioId, rutaLocal).fold(
                onSuccess = { url ->
                    val usuarioActualizado = _uiState.value.usuario
                        ?.copy(fotoPerfil = url)
                    _uiState.value = _uiState.value.copy(
                        guardandoAjuste = false,
                        usuario = usuarioActualizado
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        guardandoAjuste = false,
                        error = "No se pudo actualizar la foto"
                    )
                }
            )
        }
    }

    fun actualizarFechaRelacion(usuarioId: String, fecha: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardandoAjuste = true)

            repository.actualizarFechaRelacion(usuarioId, fecha).fold(
                onSuccess = {
                    val dias = calcularDiasJuntos(fecha)
                    _uiState.value = _uiState.value.copy(
                        guardandoAjuste = false,
                        ajusteExitoso = true,
                        fechaRelacion = fecha,
                        diasJuntos = dias
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        guardandoAjuste = false,
                        error = "No se pudo actualizar la fecha"
                    )
                }
            )
        }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    fun limpiarMensajes() {
        _uiState.value = _uiState.value.copy(
            error = null,
            exitoso = null,
            ajusteExitoso = false
        )
    }

    private fun calcularDiasJuntos(fecha: String?): Long {
        if (fecha == null) return 0
        return try {
            val inicio = formatoFecha.parse(fecha) ?: return 0
            val hoy = Date()
            val diff = hoy.time - inicio.time
            TimeUnit.MILLISECONDS.toDays(diff)
        } catch (e: Exception) {
            0
        }
    }

    private fun traducirError(mensaje: String): String = when {
        "password is invalid" in mensaje ||
                "wrong-password" in mensaje ->
            "Contraseña actual incorrecta"
        "email address is already in use" in mensaje ->
            "Este correo ya está en uso"
        "email address is badly formatted" in mensaje ->
            "Formato de correo inválido"
        "requires-recent-login" in mensaje ->
            "Por seguridad, vuelve a iniciar sesión"
        else -> "Ocurrió un error, intenta de nuevo"
    }
}