package com.cadev.mocaapp.feature.chat.ui

import kotlinx.coroutines.tasks.await
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.chat.domain.model.EstadoMensaje
import com.cadev.mocaapp.feature.chat.domain.model.Mensaje
import com.cadev.mocaapp.feature.chat.domain.model.TipoMensaje
import com.cadev.mocaapp.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val mensajes: List<Mensaje> = emptyList(),
    val textoActual: String = "",
    val enviando: Boolean = false,
    val parejaEscribiendo: Boolean = false,
    val error: String? = null,
    val mensajeSeleccionado: Mensaje? = null,  // para reacciones,eliminar
    val mostrarReacciones: Boolean = false
)

class ChatViewModel(
    private val repository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var conversacionId: String = ""
    private var usuarioId: String = ""
    private var jobEscribiendo: Job? = null

    fun inicializar(uid: String, parejaId: String) {
        usuarioId = uid
        conversacionId = repository.obtenerConversacionId(uid, parejaId)

        // Escuchar mensajes en tiempo real
        viewModelScope.launch {
            repository.escucharMensajes(conversacionId).collect { mensajes ->
                _uiState.value = _uiState.value.copy(mensajes = mensajes)
                // Marcar como entregados los mensajes de la pareja
                marcarEntregados(mensajes, uid)
            }
        }

        // Escuchar si la pareja está escribiendo
        viewModelScope.launch {
            repository.escucharEscribiendo(conversacionId, parejaId).collect { escribiendo ->
                _uiState.value = _uiState.value.copy(parejaEscribiendo = escribiendo)
            }
        }

        // Marcar como leídos al abrir
        viewModelScope.launch {
            repository.marcarComoLeido(conversacionId, uid)
        }
    }

    fun actualizarTexto(texto: String) {
        _uiState.value = _uiState.value.copy(textoActual = texto)

        // Notificar "escribiendo..." con debounce de 2 segundos
        jobEscribiendo?.cancel()
        viewModelScope.launch {
            repository.actualizarEscribiendo(conversacionId, usuarioId, true)
            jobEscribiendo = launch {
                delay(2000)
                repository.actualizarEscribiendo(conversacionId, usuarioId, false)
            }
        }
    }

    fun enviarTexto() {
        val texto = _uiState.value.textoActual.trim()
        if (texto.isBlank()) return

        val tipo = if (esEnlace(texto)) TipoMensaje.ENLACE.name
        else TipoMensaje.TEXTO.name

        val mensaje = Mensaje(
            conversacionId = conversacionId,
            remitenteId = usuarioId,
            texto = texto,
            tipo = tipo
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                textoActual = "",
                enviando = true
            )
            // Dejar de escribir
            repository.actualizarEscribiendo(conversacionId, usuarioId, false)

            repository.enviarMensaje(mensaje).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(enviando = false)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        enviando = false,
                        error = "No se pudo enviar el mensaje"
                    )
                }
            )
        }
    }

    fun enviarFoto(rutaLocal: String) {
        enviarMedia(rutaLocal, TipoMensaje.FOTO.name, "📷 Foto")
    }

    fun enviarVideo(rutaLocal: String) {
        enviarMedia(rutaLocal, TipoMensaje.VIDEO.name, "🎥 Video")
    }

    fun enviarAudio(rutaLocal: String, duracion: Int) {
        val mensaje = Mensaje(
            conversacionId = conversacionId,
            remitenteId = usuarioId,
            texto = "🎵 Audio",
            tipo = TipoMensaje.AUDIO.name,
            duracionSegundos = duracion
        )
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(enviando = true)
            repository.enviarMensajeConMedia(mensaje, rutaLocal).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(enviando = false)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        enviando = false,
                        error = "No se pudo enviar el audio"
                    )
                }
            )
        }
    }

    fun seleccionarMensaje(mensaje: Mensaje) {
        _uiState.value = _uiState.value.copy(
            mensajeSeleccionado = mensaje,
            mostrarReacciones = true
        )
    }

    fun cerrarReacciones() {
        _uiState.value = _uiState.value.copy(
            mensajeSeleccionado = null,
            mostrarReacciones = false
        )
    }

    fun reaccionar(emoji: String) {
        val mensaje = _uiState.value.mensajeSeleccionado ?: return
        viewModelScope.launch {
            repository.agregarReaccion(
                conversacionId, mensaje.id, usuarioId, emoji
            )
            cerrarReacciones()
        }
    }

    fun eliminarMensaje(mensajeId: String) {
        viewModelScope.launch {
            repository.eliminarMensaje(conversacionId, mensajeId)
            cerrarReacciones()
        }
    }

    fun marcarComoLeido() {
        viewModelScope.launch {
            repository.marcarComoLeido(conversacionId, usuarioId)
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun enviarMedia(rutaLocal: String, tipo: String, textoPreview: String) {
        val mensaje = Mensaje(
            conversacionId = conversacionId,
            remitenteId = usuarioId,
            texto = textoPreview,
            tipo = tipo
        )
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(enviando = true)
            repository.enviarMensajeConMedia(mensaje, rutaLocal).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(enviando = false)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        enviando = false,
                        error = "No se pudo enviar el archivo"
                    )
                }
            )
        }
    }

    private fun marcarEntregados(mensajes: List<Mensaje>, usuarioId: String) {
        val sinEntregar = mensajes.filter {
            it.remitenteId != usuarioId &&
                    it.estado == EstadoMensaje.ENVIADO.name
        }
        if (sinEntregar.isEmpty()) return

        viewModelScope.launch {
            val batch = com.google.firebase.firestore.FirebaseFirestore.getInstance().batch()
            sinEntregar.forEach { msg ->
                val ref = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("conversaciones")
                    .document(conversacionId)
                    .collection("mensajes")
                    .document(msg.id)
                batch.update(ref, "estado", EstadoMensaje.ENTREGADO.name)
            }
            try { batch.commit().await() } catch (e: Exception) { }
        }
    }

    private fun esEnlace(texto: String): Boolean {
        return texto.startsWith("http://") ||
                texto.startsWith("https://") ||
                texto.contains("www.")
    }

    override fun onCleared() {
        super.onCleared()
        // Limpiar estado de escribiendo al salir
        viewModelScope.launch {
            repository.actualizarEscribiendo(conversacionId, usuarioId, false)
        }
    }
}