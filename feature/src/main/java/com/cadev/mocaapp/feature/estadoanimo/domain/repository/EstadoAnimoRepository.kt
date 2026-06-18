package com.cadev.mocaapp.feature.estadoanimo.domain.repository

import com.cadev.mocaapp.feature.estadoanimo.domain.model.EstadoAnimoActual
import kotlinx.coroutines.flow.Flow

interface EstadoAnimoRepository {
    suspend fun actualizarEstado(relacionId: String, uid: String, emoji: String)
    fun escucharEstados(relacionId: String, uidPropio: String): Flow<Pair<EstadoAnimoActual?, EstadoAnimoActual?>>
    suspend fun obtenerEstados(relacionId: String, uidPropio: String): Pair<EstadoAnimoActual?, EstadoAnimoActual?>
}
