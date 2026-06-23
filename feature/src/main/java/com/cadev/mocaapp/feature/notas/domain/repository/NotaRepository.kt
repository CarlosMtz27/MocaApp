package com.cadev.mocaapp.feature.notas.domain.repository

import com.cadev.mocaapp.feature.notas.domain.model.NotaActual
import kotlinx.coroutines.flow.Flow

/**
 * REGLAS DE LAS NOTAS COMPARTIDAS
 * 
 * Qué hace:
 * Aquí definimos todas las acciones que podemos hacer con nuestras notas rápidas: 
 * actualizarlas, borrarlas o quedarnos escuchando si nuestra pareja escribe algo nuevo.
 */
interface NotaRepository {
    /**
     * Guarda o reemplaza la nota actual en la base de datos.
     */
    suspend fun actualizarNota(relacionId: String, usuarioId: String, nota: NotaActual): Result<Unit>

    /**
     * Quita la nota de nuestro rincón compartido.
     */
    suspend fun eliminarNota(relacionId: String, usuarioId: String): Result<Unit>

    /**
     * Nos avisa al instante cada vez que alguien cambia su nota.
     */
    fun escucharNota(relacionId: String, usuarioId: String): Flow<NotaActual?>

    /**
     * Busca la nota actual una sola vez.
     */
    suspend fun obtenerNota(relacionId: String, usuarioId: String): Result<NotaActual?>
}

