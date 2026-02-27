package com.cadev.mocaapp.feature.diario.domain.repository

import com.cadev.mocaapp.feature.diario.domain.model.EntradaDiario

interface DiarioRepository {

    suspend fun obtenerEntradasDelMes(
        usuarioId: String,
        anio: Int,
        mes: Int
    ): Result<List<EntradaDiario>>

    suspend fun obtenerEntradasDelDia(
        usuarioId: String,
        parejaId: String?,
        fecha: String
    ): Result<List<EntradaDiario>>

    // incluye videosLocales
    suspend fun crearEntrada(
        entrada: EntradaDiario,
        fotosLocales: List<String>,
        videosLocales: List<String>
    ): Result<EntradaDiario>

    suspend fun obtenerDiasConEntrada(
        usuarioId: String,
        parejaId: String?,
        anio: Int,
        mes: Int
    ): Result<Map<String, List<String>>>


    suspend fun obtenerEntradaPorId(entradaId: String): Result<EntradaDiario>

    // Actualizar una entrada existente
    suspend fun actualizarEntrada(
        entrada: EntradaDiario,
        fotosNuevas: List<String>,      // rutas locales de fotos nuevas
        videosNuevos: List<String>,     // rutas locales de videos nuevos
        fotosEliminar: List<String>,    // URLs de fotos a eliminar de Storage
        videosEliminar: List<String>    // URLs de videos a eliminar de Storage
    ): Result<EntradaDiario>
}