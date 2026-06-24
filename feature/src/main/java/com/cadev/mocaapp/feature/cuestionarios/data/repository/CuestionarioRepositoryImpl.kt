package com.cadev.mocaapp.feature.cuestionarios.data.repository

import android.net.Uri
import com.cadev.mocaapp.feature.cuestionarios.domain.model.*
import com.cadev.mocaapp.feature.cuestionarios.domain.repository.CuestionarioRepository
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.resume

class CuestionarioRepositoryImpl(
    private val firestore: FirebaseFirestore
) : CuestionarioRepository {

    override fun obtenerCuestionariosFlow(relacionId: String): Flow<List<Cuestionario>> {
        val predefinidosFlow = callbackFlow {
            val listener = firestore.collection("cuestionarios")
                .whereEqualTo("creadoPor", "sistema")
                .addSnapshotListener { snapshot, _ ->
                    val list = snapshot?.documents?.mapNotNull { it.toObject(Cuestionario::class.java) } ?: emptyList()
                    trySend(list)
                }
            awaitClose { listener.remove() }
        }

        val personalizadosFlow = callbackFlow {
            val listener = firestore.collection("cuestionarios")
                .whereEqualTo("relacionId", relacionId)
                .addSnapshotListener { snapshot, _ ->
                    val list = snapshot?.documents?.mapNotNull { it.toObject(Cuestionario::class.java) } ?: emptyList()
                    trySend(list)
                }
            awaitClose { listener.remove() }
        }

        return combine(predefinidosFlow, personalizadosFlow) { pre, per -> (pre + per) }
    }

    override fun obtenerEstadoFlow(
        cuestionarioId: String,
        usuarioId: String,
        parejaId: String
    ): Flow<EstadoCuestionario> = callbackFlow {
        val listener = firestore.collection("respuestas").document(cuestionarioId)
            .addSnapshotListener { snapshot, _ ->
                val completados = snapshot?.get("completadoPor") as? List<*> ?: emptyList<Any>()
                val yo = usuarioId in completados
                val pareja = parejaId in completados
                val estado = when {
                    yo && pareja -> EstadoCuestionario.AMBOS
                    yo -> EstadoCuestionario.YO_RESPONDÍ
                    pareja -> EstadoCuestionario.PAREJA_RESPONDIÓ
                    else -> EstadoCuestionario.NINGUNO
                }
                trySend(estado)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun obtenerCuestionarios(relacionId: String): Result<List<Cuestionario>> = try {
        val predefinidos = firestore.collection("cuestionarios").whereEqualTo("creadoPor", "sistema").get().await().documents.mapNotNull { it.toObject(Cuestionario::class.java) }
        val personalizados = firestore.collection("cuestionarios").whereEqualTo("relacionId", relacionId).get().await().documents.mapNotNull { it.toObject(Cuestionario::class.java) }
        Result.success(predefinidos + personalizados)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun obtenerCuestionario(id: String): Result<Cuestionario> = try {
        val doc = firestore.collection("cuestionarios").document(id).get().await()
        val c = doc.toObject(Cuestionario::class.java) ?: return Result.failure(Exception("No encontrado"))
        Result.success(c)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun guardarRespuestas(cuestionarioId: String, usuarioId: String, respuestas: Map<String, String>, respuestasFoto: Map<String, String>): Result<Unit> = try {
        val batch = firestore.batch()
        respuestas.forEach { (preguntaId, valor) ->
            val fotoUrl = respuestasFoto[preguntaId] ?: ""
            val respuesta = Respuesta(id = "$usuarioId-$preguntaId", cuestionarioId = cuestionarioId, usuarioId = usuarioId, preguntaId = preguntaId, valor = valor, imagenUrl = fotoUrl)
            val ref = firestore.collection("respuestas").document(cuestionarioId).collection(usuarioId).document(preguntaId)
            batch.set(ref, respuesta)
        }
        val refMeta = firestore.collection("respuestas").document(cuestionarioId)
        batch.set(refMeta, mapOf("completadoPor" to com.google.firebase.firestore.FieldValue.arrayUnion(usuarioId)), com.google.firebase.firestore.SetOptions.merge())
        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun obtenerRespuestas(cuestionarioId: String, usuarioId: String): Result<Map<String, String>> = try {
        val docs = firestore.collection("respuestas").document(cuestionarioId).collection(usuarioId).get().await()
        val map = docs.documents.associate { doc -> (doc.getString("preguntaId") ?: doc.id) to (doc.getString("valor") ?: "") }
        Result.success(map)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun obtenerRespuestasFoto(cuestionarioId: String, usuarioId: String): Result<Map<String, String>> = try {
        val docs = firestore.collection("respuestas").document(cuestionarioId).collection(usuarioId).get().await()
        val map = docs.documents.filter { (it.getString("imagenUrl") ?: "").isNotBlank() }.associate { doc -> (doc.getString("preguntaId") ?: doc.id) to (doc.getString("imagenUrl") ?: "") }
        Result.success(map)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun obtenerEstado(cuestionarioId: String, usuarioId: String, parejaId: String): Result<EstadoCuestionario> = try {
        val doc = firestore.collection("respuestas").document(cuestionarioId).get().await()
        val completados = doc.get("completadoPor") as? List<*> ?: emptyList<Any>()
        val yoRespondí = usuarioId in completados
        val parejaRespondió = parejaId in completados
        val estado = when {
            yoRespondí && parejaRespondió -> EstadoCuestionario.AMBOS
            yoRespondí -> EstadoCuestionario.YO_RESPONDÍ
            parejaRespondió -> EstadoCuestionario.PAREJA_RESPONDIÓ
            else -> EstadoCuestionario.NINGUNO
        }
        Result.success(estado)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun obtenerEstadosTodos(cuestionarios: List<Cuestionario>, usuarioId: String, parejaId: String): Result<Map<String, EstadoCuestionario>> = try {
        coroutineScope {
            val deferreds = cuestionarios.map { c ->
                async {
                    val estado = obtenerEstado(c.id, usuarioId, parejaId).getOrDefault(EstadoCuestionario.NINGUNO)
                    c.id to estado
                }
            }
            Result.success(deferreds.awaitAll().toMap())
        }
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun calcularResultado(cuestionarioId: String, relacionId: String, usuarioId: String, parejaId: String): Result<ResultadoCuestionario> = try {
        val cuestionario = obtenerCuestionario(cuestionarioId).getOrThrow()
        val respuestasUsuario = obtenerRespuestas(cuestionarioId, usuarioId).getOrThrow()
        val respuestasPareja = obtenerRespuestas(cuestionarioId, parejaId).getOrThrow()
        var coincidencias = 0
        var totalComparables = 0
        cuestionario.preguntas.forEach { pregunta ->
            if (pregunta.tipo == TipoPregunta.FOTO.name || pregunta.tipo == TipoPregunta.TEXTO_LIBRE.name) return@forEach
            totalComparables++
            val rU = respuestasUsuario[pregunta.id] ?: ""
            val rP = respuestasPareja[pregunta.id] ?: ""
            if (rU.isNotBlank() && rP.isNotBlank()) {
                when (pregunta.tipo) {
                    TipoPregunta.OPCION_MULTIPLE.name, TipoPregunta.SI_NO.name -> if (rU == rP) coincidencias++
                    TipoPregunta.ESCALA.name -> {
                        val diff = kotlin.math.abs((rU.toIntOrNull() ?: 0) - (rP.toIntOrNull() ?: 0))
                        if (diff <= 2) coincidencias++
                    }
                }
            }
        }
        val porcentaje = if (totalComparables > 0) (coincidencias * 100) / totalComparables else 0
        val resultado = ResultadoCuestionario(id = cuestionarioId, cuestionarioId = cuestionarioId, relacionId = relacionId, puntajeCompatibilidad = porcentaje, completadoPor = listOf(usuarioId, parejaId))
        firestore.collection("resultados").document(cuestionarioId).set(resultado).await()
        Result.success(resultado)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun obtenerResultado(cuestionarioId: String): Result<ResultadoCuestionario?> = try {
        val doc = firestore.collection("resultados").document(cuestionarioId).get().await()
        Result.success(doc.toObject(ResultadoCuestionario::class.java))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun obtenerHistorial(relacionId: String, usuarioId: String): Result<List<Cuestionario>> = try {
        coroutineScope {
            val todos = obtenerCuestionarios(relacionId).getOrThrow()
            val checks = todos.map { cuestionario ->
                async {
                    val doc = firestore.collection("respuestas").document(cuestionario.id).get().await()
                    val lista = doc.get("completadoPor") as? List<*> ?: emptyList<Any>()
                    if (usuarioId in lista) cuestionario else null
                }
            }
            Result.success(checks.awaitAll().filterNotNull())
        }
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun crearCuestionario(cuestionario: Cuestionario): Result<Cuestionario> = try {
        val ref = firestore.collection("cuestionarios").document()
        val nuevo = cuestionario.copy(id = ref.id)
        ref.set(nuevo).await()
        Result.success(nuevo)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun poblarPredefinidos(): Result<Unit> = try {
        val yaExisten = firestore.collection("cuestionarios").whereEqualTo("creadoPor", "sistema").limit(1).get().await().documents.isNotEmpty()
        if (!yaExisten) {
            val predefinidos = cuestionariosPredefinidos()
            val batch = firestore.batch()
            predefinidos.forEach { c -> batch.set(firestore.collection("cuestionarios").document(c.id), c) }
            batch.commit().await()
        }
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun subirFoto(rutaLocal: String): Result<String> = suspendCancellableCoroutine { continuation ->
        val requestId = MediaManager.get().upload(Uri.parse(rutaLocal)).option("folder", "cuestionarios").option("public_id", UUID.randomUUID().toString()).option("resource_type", "image").callback(object : UploadCallback {
            override fun onStart(requestId: String) {}
            override fun onProgress(requestId: String, bytes: Long, total: Long) {}
            override fun onSuccess(requestId: String, resultData: Map<*, *>) { continuation.resume(Result.success(resultData["secure_url"] as? String ?: "")) }
            override fun onError(requestId: String, error: ErrorInfo) { continuation.resume(Result.failure(Exception(error.description))) }
            override fun onReschedule(requestId: String, error: ErrorInfo) {}
        }).dispatch()
        continuation.invokeOnCancellation { MediaManager.get().cancelRequest(requestId) }
    }
}
