package com.cadev.mocaapp.feature.diario.data.repository

import android.net.Uri
import com.cadev.mocaapp.feature.diario.domain.model.Comentario
import com.cadev.mocaapp.feature.diario.domain.model.EntradaDiario
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import com.cadev.mocaapp.feature.diario.domain.repository.DiarioRepository
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DiarioRepositoryImpl(
    private val firestore: FirebaseFirestore
) : DiarioRepository {

    override suspend fun obtenerEntradasDelMes(
        usuarioId: String,
        anio: Int,
        mes: Int
    ): Result<List<EntradaDiario>> {
        return try {
            val mesFormateado = mes.toString().padStart(2, '0')
            val prefijo = "$anio-$mesFormateado"

            val snapshot = firestore
                .collection("entradas")
                .whereEqualTo("usuarioId", usuarioId)
                .whereGreaterThanOrEqualTo("fecha", "$prefijo-01")
                .whereLessThanOrEqualTo("fecha", "$prefijo-31")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()

            val entradas = snapshot.documents.mapNotNull {
                it.toObject(EntradaDiario::class.java)
            }

            Result.success(entradas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerEntradasDelDia(
        usuarioId: String,
        parejaId: String?,
        fecha: String
    ): Result<List<EntradaDiario>> {
        return try {
            val entradas = mutableListOf<EntradaDiario>()

            val misEntradas = firestore
                .collection("entradas")
                .whereEqualTo("usuarioId", usuarioId)
                .whereEqualTo("fecha", fecha)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(EntradaDiario::class.java) }

            entradas.addAll(misEntradas)

            if (parejaId != null) {
                val entradasPareja = firestore
                    .collection("entradas")
                    .whereEqualTo("usuarioId", parejaId)
                    .whereEqualTo("fecha", fecha)
                    .whereEqualTo("compartida", true)
                    .whereEqualTo("parejaId", usuarioId)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.toObject(EntradaDiario::class.java) }

                entradas.addAll(entradasPareja)
            }

            Result.success(entradas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun crearEntrada(
        entrada: EntradaDiario,
        fotosLocales: List<String>,
        videosLocales: List<String>
    ): Result<EntradaDiario> {
        return try {
            val urlsFotos = fotosLocales.map {
                subirArchivo(it, entrada.usuarioId, "fotos")
            }
            val urlsVideos = videosLocales.map {
                subirArchivo(it, entrada.usuarioId, "videos")
            }

            val entradaId = firestore.collection("entradas").document().id
            val entradaFinal = entrada.copy(
                id = entradaId,
                fotos = urlsFotos,
                videos = urlsVideos
            )

            firestore
                .collection("entradas")
                .document(entradaId)
                .set(entradaFinal)
                .await()

            Result.success(entradaFinal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerEntradaPorId(
        entradaId: String
    ): Result<EntradaDiario> {
        return try {
            val doc = firestore
                .collection("entradas")
                .document(entradaId)
                .get()
                .await()

            val entrada = doc.toObject(EntradaDiario::class.java)
                ?: return Result.failure(Exception("Entrada no encontrada"))

            Result.success(entrada)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun actualizarEntrada(
        entrada: EntradaDiario,
        fotosNuevas: List<String>,
        videosNuevos: List<String>,
        fotosEliminar: List<String>,
        videosEliminar: List<String>
    ): Result<EntradaDiario> {
        return try {
            // Eliminar archivos de Cloudinary
            fotosEliminar.forEach { url ->
                try { eliminarArchivo(url) } catch (e: Exception) { }
            }
            videosEliminar.forEach { url ->
                try { eliminarArchivo(url) } catch (e: Exception) { }
            }

            val urlsFotosNuevas = fotosNuevas.map {
                subirArchivo(it, entrada.usuarioId, "fotos")
            }
            val urlsVideosNuevos = videosNuevos.map {
                subirArchivo(it, entrada.usuarioId, "videos")
            }

            val entradaActualizada = entrada.copy(
                fotos = entrada.fotos + urlsFotosNuevas,
                videos = entrada.videos + urlsVideosNuevos
            )

            firestore
                .collection("entradas")
                .document(entrada.id)
                .set(entradaActualizada)
                .await()

            Result.success(entradaActualizada)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerDiasConEntrada(
        usuarioId: String,
        parejaId: String?,
        anio: Int,
        mes: Int
    ): Result<Map<String, List<String>>> {
        return try {
            val mesFormateado = mes.toString().padStart(2, '0')
            val prefijo = "$anio-$mesFormateado"
            val diasConEntrada = mutableMapOf<String, MutableList<String>>()

            //Solo un whereEqualTo — no requiere índice compuesto
            firestore.collection("entradas")
                .whereEqualTo("usuarioId", usuarioId)
                .get().await()
                .documents.forEach { doc ->
                    val fecha = doc.getString("fecha") ?: return@forEach
                    if (!fecha.startsWith(prefijo)) return@forEach  // filtro en memoria
                    val tipo = doc.getString("tipo") ?: TipoEntrada.MI_DIA.name
                    diasConEntrada.getOrPut(fecha) { mutableListOf() }.add(tipo)
                }

            if (parejaId != null) {
                firestore.collection("entradas")
                    .whereEqualTo("usuarioId", parejaId)
                    .whereEqualTo("compartida", true)
                    .get().await()
                    .documents.forEach { doc ->
                        val fecha = doc.getString("fecha") ?: return@forEach
                        if (!fecha.startsWith(prefijo)) return@forEach
                        val tipo = doc.getString("tipo") ?: TipoEntrada.MI_DIA.name
                        diasConEntrada.getOrPut(fecha) { mutableListOf() }.add(tipo)
                    }
            }

            Result.success(diasConEntrada)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //Cloudinary: subir archivo
    private suspend fun subirArchivo(
        rutaLocal: String,
        usuarioId: String,
        carpeta: String  // "fotos" o "videos"
    ): String = suspendCancellableCoroutine { continuation ->

        val resourceType = when (carpeta) {
            "fotos" -> "image"
            "videos" -> "video"
            else -> "auto"
        }

        val requestId = MediaManager.get()
            .upload(Uri.parse(rutaLocal))
            .option("folder", "entradas/$usuarioId/$carpeta")
            .option("public_id", UUID.randomUUID().toString())
            .option("resource_type", resourceType)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    if (url != null) {
                        continuation.resume(url)
                    } else {
                        continuation.resumeWithException(
                            Exception("Cloudinary no devolvió URL")
                        )
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(
                        Exception("Error Cloudinary: ${error.description}")
                    )
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(
                        Exception("Upload reprogramado: ${error.description}")
                    )
                }
            })
            .dispatch()

        // Si la coroutine se cancela, cancelamos el upload
        continuation.invokeOnCancellation {
            MediaManager.get().cancelRequest(requestId)
        }
    }

    // Cloudinary: eliminar archivo

    private fun eliminarArchivo(url: String) {
        // Extraemos el public_id de la URL de Cloudinary
        // URL ejemplo: https://res.cloudinary.com/cloud/image/upload/v123/entradas/uid/fotos/uuid.jpg
        val publicId = url
            .substringAfter("/upload/")
            .substringAfter("/")  // quita la versión v123
            .substringBeforeLast(".")  // quita la extensión
    }

    override suspend fun obtenerComentarios(
        entradaId: String
    ): Result<List<Comentario>> {
        return try {
            val snapshot = firestore
                .collection("comentarios")
                .whereEqualTo("entradaId", entradaId)
                .get()
                .await()

            val comentarios = snapshot.documents
                .mapNotNull { it.toObject(Comentario::class.java) }
                .sortedBy { it.creadoEn }  //Ordenamos en memoria

            Result.success(comentarios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun agregarComentario(
        comentario: Comentario
    ): Result<Comentario> {
        return try {
            val id = firestore.collection("comentarios").document().id
            val comentarioFinal = comentario.copy(id = id)

            firestore
                .collection("comentarios")
                .document(id)
                .set(comentarioFinal)
                .await()

            Result.success(comentarioFinal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun eliminarComentario(
        comentarioId: String
    ): Result<Unit> {
        return try {
            firestore
                .collection("comentarios")
                .document(comentarioId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}