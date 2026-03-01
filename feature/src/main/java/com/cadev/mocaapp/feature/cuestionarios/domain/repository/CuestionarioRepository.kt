package com.cadev.mocaapp.feature.cuestionarios.domain.repository

import com.cadev.mocaapp.feature.cuestionarios.domain.model.Cuestionario
import com.cadev.mocaapp.feature.cuestionarios.domain.model.EstadoCuestionario
import com.cadev.mocaapp.feature.cuestionarios.domain.model.Respuesta
import com.cadev.mocaapp.feature.cuestionarios.domain.model.ResultadoCuestionario

interface CuestionarioRepository {

    suspend fun obtenerCuestionarios(relacionId: String): Result<List<Cuestionario>>
    suspend fun obtenerCuestionario(id: String): Result<Cuestionario>

    suspend fun guardarRespuestas(
        cuestionarioId: String,
        usuarioId: String,
        respuestas: Map<String, String>,
        respuestasFoto: Map<String, String>  // preguntaId a cloudinaryUrl
    ): Result<Unit>

    suspend fun obtenerRespuestas(
        cuestionarioId: String,
        usuarioId: String
    ): Result<Map<String, String>>

    // Devuelve mapa preguntaId, imagenUrl para respuestas tipo FOTO
    suspend fun obtenerRespuestasFoto(
        cuestionarioId: String,
        usuarioId: String
    ): Result<Map<String, String>>

    suspend fun obtenerEstado(
        cuestionarioId: String,
        usuarioId: String,
        parejaId: String
    ): Result<EstadoCuestionario>

    suspend fun obtenerEstadosTodos(
        cuestionarios: List<Cuestionario>,
        usuarioId: String,
        parejaId: String
    ): Result<Map<String, EstadoCuestionario>>

    suspend fun calcularResultado(
        cuestionarioId: String,
        relacionId: String,
        usuarioId: String,
        parejaId: String
    ): Result<ResultadoCuestionario>

    suspend fun obtenerResultado(
        cuestionarioId: String
    ): Result<ResultadoCuestionario?>

    suspend fun obtenerHistorial(
        relacionId: String,
        usuarioId: String
    ): Result<List<Cuestionario>>

    suspend fun crearCuestionario(cuestionario: Cuestionario): Result<Cuestionario>
    suspend fun poblarPredefinidos(): Result<Unit>

    // Subir foto a Cloudinary (pregunta o respuesta)
    suspend fun subirFoto(rutaLocal: String): Result<String>
}