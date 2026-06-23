package com.cadev.mocaapp.feature.perfil.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.auth.domain.model.Usuario
import com.cadev.mocaapp.feature.perfil.domain.repository.PerfilRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * ESTADO DE NUESTRA INFORMACIÓN PERSONAL
 * 
 * Qué hace:
 * Almacena todos los datos de nuestra cuenta y los de nuestra pareja: nombres, 
 * fotos de perfil y cuántos recuerdos hemos escrito cada uno. También guarda 
 * la fecha de nuestro aniversario y controla los mensajes de éxito o error.
 */
data class PerfilUiState(
    val cargando: Boolean = false,              // Si estamos bajando los datos
    val error: String? = null,                  // Mensaje si algo sale mal
    val exitoso: String? = null,                // Mensaje de éxito
    val usuario: Usuario? = null,               // Nuestra información
    val pareja: Usuario? = null,                // Información de nuestro novio/a
    val totalEntradas: Int = 0,                 // Nuestros recuerdos totales
    val diasJuntos: Long = 0,                   // El contador de nuestra historia
    val fechaRelacion: String? = null,          // Día del aniversario
    val guardandoAjuste: Boolean = false,       // Si estamos guardando un cambio
    val ajusteExitoso: Boolean = false,         // Si el cambio se guardó bien
    val entradasPareja: Int = 0                 // Recuerdos totales de nuestra pareja
)

/**
 * GESTOR DE NUESTRO PERFIL Y CONFIGURACIÓN
 * 
 * Qué hace:
 * Se encarga de descargar y actualizar la información de nuestra cuenta. Nos permite 
 * cambiar nuestro nombre, el correo, la clave y la foto. También calcula 
 * automáticamente cuántos días llevamos juntos.
 * 
 * Cómo lo podemos modificar:
 * Si queremos añadir un sistema de "Nivel de usuario" basado en los recuerdos 
 * escritos, debemos añadir esa lógica dentro de la función `cargarPerfil`.
 */
class PerfilViewModel(
    private val repository: PerfilRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    private val formatoFecha = SimpleDateFormat(
        "yyyy-MM-dd", Locale.getDefault()
    )

    private var jobUsuario: kotlinx.coroutines.Job? = null

    /**
     * INICIAR VIGILANCIA:
     * Activa la escucha en tiempo real de nuestro perfil para detectar 
     * vinculaciones o cambios de nombre/foto al instante.
     */
    fun iniciarEscucha(usuarioId: String) {
        if (usuarioId.isBlank()) return
        jobUsuario?.cancel()
        jobUsuario = viewModelScope.launch {
            repository.escucharUsuario(usuarioId).collect { usuario ->
                _uiState.value = _uiState.value.copy(usuario = usuario)
                // Si el usuario detecta que tiene pareja, cargamos los datos extra
                if (usuario?.parejaId != null && usuario.parejaId.isNotBlank()) {
                    cargarDatosExtras(usuarioId, usuario.parejaId)
                }
            }
        }
    }

    /**
     * CARGAR DATOS EXTRAS:
     * Descarga información adicional como el aniversario y el perfil de la pareja.
     */
    private fun cargarDatosExtras(usuarioId: String, parejaId: String) {
        viewModelScope.launch {
            val deferredPareja = async { repository.obtenerPareja(parejaId) }
            val deferredEntradas = async { repository.contarEntradas(usuarioId) }
            val deferredEntradasPareja = async { repository.contarEntradas(parejaId) }
            val deferredFecha = async { repository.obtenerFechaRelacion(usuarioId) }

            deferredPareja.await().onSuccess { pareja ->
                _uiState.value = _uiState.value.copy(pareja = pareja)
            }
            deferredEntradas.await().onSuccess { count ->
                _uiState.value = _uiState.value.copy(totalEntradas = count)
            }
            deferredEntradasPareja.await().onSuccess { count ->
                _uiState.value = _uiState.value.copy(entradasPareja = count)
            }
            deferredFecha.await().onSuccess { fecha ->
                _uiState.value = _uiState.value.copy(
                    fechaRelacion = fecha,
                    diasJuntos = calcularDiasJuntos(fecha)
                )
            }
        }
    }

    /**
     * CARGAR TODO (OBSOLETO pero mantenido para compatibilidad):
     * Ahora usamos iniciarEscucha para tiempo real.
     */
    fun cargarPerfil(usuarioId: String, parejaId: String?) {
        if (_uiState.value.usuario != null &&
            _uiState.value.usuario?.id == usuarioId) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)

            val deferredUsuario = async { repository.obtenerUsuario(usuarioId) }
            val deferredPareja = if (parejaId != null) {
                async { repository.obtenerPareja(parejaId) }
            } else null
            val deferredEntradas = async { repository.contarEntradas(usuarioId) }
            val deferredEntradasPareja = if (parejaId != null) {
                async { repository.contarEntradas(parejaId) }
            } else null
            val deferredFecha = async { repository.obtenerFechaRelacion(usuarioId) }

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

    /**
     * CAMBIAR NOMBRE:
     * Guarda nuestra nueva identidad en la base de datos.
     */
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

    /**
     * CAMBIAR CORREO:
     * Modifica nuestro correo de acceso, pidiéndonos la clave actual por seguridad.
     */
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

    /**
     * CAMBIAR CLAVE:
     * Actualiza nuestra contraseña secreta tras verificar que la escribimos bien dos veces.
     */
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

    /**
     * CAMBIAR FOTO:
     * Sube nuestra nueva foto a internet y la guarda como nuestra imagen oficial.
     */
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

    /**
     * CAMBIAR ANIVERSARIO:
     * Modifica el día que empezó nuestra historia y refresca el contador de días.
     */
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

    /**
     * LIMPIEZA:
     * Borra los avisos viejos para que la pantalla de ajustes se vea limpia de nuevo.
     */
    fun limpiarMensajes() {
        _uiState.value = _uiState.value.copy(
            error = null,
            exitoso = null,
            ajusteExitoso = false
        )
    }

    /**
     * CÁLCULO DE DÍAS:
     * Función interna que resta fechas para saber cuántos días llevamos juntos.
     */
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

    /**
     * TRADUCTOR DE ERRORES:
     * Convierte los mensajes técnicos del servidor en avisos que nosotros entendamos bien.
     */
    private fun traducirError(mensaje: String): String = when {
        "password is invalid" in mensaje ||
                "wrong-password" in mensaje ->
            "Contraseña actual incorrecta"
        "email address is already in use" in mensaje ->
            "Este correo ya está en uso"
        "email address is badly formatted" in mensaje ->
            "Formato de correo inválido"
        "requires-recent-login" in mensaje ->
            "Por seguridad vuelve a iniciar sesión"
        else -> "Ocurrió un error intenta de nuevo"
    }
}
