package com.cadev.mocaapp.feature.eventos.domain.repository

import com.cadev.mocaapp.feature.eventos.domain.model.Evento

/**
 * REGLAS DE LOS EVENTOS Y PLANES
 * 
 * Qué hace:
 * Aquí definimos todas las acciones que podemos hacer con nuestro calendario 
 * compartido: crear planes, verlos todos, editarlos o borrarlos.
 */
interface EventoRepository {
    /**
     * Guarda un plan nuevo para la pareja.
     */
    suspend fun crearEvento(evento: Evento): Result<Evento>

    /**
     * Trae todos los planes que la pareja ha guardado.
     */
    suspend fun obtenerEventos(relacionId: String): Result<List<Evento>>

    /**
     * Busca la información de una sola cita usando su ID.
     */
    suspend fun obtenerEvento(eventoId: String): Result<Evento>

    /**
     * Guarda los cambios de una cita que ya existía.
     */
    suspend fun actualizarEvento(evento: Evento): Result<Unit>

    /**
     * Quita una cita de nuestro calendario compartido.
     */
    suspend fun eliminarEvento(eventoId: String): Result<Unit>
}
