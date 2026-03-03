package com.cadev.mocaapp.feature.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import androidx.annotation.RequiresApi
import com.cadev.mocaapp.core.model.TipoNotificacion

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
    private fun canales(audioAttr: AudioAttributes) = listOf(

        NotificationChannel(
            TipoNotificacion.CHAT.canal,
            "Mensajes",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Nuevos mensajes de tu pareja"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 250, 100, 250)
        },

        NotificationChannel(
            TipoNotificacion.DIARIO.canal,
            "Diario compartido",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Tu pareja agregó un recuerdo"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 150, 100, 150)
        },

        NotificationChannel(
            TipoNotificacion.CUESTIONARIO.canal,
            "Cuestionarios",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Tu pareja respondió un cuestionario"
            enableVibration(true)
        },

        NotificationChannel(
            TipoNotificacion.ANIVERSARIO.canal,
            "Fechas especiales",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Recordatorios de fechas importantes"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
        }
    )
}