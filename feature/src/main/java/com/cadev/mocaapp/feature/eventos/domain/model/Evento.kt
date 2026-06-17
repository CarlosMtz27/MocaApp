package com.cadev.mocaapp.feature.eventos.domain.model

import com.google.firebase.Timestamp
import com.cadev.mocaapp.core.model.TipoEvento

data class Evento(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: String = "",           // "yyyy-MM-dd"
    val hora: String = "12:00",       // "HH:mm"
    val tipo: String = TipoEvento.OTRO.name,
    val creadoPor: String = "",
    val relacionId: String = "",
    val recordatorio: Boolean = true,
    val minutosAntes: Int = 60,
    val creadoEn: Timestamp = Timestamp.now()
)

enum class RecordatorioOpcion(val etiqueta: String, val minutos: Int) {
    QUINCE_MIN("15 minutos antes", 15),
    TREINTA_MIN("30 minutos antes", 30),
    UNA_HORA("1 hora antes", 60),
    DOS_HORAS("2 horas antes", 120),
    UN_DIA("1 día antes", 1440),
    UNA_SEMANA("1 semana antes", 10080)
}
