package com.cadev.mocaapp.feature.notas.domain.model

import com.google.firebase.Timestamp

/**
 * NUESTRO MODELO DE NOTA RÁPIDA
 * 
 * Qué hace:
 * Define la estructura de los mensajes cortos que nos dejamos en el rincón 
 * compartido: el texto, quién lo escribió y cuándo se actualizó.
 * 
 * Cómo lo podemos modificar:
 * Si queremos que las notas tengan un color de fondo elegido por el usuario, 
 * debemos añadir una propiedad como `val colorHex: String = "#FFFFFF"`.
 */
data class NotaActual(
    val texto: String = "",           // El mensaje escrito
    val autorId: String = "",         // ID de quien escribió la nota
    val nombreAutor: String = "",     // Nombre para mostrar rápido en el widget
    val actualizadaEn: Timestamp = Timestamp.now() // Cuándo se guardó
)

