package com.cadev.mocaapp.feature.pareja.domain.repository

import com.cadev.mocaapp.feature.pareja.domain.model.Relacion

interface ParejaRepository {

    // Busca un usuario por su código y vincula la relación
    suspend fun vincularPorCodigo(
        codigoPareja: String,
        miUsuarioId: String
    ): Result<Relacion>

    // Obtiene el código propio del usuario actual
    suspend fun obtenerMiCodigo(usuarioId: String): Result<String>

    // Verifica si el usuario ya tiene pareja vinculada
    suspend fun tienePareja(usuarioId: String): Boolean
}