package com.cadev.mocaapp.feature.cuestionarios.data.repository

import com.cadev.mocaapp.feature.cuestionarios.domain.model.*
import com.cadev.mocaapp.feature.cuestionarios.domain.repository.CuestionarioRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CuestionarioRepositoryImpl(
    private val firestore: FirebaseFirestore
) : CuestionarioRepository {

    override suspend fun obtenerCuestionarios(
        relacionId: String
    ): Result<List<Cuestionario>> {
        return try {
            // Predefinidos del sistema
            val predefinidos = firestore
                .collection("cuestionarios")
                .whereEqualTo("creadoPor", "sistema")
                .get().await()
                .documents.mapNotNull { it.toObject(Cuestionario::class.java) }

            // Personalizados de la pareja
            val personalizados = firestore
                .collection("cuestionarios")
                .whereEqualTo("relacionId", relacionId)
                .get().await()
                .documents.mapNotNull { it.toObject(Cuestionario::class.java) }

            Result.success(predefinidos + personalizados)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerCuestionario(
        id: String
    ): Result<Cuestionario> {
        return try {
            val doc = firestore
                .collection("cuestionarios")
                .document(id)
                .get().await()
            val cuestionario = doc.toObject(Cuestionario::class.java)
                ?: return Result.failure(Exception("No encontrado"))
            Result.success(cuestionario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun guardarRespuestas(
        cuestionarioId: String,
        usuarioId: String,
        respuestas: Map<String, String>
    ): Result<Unit> {
        return try {
            val batch = firestore.batch()

            respuestas.forEach { (preguntaId, valor) ->
                val respuesta = Respuesta(
                    id = "$usuarioId-$preguntaId",
                    cuestionarioId = cuestionarioId,
                    usuarioId = usuarioId,
                    preguntaId = preguntaId,
                    valor = valor
                )
                val ref = firestore
                    .collection("respuestas")
                    .document(cuestionarioId)
                    .collection(usuarioId)
                    .document(preguntaId)
                batch.set(ref, respuesta)
            }

            // Marcar que este usuario completó
            val refCompletado = firestore
                .collection("respuestas")
                .document(cuestionarioId)
            batch.set(
                refCompletado,
                mapOf("completadoPor" to
                        com.google.firebase.firestore.FieldValue.arrayUnion(usuarioId)),
                com.google.firebase.firestore.SetOptions.merge()
            )

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerRespuestas(
        cuestionarioId: String,
        usuarioId: String
    ): Result<Map<String, String>> {
        return try {
            val docs = firestore
                .collection("respuestas")
                .document(cuestionarioId)
                .collection(usuarioId)
                .get().await()

            val map = docs.documents.associate { doc ->
                val preguntaId = doc.getString("preguntaId") ?: doc.id
                val valor = doc.getString("valor") ?: ""
                preguntaId to valor
            }
            Result.success(map)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun parejaRespondio(
        cuestionarioId: String,
        parejaId: String
    ): Result<Boolean> {
        return try {
            val doc = firestore
                .collection("respuestas")
                .document(cuestionarioId)
                .get().await()

            val completados = doc.get("completadoPor") as? List<*> ?: emptyList<Any>()
            Result.success(parejaId in completados)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun calcularResultado(
        cuestionarioId: String,
        relacionId: String,
        usuarioId: String,
        parejaId: String
    ): Result<ResultadoCuestionario> {
        return try {
            val cuestionario = obtenerCuestionario(cuestionarioId).getOrThrow()
            val respuestasUsuario = obtenerRespuestas(cuestionarioId, usuarioId)
                .getOrThrow()
            val respuestasPareja = obtenerRespuestas(cuestionarioId, parejaId)
                .getOrThrow()

            var coincidencias = 0
            val totalPreguntas = cuestionario.preguntas.size

            cuestionario.preguntas.forEach { pregunta ->
                val rUsuario = respuestasUsuario[pregunta.id] ?: ""
                val rPareja = respuestasPareja[pregunta.id] ?: ""

                if (rUsuario.isNotBlank() && rPareja.isNotBlank()) {
                    when (pregunta.tipo) {
                        TipoPregunta.OPCION_MULTIPLE.name,
                        TipoPregunta.SI_NO.name -> {
                            if (rUsuario == rPareja) coincidencias++
                        }
                        TipoPregunta.ESCALA.name -> {
                            // Contar como coincidencia si la diferencia es <= 2
                            val diff = kotlin.math.abs(
                                rUsuario.toIntOrNull() ?: 0) -
                                    (rPareja.toIntOrNull() ?: 0
                                            )
                            if (kotlin.math.abs(diff) <= 2) coincidencias++
                        }
                        else -> { /* texto libre no puntúa */ }
                    }
                }
            }

            val porcentaje = if (totalPreguntas > 0)
                (coincidencias * 100) / totalPreguntas else 0

            val resultado = ResultadoCuestionario(
                id = cuestionarioId,
                cuestionarioId = cuestionarioId,
                relacionId = relacionId,
                puntajeCompatibilidad = porcentaje,
                completadoPor = listOf(usuarioId, parejaId)
            )

            firestore
                .collection("resultados")
                .document(cuestionarioId)
                .set(resultado)
                .await()

            Result.success(resultado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerResultado(
        cuestionarioId: String
    ): Result<ResultadoCuestionario?> {
        return try {
            val doc = firestore
                .collection("resultados")
                .document(cuestionarioId)
                .get().await()
            Result.success(doc.toObject(ResultadoCuestionario::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerHistorial(
        relacionId: String,
        usuarioId: String
    ): Result<List<Cuestionario>> {
        return try {
            // Obtener IDs donde este usuario ya respondió
            // Simplificado: obtener todos y filtrar
            val todos = obtenerCuestionarios(relacionId).getOrThrow()
            val completados = todos.filter { cuestionario ->
                val doc = firestore
                    .collection("respuestas")
                    .document(cuestionario.id)
                    .get().await()
                val lista = doc.get("completadoPor") as? List<*> ?: emptyList<Any>()
                usuarioId in lista
            }
            Result.success(completados)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun crearCuestionario(
        cuestionario: Cuestionario
    ): Result<Cuestionario> {
        return try {
            val ref = firestore.collection("cuestionarios").document()
            val nuevo = cuestionario.copy(id = ref.id)
            ref.set(nuevo).await()
            Result.success(nuevo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun poblarPredefinidos(): Result<Unit> {
        return try {
            val predefinidos = cuestionariosPredefinidos()
            val batch = firestore.batch()
            predefinidos.forEach { cuestionario ->
                val ref = firestore
                    .collection("cuestionarios")
                    .document(cuestionario.id)
                batch.set(ref, cuestionario)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}