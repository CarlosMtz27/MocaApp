package com.cadev.mocaapp.feature.estadoanimo.domain.repository

import com.cadev.mocaapp.feature.estadoanimo.domain.model.EstadoAnimoActual
import kotlinx.coroutines.flow.Flow

/**
 * REGLAS DEL ESTADO DE ÁNIMO
 * 
 * Qué hace:
 * Define cómo podemos guardar y leer cómo se siente cada miembro de la pareja.
 */
interface EstadoAnimoRepository {
    /**
     * Guarda el estado de ánimo actual del usuario.
     */
    suspend fun actualizarEstado(relacionId: String, uid: String, emoji: String)

    /**
     * Recibe avisos en tiempo real cada vez que alguien cambia su estado.
     */
    fun escucharEstados(relacionId: String, uidPropio: String): Flow<Pair<EstadoAnimoActual?, EstadoAnimoActual?>>

    /**
     * Busca los estados actuales una sola vez.
     */
    suspend fun obtenerEstados(relacionId: String, uidPropio: String): Pair<EstadoAnimoActual?, EstadoAnimoActual?>
}

