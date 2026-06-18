package com.cadev.mocaapp.feature.notas.domain.repository

import com.cadev.mocaapp.feature.notas.domain.model.NotaActual
import kotlinx.coroutines.flow.Flow

interface NotaRepository {
    suspend fun actualizarNota(relacionId: String, usuarioId: String, nota: NotaActual): Result<Unit>
    suspend fun eliminarNota(relacionId: String, usuarioId: String): Result<Unit>
    fun escucharNota(relacionId: String, usuarioId: String): Flow<NotaActual?>
    suspend fun obtenerNota(relacionId: String, usuarioId: String): Result<NotaActual?>
}
