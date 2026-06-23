package com.cadev.mocaapp.core.model

/**
 * ESTE ARCHIVO DEFINE LOS CANALES DE AVISOS
 * 
 * Qué hace
 * Aquí se organizan las diferentes categorías de notificaciones que el móvil puede recibir. 
 * Esto permite que el sistema sepa si el aviso es sobre el chat, el diario o un evento.
 */
enum class TipoNotificacion(val canal: String) {
    CHAT("canal_chat"),
    DIARIO("canal_diario"),
    CUESTIONARIO("canal_cuestionario"),
    ANIVERSARIO("canal_aniversario"),
    NOTA("canal_nota"),
    EVENTO("canal_evento"),
    ESTADO_ANIMO("canal_estado_animo")
}
