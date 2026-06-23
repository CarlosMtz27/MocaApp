package com.cadev.mocaapp.feature.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import androidx.annotation.RequiresApi
import com.cadev.mocaapp.core.model.TipoNotificacion

/**
 * NUESTROS CANALES DE AVISO
 * 
 * Qué hace:
 * Organiza las notificaciones de la app en diferentes categorías (Mensajes, 
 * Diario, Cuestionarios, etc.). Esto permite que el usuario pueda silenciar 
 * una categoría sin apagar las demás desde los ajustes del móvil.
 * 
 * Cómo lo podemos modificar:
 * Si añadimos una nueva función a la app (ej: "Juegos"), debemos crear aquí 
 * un nuevo `NotificationChannel` para sus avisos.
 */
object NotificationChannels {

    fun crearTodos(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        val audioAttr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        canales(audioAttr).forEach { canal ->
            manager.createNotificationChannel(canal)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun canales(audioAttr: AudioAttributes): List<NotificationChannel> {
        val colorRomantico = android.graphics.Color.parseColor("#FF69B4")
        val patronLatido = longArrayOf(0, 100, 100, 100, 600)

        return listOf(
            NotificationChannel(
                TipoNotificacion.CHAT.canal,
                "Mensajes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Nuevos mensajes de tu pareja"
                lightColor = colorRomantico
                enableLights(true)
                enableVibration(true)
                vibrationPattern = patronLatido
            },

            NotificationChannel(
                TipoNotificacion.DIARIO.canal,
                "Diario compartido",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Tu pareja agregó un recuerdo"
                lightColor = colorRomantico
                enableLights(true)
                enableVibration(true)
                vibrationPattern = patronLatido
            },

            NotificationChannel(
                TipoNotificacion.CUESTIONARIO.canal,
                "Cuestionarios",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Tu pareja respondió un cuestionario"
                lightColor = colorRomantico
                enableLights(true)
                enableVibration(true)
                vibrationPattern = patronLatido
            },

            NotificationChannel(
                TipoNotificacion.ANIVERSARIO.canal,
                "Fechas especiales",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorios de fechas importantes"
                lightColor = colorRomantico
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200, 100, 200)
            },

            NotificationChannel(
                TipoNotificacion.ESTADO_ANIMO.canal,
                "Estados de Ánimo",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Cambios en el estado de tu pareja"
                lightColor = colorRomantico
                enableLights(true)
                enableVibration(true)
                vibrationPattern = patronLatido
            }
        )
    }
}
