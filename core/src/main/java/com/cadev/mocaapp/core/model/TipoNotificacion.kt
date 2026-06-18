package com.cadev.mocaapp.core.model
enum class TipoNotificacion(val canal: String) {
    CHAT("canal_chat"),
    DIARIO("canal_diario"),
    CUESTIONARIO("canal_cuestionario"),
    ANIVERSARIO("canal_aniversario"),
    NOTA("canal_nota"),
    EVENTO("canal_evento")
}