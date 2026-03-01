package com.cadev.mocaapp.feature.chat.data.repository

import android.net.Uri
import com.cadev.mocaapp.feature.chat.domain.model.EstadoMensaje
import com.cadev.mocaapp.feature.chat.domain.model.Mensaje
import com.cadev.mocaapp.feature.chat.domain.repository.ChatRepository
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ChatRepositoryImpl(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    override fun obtenerConversacionId(uid1: String, uid2: String): String {
        // Ordenar para que siempre sea el mismo ID sin importar quien inicia
        return listOf(uid1, uid2).sorted().joinToString("_")
    }

    override fun escucharMensajes(
        conversacionId: String
    ): Flow<List<Mensaje>> = callbackFlow {
        val listener = firestore
            .collection("conversaciones")
            .document(conversacionId)
            .collection("mensajes")
            .orderBy("creadoEn", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val mensajes = snapshot?.documents
                    ?.mapNotNull { it.toObject(Mensaje::class.java) }
                    ?: emptyList()
                trySend(mensajes)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun enviarMensaje(
        mensaje: Mensaje
    ): Result<Mensaje> {
        return try {
            val id = firestore
                .collection("conversaciones")
                .document(mensaje.conversacionId)
                .collection("mensajes")
                .document()
                .id

            val mensajeFinal = mensaje.copy(
                id = id,
                estado = EstadoMensaje.ENVIADO.name,
                creadoEn = Timestamp.now()
            )

            firestore
                .collection("conversaciones")
                .document(mensaje.conversacionId)
                .collection("mensajes")
                .document(id)
                .set(mensajeFinal)
                .await()

            // Actualizar metadatos de la conversación
            firestore
                .collection("conversaciones")
                .document(mensaje.conversacionId)
                .set(
                    mapOf(
                        "ultimoMensaje" to mensaje.texto.ifBlank { "📎 Archivo" },
                        "ultimaActividad" to Timestamp.now(),
                        "participantes" to listOf(
                            mensaje.remitenteId,
                            mensaje.conversacionId
                                .replace(mensaje.remitenteId, "")
                                .replace("_", "")
                        )
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                )
                .await()

            Result.success(mensajeFinal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun enviarMensajeConMedia(
        mensaje: Mensaje,
        rutaLocal: String
    ): Result<Mensaje> {
        return try {
            val carpeta = when (mensaje.tipo) {
                "FOTO" -> "fotos"
                "VIDEO" -> "videos"
                "AUDIO", "VOZ" -> "audios"
                else -> "otros"
            }
            val url = subirArchivo(rutaLocal, carpeta)
            val mensajeConMedia = mensaje.copy(mediaUrl = url)
            enviarMensaje(mensajeConMedia)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun marcarComoLeido(
        conversacionId: String,
        usuarioId: String
    ): Result<Unit> {
        return try {
            val mensajes = firestore
                .collection("conversaciones")
                .document(conversacionId)
                .collection("mensajes")
                .whereNotEqualTo("remitenteId", usuarioId)
                .whereEqualTo("estado", EstadoMensaje.ENTREGADO.name)
                .get()
                .await()

            val batch = firestore.batch()
            mensajes.documents.forEach { doc ->
                batch.update(doc.reference, "estado", EstadoMensaje.LEIDO.name)
            }
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun actualizarEscribiendo(
        conversacionId: String,
        usuarioId: String,
        escribiendo: Boolean
    ): Result<Unit> {
        return try {
            firestore
                .collection("typing")
                .document(conversacionId)
                .set(
                    mapOf(usuarioId to escribiendo),
                    com.google.firebase.firestore.SetOptions.merge()
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun escucharEscribiendo(
        conversacionId: String,
        parejaId: String
    ): Flow<Boolean> = callbackFlow {
        val listener = firestore
            .collection("typing")
            .document(conversacionId)
            .addSnapshotListener { snapshot, _ ->
                val escribiendo = snapshot?.getBoolean(parejaId) ?: false
                trySend(escribiendo)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun agregarReaccion(
        conversacionId: String,
        mensajeId: String,
        usuarioId: String,
        emoji: String
    ): Result<Unit> {
        return try {
            firestore
                .collection("conversaciones")
                .document(conversacionId)
                .collection("mensajes")
                .document(mensajeId)
                .update("reacciones.$usuarioId", emoji)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun eliminarMensaje(
        conversacionId: String,
        mensajeId: String
    ): Result<Unit> {
        return try {
            firestore
                .collection("conversaciones")
                .document(conversacionId)
                .collection("mensajes")
                .document(mensajeId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun subirArchivo(
        rutaLocal: String,
        carpeta: String
    ): String = suspendCancellableCoroutine { continuation ->

        // Cloudinary usa "video" para audio Y video
        val resourceType = when (carpeta) {
            "fotos" -> "image"
            "videos", "audios" -> "video"
            else -> "auto"
        }

        val requestId = MediaManager.get()
            .upload(Uri.parse(rutaLocal))
            .option("folder", "chat/$carpeta")
            .option("public_id", UUID.randomUUID().toString())
            .option("resource_type", resourceType)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    if (url != null) continuation.resume(url)
                    else continuation.resumeWithException(Exception("Sin URL"))
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(Exception(error.description))
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(Exception(error.description))
                }
            })
            .dispatch()

        continuation.invokeOnCancellation {
            MediaManager.get().cancelRequest(requestId)
        }
    }
}