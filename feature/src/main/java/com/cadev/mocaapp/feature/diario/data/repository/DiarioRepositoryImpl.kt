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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * EL MOTOR DEL DIARIO (FIREBASE Y CLOUDINARY)
 * 
 * Qué hace:
 * Aquí escribimos la lógica real para que nuestros recuerdos se guarden en la nube. 
 * Usamos Firestore para los textos y fechas, y Cloudinary para almacenar de 
 * forma segura nuestras fotos y vídeos.
 */
class DiarioRepositoryImpl(
    private val firestore: FirebaseFirestore
) : DiarioRepository {

    /**
     * TRAER TODOS (TIEMPO REAL):
     * Combina vuestros recuerdos y los compartidos por tu pareja en un flujo continuo.
     */
    override fun obtenerEntradasFlow(usuarioId: String, parejaId: String?): Flow<List<EntradaDiario>> {
        val misEntradasFlow = callbackFlow {
            val listener = firestore.collection("entradas")
                .whereEqualTo("usuarioId", usuarioId)
                .addSnapshotListener { snapshot, _ ->
                    val list = snapshot?.documents?.mapNotNull { mapToEntradaDiario(it) } ?: emptyList()
                    trySend(list)
                }
            awaitClose { listener.remove() }
        }

        val entradasParejaFlow = callbackFlow {
            val listener = firestore.collection("entradas")
                .whereEqualTo("parejaId", usuarioId)
                .whereEqualTo("compartida", true)
                .addSnapshotListener { snapshot, _ ->
                    val list = snapshot?.documents?.mapNotNull { mapToEntradaDiario(it) } ?: emptyList()
                    trySend(list)
                }
            awaitClose { listener.remove() }
        }

        return combine(misEntradasFlow, entradasParejaFlow) { mis, pareja ->
            (mis + pareja).sortedByDescending { it.creadaEn }
        }
    }

    /**
     * FUNCIÓN AUXILIAR PARA MAPEAR DOCUMENTOS DE FIREBASE A NUESTRO MODELO
     */
    private fun mapToEntradaDiario(doc: com.google.firebase.firestore.DocumentSnapshot): EntradaDiario? {
        return try {
            EntradaDiario(
                id = doc.id,
                usuarioId = doc.getString("usuarioId") ?: "",
                fecha = doc.getString("fecha") ?: "",
                tipo = doc.getString("tipo") ?: TipoEntrada.MI_DIA.name,
                etiqueta = doc.getString("etiqueta") ?: "",
                titulo = doc.getString("titulo") ?: "",
                detalles = doc.getString("detalles") ?: "",
                emociones = (doc.get("emociones") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                fotos = (doc.get("fotos") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                videos = (doc.get("videos") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                compartida = doc.getBoolean("compartida") ?: false,
                parejaId = doc.getString("parejaId"),
                creadaEn = doc.getTimestamp("creadaEn") ?: com.google.firebase.Timestamp.now()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

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
                .get()
                .await()

            val misEntradas = snapshot.documents.mapNotNull { mapToEntradaDiario(it) }
                .filter { it.fecha.startsWith(prefijo) }

            val entradasCompartidasConmigo = firestore
                .collection("entradas")
                .whereEqualTo("parejaId", usuarioId)
                .whereEqualTo("compartida", true)
                .get()
                .await()
                .documents
                .mapNotNull { mapToEntradaDiario(it) }
                .filter { it.fecha.startsWith(prefijo) }

            val todas = (misEntradas + entradasCompartidasConmigo).sortedByDescending { it.fecha }
            Result.success(todas)
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
            val misEntradas = firestore
                .collection("entradas")
                .whereEqualTo("usuarioId", usuarioId)
                .whereEqualTo("fecha", fecha)
                .get()
                .await()
                .documents
                .mapNotNull { mapToEntradaDiario(it) }

            val entradasPareja = if (parejaId != null) {
                firestore
                    .collection("entradas")
                    .whereEqualTo("usuarioId", parejaId)
                    .whereEqualTo("fecha", fecha)
                    .whereEqualTo("compartida", true)
                    .whereEqualTo("parejaId", usuarioId)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { mapToEntradaDiario(it) }
            } else emptyList()

            Result.success(misEntradas + entradasPareja)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerUltimasEntradas(
        usuarioId: String,
        parejaId: String?,
        limite: Int
    ): Result<List<EntradaDiario>> {
        return try {
            val misEntradas = firestore
                .collection("entradas")
                .whereEqualTo("usuarioId", usuarioId)
                .get()
                .await()
                .documents
                .mapNotNull { mapToEntradaDiario(it) }

            val entradasPareja = firestore
                .collection("entradas")
                .whereEqualTo("parejaId", usuarioId)
                .whereEqualTo("compartida", true)
                .get()
                .await()
                .documents
                .mapNotNull { mapToEntradaDiario(it) }

            val todas = (misEntradas + entradasPareja)
                .sortedByDescending { it.creadaEn }
                .take(limite)

            Result.success(todas)
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
            val urlsFotos = fotosLocales.map { subirArchivo(it, entrada.usuarioId, "fotos") }
            val urlsVideos = videosLocales.map { subirArchivo(it, entrada.usuarioId, "videos") }

            val entradaId = firestore.collection("entradas").document().id
            val entradaFinal = entrada.copy(id = entradaId, fotos = urlsFotos, videos = urlsVideos)

            firestore.collection("entradas").document(entradaId).set(entradaFinal).await()
            Result.success(entradaFinal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun escucharEntrada(entradaId: String): Flow<EntradaDiario?> = callbackFlow {
        val listener = firestore.collection("entradas").document(entradaId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val entrada = snapshot?.let { mapToEntradaDiario(it) }
                trySend(entrada)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun actualizarEntrada(
        entrada: EntradaDiario,
        fotosNuevas: List<String>,
        videosNuevos: List<String>,
        fotosEliminar: List<String>,
        videosEliminar: List<String>
    ): Result<EntradaDiario> {
        return try {
            fotosEliminar.forEach { try { eliminarArchivo(it) } catch (e: Exception) { } }
            videosEliminar.forEach { try { eliminarArchivo(it) } catch (e: Exception) { } }

            val urlsFotosNuevas = fotosNuevas.map { subirArchivo(it, entrada.usuarioId, "fotos") }
            val urlsVideosNuevos = videosNuevos.map { subirArchivo(it, entrada.usuarioId, "videos") }

            val entradaActualizada = entrada.copy(
                fotos = entrada.fotos + urlsFotosNuevas,
                videos = entrada.videos + urlsVideosNuevos
            )

            firestore.collection("entradas").document(entrada.id).set(entradaActualizada).await()
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
    ): Result<Map<String, com.cadev.mocaapp.feature.diario.domain.model.DiaCalendarioInfo>> {
        return try {
            val mesFormateado = mes.toString().padStart(2, '0')
            val prefijo = "$anio-$mesFormateado"
            val diasConEntrada = mutableMapOf<String, com.cadev.mocaapp.feature.diario.domain.model.DiaCalendarioInfo>()

            val processDocs: (List<com.google.firebase.firestore.DocumentSnapshot>) -> Unit = { docs ->
                docs.forEach { doc ->
                    val fecha = doc.getString("fecha") ?: return@forEach
                    if (!fecha.startsWith(prefijo)) return@forEach
                    
                    val entrada = mapToEntradaDiario(doc) ?: return@forEach
                    val infoActual = diasConEntrada[fecha] ?: com.cadev.mocaapp.feature.diario.domain.model.DiaCalendarioInfo()
                    diasConEntrada[fecha] = infoActual.copy(
                        tipos = (infoActual.tipos + entrada.tipo).distinct(),
                        primeraFoto = infoActual.primeraFoto ?: entrada.fotos.firstOrNull(),
                        autores = infoActual.autores + entrada.usuarioId
                    )
                }
            }

            val misDocs = firestore.collection("entradas").whereEqualTo("usuarioId", usuarioId).get().await()
            processDocs(misDocs.documents)

            val parejaDocs = firestore.collection("entradas").whereEqualTo("parejaId", usuarioId).whereEqualTo("compartida", true).get().await()
            processDocs(parejaDocs.documents)

            Result.success(diasConEntrada)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun subirArchivo(rutaLocal: String, usuarioId: String, carpeta: String): String = suspendCancellableCoroutine { continuation ->
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
                    if (url != null) continuation.resume(url) else continuation.resumeWithException(Exception("No URL"))
                }
                override fun onError(requestId: String, error: ErrorInfo) { continuation.resumeWithException(Exception(error.description)) }
                override fun onReschedule(requestId: String, error: ErrorInfo) { continuation.resumeWithException(Exception(error.description)) }
            })
            .dispatch()
        continuation.invokeOnCancellation { MediaManager.get().cancelRequest(requestId) }
    }

    private fun eliminarArchivo(url: String) {
        val publicId = url.substringAfter("/upload/").substringAfter("/").substringBeforeLast(".") 
    }

    private fun mapToComentario(doc: com.google.firebase.firestore.DocumentSnapshot): Comentario? {
        return try {
            Comentario(
                id = doc.id,
                entradaId = doc.getString("entradaId") ?: "",
                usuarioId = doc.getString("usuarioId") ?: "",
                nombreUsuario = doc.getString("nombreUsuario") ?: "",
                texto = doc.getString("texto") ?: "",
                relacionId = doc.getString("relacionId") ?: "",
                creadoEn = doc.getTimestamp("creadoEn") ?: com.google.firebase.Timestamp.now()
            )
        } catch (e: Exception) { null }
    }

    override suspend fun obtenerComentarios(entradaId: String): Result<List<Comentario>> {
        return try {
            val snapshot = firestore.collection("entradas").document(entradaId).collection("comentarios").get().await()
            val comentarios = snapshot.documents.mapNotNull { mapToComentario(it) }.sortedBy { it.creadoEn }
            Result.success(comentarios)
        } catch (e: Exception) { Result.failure(e) }
    }

    override fun escucharComentarios(entradaId: String): Flow<List<Comentario>> = callbackFlow {
        val listener = firestore.collection("entradas").document(entradaId)
            .collection("comentarios")
            .orderBy("creadoEn", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val comentarios = snapshot?.documents?.mapNotNull { mapToComentario(it) } ?: emptyList()
                trySend(comentarios)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun agregarComentario(comentario: Comentario): Result<Comentario> {
        return try {
            val ref = firestore.collection("entradas").document(comentario.entradaId).collection("comentarios").document()
            val final = comentario.copy(id = ref.id)
            ref.set(final).await()
            Result.success(final)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun eliminarComentario(entradaId: String, comentarioId: String): Result<Unit> {
        return try {
            firestore.collection("entradas").document(entradaId).collection("comentarios").document(comentarioId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
