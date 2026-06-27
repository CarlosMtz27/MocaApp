package com.cadev.mocaapp.feature.chat.ui

import kotlinx.coroutines.tasks.await
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadev.mocaapp.feature.chat.domain.model.EstadoMensaje
import com.cadev.mocaapp.feature.chat.domain.model.Mensaje
import com.cadev.mocaapp.feature.chat.domain.model.TipoMensaje
import com.cadev.mocaapp.feature.chat.domain.model.ReaccionType
import com.cadev.mocaapp.feature.chat.domain.repository.ChatRepository
import com.cadev.mocaapp.feature.notificaciones.data.NotificacionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ESTADO DEL CHAT PRIVADO
 * 
 * Qué hace
 * Guarda la lista de mensajes de la conversación el texto que se está escribiendo 
 * y controla si la pareja está escribiendo en ese momento. También gestiona 
 * los avisos de carga y los errores al enviar mensajes.
 */
data class ChatUiState(
    val mensajes: List<Mensaje> = emptyList(),
    val textoActual: String = "",
    val enviando: Boolean = false,
    val parejaEscribiendo: Boolean = false,
    val error: String? = null,
    val mensajeSeleccionado: Mensaje? = null,
    val mostrarReacciones: Boolean = false
)

/**
 * GESTOR DEL CHAT PRIVADO
 * 
 * Qué hace:
 * Aquí controlamos todo el intercambio de mensajes, fotos y audios entre la pareja. 
 * Nos encargamos de que los mensajes lleguen en tiempo real, de mostrar cuando 
 * el otro está escribiendo y de gestionar las reacciones con dibujos.
 * 
 * Cómo lo podemos modificar:
 * Si queremos añadir un sonido cada vez que enviamos un mensaje, debemos hacerlo 
 * dentro de la función `enviarTexto` cuando el resultado es exitoso.
 */
