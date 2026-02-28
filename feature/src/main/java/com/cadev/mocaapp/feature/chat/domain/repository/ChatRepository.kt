package com.cadev.mocaapp.feature.chat.domain.repository

import com.cadev.mocaapp.feature.chat.domain.model.Mensaje
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    // Escuchar mensajes en tiempo real
    fun escucharMensajes(conversacionId: String): Flow<List<Mensaje>>

    // Enviar mensaje de texto
    suspend fun enviarMensaje(mensaje: Mensaje): Result<Mensaje>

    // Enviar mensaje con media (foto,video,audio)
    suspend fun enviarMensajeConMedia(
        mensaje: Mensaje,
        rutaLocal: String
    ): Result<Mensaje>

    // Marcar mensajes como leídos
    suspend fun marcarComoLeido(
        conversacionId: String,
        usuarioId: String
    ): Result<Unit>

    // Actualizar estado "escribiendo..."
    suspend fun actualizarEscribiendo(
        conversacionId: String,
        usuarioId: String,
        escribiendo: Boolean
    ): Result<Unit>

    // Escuchar si la pareja está escribiendo
    fun escucharEscribiendo(
        conversacionId: String,
        parejaId: String
    ): Flow<Boolean>

    // Agregar reacción a un mensaje
    suspend fun agregarReaccion(
        conversacionId: String,
        mensajeId: String,
        usuarioId: String,
        emoji: String
    ): Result<Unit>

    // Eliminar mensaje
    suspend fun eliminarMensaje(
        conversacionId: String,
        mensajeId: String
    ): Result<Unit>

    // Generar ID de conversación determinístico
    fun obtenerConversacionId(uid1: String, uid2: String): String
}