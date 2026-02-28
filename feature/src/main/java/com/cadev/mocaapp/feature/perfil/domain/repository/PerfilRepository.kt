package com.cadev.mocaapp.feature.perfil.domain.repository

import com.cadev.mocaapp.feature.auth.domain.model.Usuario

interface PerfilRepository {

    suspend fun obtenerUsuario(usuarioId: String): Result<Usuario>

    suspend fun obtenerPareja(parejaId: String): Result<Usuario>

    suspend fun actualizarNombre(
        usuarioId: String,
        nuevoNombre: String
    ): Result<Unit>

    suspend fun actualizarEmail(
        usuarioId: String,
        nuevoEmail: String,
        passwordActual: String
    ): Result<Unit>

    suspend fun actualizarPassword(
        emailActual: String,
        passwordActual: String,
        nuevoPassword: String
    ): Result<Unit>

    suspend fun actualizarFotoPerfil(
        usuarioId: String,
        rutaLocal: String
    ): Result<String>  // devuelve URL de Cloudinary

    suspend fun contarEntradas(usuarioId: String): Result<Int>

    suspend fun obtenerFechaRelacion(
        usuarioId: String
    ): Result<String?>  // "yyyy-MM-dd" o null

    suspend fun actualizarFechaRelacion(
        usuarioId: String,
        fecha: String
    ): Result<Unit>
}