class ChatViewModel(
    private val repository: ChatRepository,
    private val notificacionRepository: NotificacionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var conversacionId: String = ""
    private var usuarioId: String = ""
    private var usuarioNombre: String = ""
    private var usuarioFoto: String? = null
    private var parejaId: String = ""
    private var jobEscribiendo: Job? = null
    private var inicializado = false

    /**
     * CONEXIÓN INICIAL:
     * Preparamos la conexión con la base de datos para recibir mensajes en tiempo real.
     */
    fun inicializar(uid: String, nombre: String, foto: String?, pId: String) {
        if (inicializado || uid.isBlank() || pId.isBlank()) {
            // Si ya estamos inicializados, actualizamos el nombre/foto por si cambiaron
            if (uid == usuarioId) {
                usuarioNombre = nombre
                usuarioFoto = foto
            }
            return
        }
        inicializado = true

        usuarioId = uid
        usuarioNombre = nombre
        usuarioFoto = foto
        parejaId = pId
        conversacionId = repository.obtenerConversacionId(uid, pId)

        /**
         * Activamos la escucha constante de nuevos mensajes en la conversación.
         */
        viewModelScope.launch {
            try {
                repository.escucharMensajes(conversacionId).collect { mensajes ->
                    _uiState.value = _uiState.value.copy(mensajes = mensajes)
                    marcarEntregados(mensajes, uid)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        /**
         * Vigilamos si nuestra pareja está pulsando teclas en su teléfono.
         */
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

    /**
     * ACTUALIZAR ESCRITURA:
     * Actualiza el texto escrito y avisa a la pareja que estamos escribiendo.
     */
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

    /**
     * ENVIAR TEXTO:
     * Envía el mensaje escrito a nuestra pareja y dispara una notificación push.
     */
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
                    /**
                     * Enviamos el aviso al teléfono de la pareja.
                     */
                    launch {
                        notificacionRepository.incrementarBadge(parejaId, "chat")
                        notificacionRepository.enviarPush(
                            parejaId = parejaId,
                            titulo   = usuarioNombre,
                            cuerpo   = texto.take(60),
                            deepLink = "main/chat",
                            tipo     = "chat",
                            fotoUrl  = usuarioFoto,
                            remitenteId = usuarioId
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

    /**
     * ENVIAR FOTO:
     * Envía una fotografía seleccionada de la galería.
     */
    fun enviarFoto(rutaLocal: String) {
        enviarMedia(rutaLocal, TipoMensaje.FOTO.name, "Foto")
    }

    /**
     * ENVIAR VIDEO:
     * Envía un archivo de vídeo al chat compartido.
     */
    fun enviarVideo(rutaLocal: String) {
        enviarMedia(rutaLocal, TipoMensaje.VIDEO.name, "Video")
    }

    /**
     * ENVIAR AUDIO:
     * Envía una grabación de voz indicando cuántos segundos dura.
     */
    fun enviarAudio(rutaLocal: String, duracion: Int) {
        val mensaje = Mensaje(
            conversacionId  = conversacionId,
            remitenteId     = usuarioId,
            texto           = "Audio",
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
                            titulo   = usuarioNombre,
                            cuerpo   = "Te envió un audio",
                            deepLink = "main/chat",
                            tipo     = "chat",
                            fotoUrl  = usuarioFoto,
                            remitenteId = usuarioId
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

    /**
     * SELECCIONAR MENSAJE:
     * Marca un mensaje para poder añadirle un dibujo de reacción.
     */
    fun seleccionarMensaje(mensaje: Mensaje) {
        _uiState.value = _uiState.value.copy(
            mensajeSeleccionado = mensaje,
            mostrarReacciones   = true
        )
    }

    /**
     * CERRAR MENÚ REACCIONES:
     * Quita el menú de dibujos de reacción.
     */
    fun cerrarReacciones() {
        _uiState.value = _uiState.value.copy(
            mensajeSeleccionado = null,
            mostrarReacciones   = false
        )
    }

    /**
     * REACCIONAR:
     * Añade un dibujo de reacción a un mensaje recibido o enviado.
     */
    fun reaccionar(reaccion: ReaccionType) {
        val mensaje = _uiState.value.mensajeSeleccionado ?: return
        viewModelScope.launch {
            try {
                repository.agregarReaccion(
                    conversacionId, mensaje.id, usuarioId, reaccion.id
                )
            } catch (e: Exception) { }
            cerrarReacciones()
        }
    }

    /**
     * ELIMINAR:
     * Borra un mensaje de la conversación para los dos.
     */
    fun eliminarMensaje(mensajeId: String) {
        viewModelScope.launch {
            try { repository.eliminarMensaje(conversacionId, mensajeId) }
            catch (e: Exception) { }
            cerrarReacciones()
        }
    }

    /**
     * LEER:
     * Avisa a la base de datos que ya vimos todos los mensajes nuevos.
     */
    fun marcarComoLeido() {
        viewModelScope.launch {
            try { repository.marcarComoLeido(conversacionId, usuarioId) }
            catch (e: Exception) { }
        }
    }

    /**
     * LIMPIAR ERROR:
     * Quita el aviso de error de la pantalla del chat.
     */
    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * FUNCIÓN PRIVADA PARA SUBIR ARCHIVOS:
     * Gestiona la subida de archivos pesados como fotos o vídeos.
     */
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
                            titulo   = usuarioNombre,
                            cuerpo   = textoPreview,
                            deepLink = "main/chat",
                            tipo     = "chat",
                            fotoUrl  = usuarioFoto,
                            remitenteId = usuarioId
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

    /**
     * MARCAR ENTREGADOS:
     * Marca automáticamente los mensajes nuevos como entregados cuando los recibimos.
     */
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

    /**
     * COMPROBAR ENLACES:
     * Mira si el texto escrito es una dirección de internet.
     */
    private fun esEnlace(texto: String): Boolean {
        return texto.startsWith("http://") ||
                texto.startsWith("https://") ||
                texto.contains("www.")
    }

    /**
     * LIMPIEZA AL SALIR:
     * Nos aseguramos de avisar que ya no estamos escribiendo cuando salimos del chat.
     */
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

