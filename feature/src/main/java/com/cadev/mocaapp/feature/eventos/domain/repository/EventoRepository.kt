package com.cadev.mocaapp.feature.eventos.domain.repository

import com.cadev.mocaapp.feature.eventos.domain.model.Evento

interface EventoRepository {
    suspend fun crearEvento(evento: Evento): Result<Evento>
    suspend fun obtenerEventos(relacionId: String): Result<List<Evento>>
    suspend fun obtenerEvento(eventoId: String): Result<Evento>
    suspend fun actualizarEvento(evento: Evento): Result<Unit>
    suspend fun eliminarEvento(eventoId: String): Result<Unit>
}