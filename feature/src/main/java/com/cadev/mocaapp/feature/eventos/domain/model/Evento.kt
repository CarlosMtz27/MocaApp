package com.cadev.mocaapp.feature.eventos.domain.model

import com.google.firebase.Timestamp
import com.cadev.mocaapp.core.model.TipoEvento

/**
 * EL MODELO DE UN EVENTO
 * 
 * Qué hace:
 * Aquí definimos qué información guardamos de cada cita o plan: el título, la fecha, 
 * la hora y si queremos que la app nos avise con un recordatorio.
 * 
 * Cómo lo podemos modificar:
 * Si queremos añadir una ubicación al evento (ej: el nombre del restaurante), 
 * debemos añadir una propiedad como `val lugar: String = ""`.
 */
data class Evento(
    val id: String = "",                     // ID único del evento
    val titulo: String = "",                 // Nombre de la cita (ej: "Cena romántica")
    val descripcion: String = "",           // Detalles adicionales del plan
    val fecha: String = "",                  // Día en formato YYYY-MM-DD
    val hora: String = "12:00",              // Hora en formato HH:mm
    val tipo: String = TipoEvento.OTRO.name, // Categoría del evento
    val creadoPor: String = "",              // Quién de los dos creó el plan
    val relacionId: String = "",             // A qué pareja pertenece el evento
    val recordatorio: Boolean = true,        // Si se debe activar la alarma
    val minutosAntes: Int = 60,              // Cuánto tiempo antes avisar
    val creadoEn: Timestamp = Timestamp.now(), // Cuándo se guardó el plan
    val convertidoEnRecuerdo: Boolean = false, // Si ya se creó un recuerdo de este evento
    val pospuesto: Boolean = false,            // Si el evento fue movido de su fecha original
    val fechaOriginal: String? = null,         // Almacena la fecha inicial si se pospone
    val fotoUrl: String = ""                   // Imagen destacada del evento
)

/**
 * OPCIONES DE RECORDATORIO
 * 
 * Qué hace:
 * Define las opciones que podemos elegir para que la app nos avise antes de la cita.
 */
enum class RecordatorioOpcion(val etiqueta: String, val minutos: Int) {
    QUINCE_MIN("15 minutos antes", 15),
    TREINTA_MIN("30 minutos antes", 30),
    UNA_HORA("1 hora antes", 60),
    DOS_HORAS("2 horas antes", 120),
    UN_DIA("1 día antes", 1440),
    UNA_SEMANA("1 semana antes", 10080)
}

