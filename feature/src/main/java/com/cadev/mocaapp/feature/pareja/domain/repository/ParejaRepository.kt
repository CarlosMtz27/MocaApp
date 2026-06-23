package com.cadev.mocaapp.feature.pareja.domain.repository

/**
 * REGLAS DE LA VINCULACIÓN DE PAREJA
 * 
 * Qué hace:
 * Aquí definimos qué acciones podemos hacer para conectarnos: buscar por código, 
 * guardar nuestra fecha especial y verificar si ya estamos vinculados.
 */
interface ParejaRepository {

    /**
     * Nos permite unirnos usando el código que nos compartió nuestra pareja.
     */
    suspend fun vincularPorCodigo(
        codigoPareja: String,
        miUsuarioId: String
    ): Result<String>

    /**
     * Guarda el día exacto en que empezó nuestra relación.
     */
    suspend fun guardarFechaInicio(
        relacionId: String,
        fecha: Long
    ): Result<Unit>

    /**
     * Recupera nuestro propio código para que podamos compartirlo.
     */
    suspend fun obtenerMiCodigo(usuarioId: String): Result<String>

    /**
     * Comprueba si ya tenemos a alguien vinculado en nuestra cuenta.
     */
    suspend fun tienePareja(usuarioId: String): Boolean
}
