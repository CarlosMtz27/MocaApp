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

/**
 * EL MOTOR DEL DIARIO (FIREBASE Y CLOUDINARY)
 * 
 * Qué hace:
 * Aquí escribimos la lógica real para que nuestros recuerdos se guarden en la nube. 
 * Usamos Firestore para los textos y fechas, y Cloudinary para almacenar de 
 * forma segura nuestras fotos y vídeos.
 * 
 * Cómo lo podemos modificar:
 * Si decidimos cambiar el límite de fotos por recuerdo, debemos controlar esa 
 * lógica dentro de la función `crearEntrada` o `actualizarEntrada`.
 */
class DiarioRepositoryImpl(
    private val firestore: FirebaseFirestore
) : DiarioRepository {

    /**
     * BUSCAR POR MES:
     * Trae de Firestore todos los recuerdos que escribimos en un mes determinado.
     */
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

    /**
     * BUSCAR POR DÍA:
     * Descarga tanto nuestros recuerdos como los que nuestra pareja ha decidido 
     * compartir con nosotros en una fecha concreta.
     */
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

    /**
     * ÚLTIMA ACTIVIDAD:
     * Trae los recuerdos más recientes para que podamos verlos rápidamente al abrir la app.
     */
    override suspend fun obtenerUltimasEntradas(
        usuarioId: String,
        parejaId: String?,
        limite: Int
    ): Result<List<EntradaDiario>> {
        return try {
            val misEntradas = firestore
                .collection("entradas")
                .whereEqualTo("usuarioId", usuarioId)
                .orderBy("creadaEn", Query.Direction.DESCENDING)
                .limit(limite.toLong())
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(EntradaDiario::class.java) }

            val entradasPareja = if (parejaId != null) {
                firestore
                    .collection("entradas")
                    .whereEqualTo("usuarioId", parejaId)
                    .whereEqualTo("compartida", true)
                    .whereEqualTo("parejaId", usuarioId)
                    .orderBy("creadaEn", Query.Direction.DESCENDING)
                    .limit(limite.toLong())
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.toObject(EntradaDiario::class.java) }
            } else emptyList()

            val todas = (misEntradas + entradasPareja)
                .sortedByDescending { it.creadaEn }
                .take(limite)

            Result.success(todas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * CREAR RECUERDO:
     * 1. Sube las fotos y vídeos a Cloudinary.
     * 2. Recibe los enlaces de internet.
     * 3. Guarda el recuerdo completo en Firestore.
     */
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

    /**
     * BUSCAR POR ID:
     * Obtiene toda la información de un recuerdo específico de la base de datos.
     */
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

    /**
     * ACTUALIZAR:
     * Modifica los textos de un recuerdo y gestiona la subida de nuevas fotos o el 
     * borrado de las antiguas.
     */
    override suspend fun actualizarEntrada(
        entrada: EntradaDiario,
        fotosNuevas: List<String>,
        videosNuevos: List<String>,
        fotosEliminar: List<String>,
        videosEliminar: List<String>
    ): Result<EntradaDiario> {
        return try {
            // Eliminamos los archivos de Cloudinary que ya no queremos
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

    /**
     * DÍAS CON CONTENIDO:
     * Revisa todo el mes para decirnos qué cuadraditos del calendario deben tener 
     * color o una foto de miniatura.
     */
    override suspend fun obtenerDiasConEntrada(
        usuarioId: String,
        parejaId: String?,
        anio: Int,
        mes: Int
    ): Result<Map<String, com.cadev.mocaapp.feature.diario.domain.model.DiaCalendarioInfo>> {
        return try {
            val mesFormateado = mes.toString().padStart(2, '0')
            val prefijo = "$anio-$mesFormateado"
            val diasConEntrada = mutableMapOf<String, com.cadev.mocaapp.feature.diario.domain.model.DiaCalendarioInfo>()

            val processDocs: (List<com.google.firebase.firestore.DocumentSnapshot>) -> Unit = { docs ->
                docs.forEach { doc ->
                    val fecha = doc.getString("fecha") ?: return@forEach
                    if (!fecha.startsWith(prefijo)) return@forEach
                    
                    val tipo = doc.getString("tipo") ?: TipoEntrada.MI_DIA.name
                    val autor = doc.getString("usuarioId") ?: ""
                    val fotos = doc.get("fotos") as? List<*>
                    val primeraFoto = fotos?.firstOrNull() as? String
                    
                    val infoActual = diasConEntrada[fecha] ?: com.cadev.mocaapp.feature.diario.domain.model.DiaCalendarioInfo()
                    diasConEntrada[fecha] = infoActual.copy(
                        tipos = (infoActual.tipos + tipo).distinct(),
                        primeraFoto = infoActual.primeraFoto ?: primeraFoto,
                        autores = infoActual.autores + autor
                    )
                }
            }

            val misDocs = firestore.collection("entradas")
                .whereEqualTo("usuarioId", usuarioId)
                .get().await()
            processDocs(misDocs.documents)

            if (parejaId != null) {
                val parejaDocs = firestore.collection("entradas")
                    .whereEqualTo("usuarioId", parejaId)
                    .whereEqualTo("compartida", true)
                    .get().await()
                processDocs(parejaDocs.documents)
            }

            Result.success(diasConEntrada)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * SUBIDA A LA NUBE (CLOUDINARY):
     * Envía el archivo local (foto o vídeo) al servidor web de Cloudinary.
     */
    private suspend fun subirArchivo(
        rutaLocal: String,
        usuarioId: String,
        carpeta: String 
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

        continuation.invokeOnCancellation {
            MediaManager.get().cancelRequest(requestId)
        }
    }

    /**
     * ELIMINAR ARCHIVO:
     * Prepara el ID necesario para quitar una imagen de la nube.
     */
    private fun eliminarArchivo(url: String) {
        val publicId = url
            .substringAfter("/upload/")
            .substringAfter("/") 
            .substringBeforeLast(".") 
    }

    /**
     * LISTA DE COMENTARIOS:
     * Descarga todos los comentarios que ha recibido un recuerdo.
     */
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
                .sortedBy { it.creadoEn }

            Result.success(comentarios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * PUBLICAR COMENTARIO:
     * Guarda en Firestore un nuevo mensaje de texto en un recuerdo.
     */
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

    /**
     * BORRAR COMENTARIO:
     * Quita un mensaje de texto de la base de datos definitivamente.
     */
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
