package com.cadev.mocaapp.feature.cuestionarios.domain.repository

import com.cadev.mocaapp.feature.cuestionarios.domain.model.Cuestionario
import com.cadev.mocaapp.feature.cuestionarios.domain.model.Respuesta
import com.cadev.mocaapp.feature.cuestionarios.domain.model.ResultadoCuestionario

interface CuestionarioRepository {

    // Obtener cuestionarios predefinidos + personalizados de la pareja
    suspend fun obtenerCuestionarios(relacionId: String): Result<List<Cuestionario>>

    // Obtener un cuestionario por ID
    suspend fun obtenerCuestionario(id: String): Result<Cuestionario>

    // Guardar respuestas del usuario
    suspend fun guardarRespuestas(
        cuestionarioId: String,
        usuarioId: String,
        respuestas: Map<String, String>  // preguntaId, valor
    ): Result<Unit>

    // Obtener respuestas de un usuario
    suspend fun obtenerRespuestas(
        cuestionarioId: String,
        usuarioId: String
    ): Result<Map<String, String>>

    // Verificar si la pareja ya respondió
    suspend fun parejaRespondio(
        cuestionarioId: String,
        parejaId: String
    ): Result<Boolean>

    // Calcular y guardar resultado
    suspend fun calcularResultado(
        cuestionarioId: String,
        relacionId: String,
        usuarioId: String,
        parejaId: String
    ): Result<ResultadoCuestionario>

    // Obtener resultado
    suspend fun obtenerResultado(
        cuestionarioId: String
    ): Result<ResultadoCuestionario?>

    // Historial de completados
    suspend fun obtenerHistorial(
        relacionId: String,
        usuarioId: String
    ): Result<List<Cuestionario>>

    // Crear cuestionario personalizado
    suspend fun crearCuestionario(
        cuestionario: Cuestionario
    ): Result<Cuestionario>

    // Poblar cuestionarios predefinidos (llamar una vez)
    suspend fun poblarPredefinidos(): Result<Unit>
}