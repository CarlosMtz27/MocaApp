package com.cadev.mocaapp.feature.cuestionarios.domain.model

import com.google.firebase.Timestamp

/**
 * EL MODELO DE UNA RESPUESTA
 * 
 * Qué hace:
 * Guarda lo que un usuario ha contestado a una pregunta específica de un cuestionario. 
 * Incluye el valor en texto o el enlace a una foto si la respuesta fue una imagen.
 * 
 * Cómo lo podemos modificar:
 * Si queremos saber desde dónde se respondió, debemos añadir `val ubicacion: String = ""`.
 */
data class Respuesta(
    val id: String = "",                      // ID único de esta respuesta
    val cuestionarioId: String = "",          // A qué test pertenece
    val usuarioId: String = "",               // Quién respondió
    val preguntaId: String = "",              // Cuál era la pregunta
    val valor: String = "",                   // El contenido de la respuesta
    val imagenUrl: String = "",               // Enlace a la foto si el tipo era FOTO
    val creadoEn: Timestamp = Timestamp.now() // Cuándo se guardó
)
