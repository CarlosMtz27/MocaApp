package com.cadev.mocaapp.feature.cuestionarios.domain.repository

import com.cadev.mocaapp.feature.cuestionarios.domain.model.Cuestionario
import com.cadev.mocaapp.feature.cuestionarios.domain.model.EstadoCuestionario
import com.cadev.mocaapp.feature.cuestionarios.domain.model.Respuesta
import com.cadev.mocaapp.feature.cuestionarios.domain.model.ResultadoCuestionario

/**
 * REGLAS DE LOS TESTS DE PAREJA
 * 
 * Qué hace:
 * Aquí definimos todas las acciones relacionadas con los cuestionarios: 
 * ver los disponibles, responderlos, calcular la compatibilidad o subir fotos.
 * 
 * Cómo lo podemos ampliar:
 * Si queremos que los usuarios puedan "darle like" a un test, debemos añadir:
 * suspend fun darLike(cuestionarioId: String): Result<Unit>
 */
interface CuestionarioRepository {

    /**
     * Trae todos los tests (los que vienen con la app y los creados por la pareja).
     */
    suspend fun obtenerCuestionarios(relacionId: String): Result<List<Cuestionario>>

    /**
     * Busca la información completa de un solo test usando su ID.
     */
    suspend fun obtenerCuestionario(id: String): Result<Cuestionario>

    /**
     * Guarda lo que el usuario respondió (textos y fotos) en la base de datos.
     */
    suspend fun guardarRespuestas(
        cuestionarioId: String,
        usuarioId: String,
        respuestas: Map<String, String>,
        respuestasFoto: Map<String, String>
    ): Result<Unit>

    /**
     * Trae las respuestas en texto que dio un usuario específico.
     */
    suspend fun obtenerRespuestas(
        cuestionarioId: String,
        usuarioId: String
    ): Result<Map<String, String>>

    /**
     * Trae los enlaces a las fotos que subió un usuario como respuesta.
     */
    suspend fun obtenerRespuestasFoto(
        cuestionarioId: String,
        usuarioId: String
    ): Result<Map<String, String>>

    /**
     * Mira si un test está pendiente, si lo hizo uno o si lo terminaron los dos.
     */
    suspend fun obtenerEstado(
        cuestionarioId: String,
        usuarioId: String,
        parejaId: String
    ): Result<EstadoCuestionario>

    /**
     * Trae el estado de muchos cuestionarios a la vez (para la lista principal).
     */
    suspend fun obtenerEstadosTodos(
        cuestionarios: List<Cuestionario>,
        usuarioId: String,
        parejaId: String
    ): Result<Map<String, EstadoCuestionario>>

    /**
     * Compara las respuestas de los dos y saca el porcentaje de match.
     */
    suspend fun calcularResultado(
        cuestionarioId: String,
        relacionId: String,
        usuarioId: String,
        parejaId: String
    ): Result<ResultadoCuestionario>

    /**
     * Busca el porcentaje de match que ya fue calculado anteriormente.
     */
    suspend fun obtenerResultado(
        cuestionarioId: String
    ): Result<ResultadoCuestionario?>

    /**
     * Lista todos los tests que el usuario ya ha terminado.
     */
    suspend fun obtenerHistorial(
        relacionId: String,
        usuarioId: String
    ): Result<List<Cuestionario>>

    /**
     * Permite crear un test nuevo personalizado.
     */
    suspend fun crearCuestionario(cuestionario: Cuestionario): Result<Cuestionario>

    /**
     * Crea automáticamente los tests iniciales cuando se abre la app por primera vez.
     */
    suspend fun poblarPredefinidos(): Result<Unit>

    /**
     * Sube una imagen a la nube (Cloudinary) para usarla en el test.
     */
    suspend fun subirFoto(rutaLocal: String): Result<String>
}
