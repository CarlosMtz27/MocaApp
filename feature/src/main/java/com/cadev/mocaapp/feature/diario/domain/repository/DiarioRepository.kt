package com.cadev.mocaapp.feature.diario.domain.repository

import com.cadev.mocaapp.feature.diario.domain.model.EntradaDiario

interface DiarioRepository {

    // Obtener todas las entradas de un mes
    // Devuelve un Flow para que la UI se actualice automaticamente
    suspend fun obtenerEntradasDelMes(
        usuarioId: String,
        anio: Int,
        mes: Int
    ): Result<List<EntradaDiario>>

    // Obtener entradas de un dia específico
    // Incluye las de la pareja si están compartidas
    suspend fun obtenerEntradasDelDia(
        usuarioId: String,
        parejaId: String?,
        fecha: String
    ): Result<List<EntradaDiario>>

    // Crear nueva entrada
    suspend fun crearEntrada(
        entrada: EntradaDiario,
        fotosLocales: List<String>  // rutas locales de las fotos
    ): Result<EntradaDiario>

    // Obtener días del mes que tienen entradas (para marcar en calendario)
    suspend fun obtenerDiasConEntrada(
        usuarioId: String,
        parejaId: String?,
        anio: Int,
        mes: Int
    ): Result<Map<String, List<String>>>
    // String = fecha YYYY-MM-DD, Boolean = si es de la pareja
}