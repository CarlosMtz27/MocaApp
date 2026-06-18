package com.cadev.mocaapp.feature.chat.ui

import kotlinx.coroutines.tasks.await
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.chat.domain.model.EstadoMensaje
import com.cadev.mocaapp.feature.chat.domain.model.Mensaje
import com.cadev.mocaapp.feature.chat.domain.model.TipoMensaje
import com.cadev.mocaapp.feature.chat.domain.repository.ChatRepository
import com.cadev.mocaapp.feature.notificaciones.data.NotificacionRepository
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
    val mensajeSeleccionado: Mensaje? = null,
    val mostrarReacciones: Boolean = false
)

class ChatViewModel(
    private val repository: ChatRepository,
    private val notificacionRepository: NotificacionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var conversacionId: String = ""
    private var usuarioId: String = ""
    private var parejaId: String = ""
    private var jobEscribiendo: Job? = null
    private var inicializado = false

    fun inicializar(uid: String, pId: String) {
        if (inicializado) return   //evitamos doble inicialización
        inicializado = true

        usuarioId = uid
        parejaId = pId
        conversacionId = repository.obtenerConversacionId(uid, pId)

        viewModelScope.launch {
            try {
                repository.escucharMensajes(conversacionId).collect { mensajes ->
                    _uiState.value = _uiState.value.copy(mensajes = mensajes)
                    marcarEntregados(mensajes, uid)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        viewModelScope.launch {
            try {
                repository.escucharEscribiendo(conversacionId, pId)
                    .collect { escribiendo ->
                        _uiState.value = _uiState.value.copy(
                            parejaEscribiendo = escribiendo
                        )
                    }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun actualizarTexto(texto: String) {
        _uiState.value = _uiState.value.copy(textoActual = texto)
        jobEscribiendo?.cancel()
        viewModelScope.launch {
            try {
                repository.actualizarEscribiendo(conversacionId, usuarioId, true)
                jobEscribiendo = launch {
                    delay(2000)
                    repository.actualizarEscribiendo(conversacionId, usuarioId, false)
                }
            } catch (e: Exception) { }
        }
    }

    fun enviarTexto() {
        val texto = _uiState.value.textoActual.trim()
        if (texto.isBlank()) return

        val tipo = if (esEnlace(texto)) TipoMensaje.ENLACE.name
        else TipoMensaje.TEXTO.name

        val mensaje = Mensaje(
            conversacionId = conversacionId,
            remitenteId    = usuarioId,
            texto          = texto,
            tipo           = tipo
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(textoActual = "", enviando = true)
            try {
                repository.actualizarEscribiendo(conversacionId, usuarioId, false)
            } catch (e: Exception) { }

            repository.enviarMensaje(mensaje).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(enviando = false)
                    launch {
                        notificacionRepository.incrementarBadge(parejaId, "chat")
                        notificacionRepository.enviarPush(
                            parejaId = parejaId,
                            titulo   = "💬 Nuevo mensaje",
                            cuerpo   = texto.take(60),
                            deepLink = "main/chat",
                            tipo     = "chat"
                        )
                    }
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        enviando = false,
                        error    = "No se pudo enviar el mensaje"
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
            conversacionId  = conversacionId,
            remitenteId     = usuarioId,
            texto           = "🎵 Audio",
            tipo            = TipoMensaje.AUDIO.name,
            duracionSegundos = duracion
        )
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(enviando = true)
            repository.enviarMensajeConMedia(mensaje, rutaLocal).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(enviando = false)
                    launch {
                        notificacionRepository.incrementarBadge(parejaId, "chat")
                        notificacionRepository.enviarPush(
                            parejaId = parejaId,
                            titulo   = "💬 Nuevo mensaje de voz",
                            cuerpo   = "Te envió un audio 🎵",
                            deepLink = "main/chat",
                            tipo     = "chat"
                        )
                    }
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        enviando = false,
                        error    = "No se pudo enviar el audio"
                    )
                }
            )
        }
    }

    fun seleccionarMensaje(mensaje: Mensaje) {
        _uiState.value = _uiState.value.copy(
            mensajeSeleccionado = mensaje,
            mostrarReacciones   = true
        )
    }

    fun cerrarReacciones() {
        _uiState.value = _uiState.value.copy(
            mensajeSeleccionado = null,
            mostrarReacciones   = false
        )
    }

    fun reaccionar(emoji: String) {
        val mensaje = _uiState.value.mensajeSeleccionado ?: return
        viewModelScope.launch {
            try {
                repository.agregarReaccion(
                    conversacionId, mensaje.id, usuarioId, emoji
                )
            } catch (e: Exception) { }
            cerrarReacciones()
        }
    }

    fun eliminarMensaje(mensajeId: String) {
        viewModelScope.launch {
            try { repository.eliminarMensaje(conversacionId, mensajeId) }
            catch (e: Exception) { }
            cerrarReacciones()
        }
    }

    fun marcarComoLeido() {
        viewModelScope.launch {
            try { repository.marcarComoLeido(conversacionId, usuarioId) }
            catch (e: Exception) { }
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun enviarMedia(rutaLocal: String, tipo: String, textoPreview: String) {
        val mensaje = Mensaje(
            conversacionId = conversacionId,
            remitenteId    = usuarioId,
            texto          = textoPreview,
            tipo           = tipo
        )
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(enviando = true)
            repository.enviarMensajeConMedia(mensaje, rutaLocal).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(enviando = false)
                    launch {
                        notificacionRepository.incrementarBadge(parejaId, "chat")
                        notificacionRepository.enviarPush(
                            parejaId = parejaId,
                            titulo   = "💬 Nuevo mensaje",
                            cuerpo   = textoPreview,
                            deepLink = "main/chat",
                            tipo     = "chat"
                        )
                    }
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        enviando = false,
                        error    = "No se pudo enviar el archivo"
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
            try {
                val firestore = com.google.firebase.firestore.FirebaseFirestore
                    .getInstance()
                val batch = firestore.batch()
                sinEntregar.forEach { msg ->
                    val ref = firestore
                        .collection("conversaciones")
                        .document(conversacionId)
                        .collection("mensajes")
                        .document(msg.id)
                    batch.update(ref, "estado", EstadoMensaje.ENTREGADO.name)
                }
                batch.commit().await()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun esEnlace(texto: String): Boolean {
        return texto.startsWith("http://") ||
                texto.startsWith("https://") ||
                texto.contains("www.")
    }

    override fun onCleared() {
        super.onCleared()
        jobEscribiendo?.cancel()
        viewModelScope.launch {
            try {
                repository.actualizarEscribiendo(conversacionId, usuarioId, false)
            } catch (e: Exception) { }
        }
    }
}