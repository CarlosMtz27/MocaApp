package com.cadev.mocaapp.feature.notificaciones

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.cadev.mocaapp.core.model.TipoNotificacion

/**
 * NUESTRO AYUDANTE DE NOTIFICACIONES VISUALES
 * 
 * Qué hace:
 * Se encarga de construir y mostrar las alertas que aparecen en la barra superior 
 * del teléfono. Configura el título, el mensaje, la vibración y lo más importante: 
 * el "Deep Link" que nos lleva a la sección correcta de la app al tocarla.
 * 
 * Cómo lo podemos modificar:
 * Si queremos que todas las notificaciones tengan un sonido personalizado, debemos 
 * añadir `.setSound(Uri)` dentro del `NotificationCompat.Builder`.
 */
object NotificationHelper {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun mostrar(
        context: Context,
        tipo: TipoNotificacion,
        titulo: String,
        cuerpo: String,
        deepLink: String,
        notifId: Int = tipo.ordinal
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) return
        }

        // Sin referencia a MainActivity, usa el launcher del paquete
        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply {
                putExtra("deepLink", deepLink)
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            } ?: return

        val pendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        /**
         * ESTILO ROMÁNTICO Y MINIMALISTA:
         * 1. Usamos el icono del corazón para todas las notificaciones.
         * 2. Un color Hot Pink suave para acentuar el aviso.
         * 3. La vibración imita un latido (bum-bum... pausa).
         */
        val colorRomantico = android.graphics.Color.parseColor("#FF69B4") // Hot Pink
        val patronLatido = longArrayOf(0, 100, 100, 100, 600) // Vibra como un latido

        val notificacion = NotificationCompat.Builder(context, tipo.canal)
            .setSmallIcon(com.cadev.mocaapp.feature.R.drawable.ic_notif_corazon)
            .setColor(colorRomantico)
            .setColorized(true)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Bajado de HIGH para evitar Heads-up
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(patronLatido)
            .build()

        NotificationManagerCompat.from(context).notify(notifId, notificacion)
    }
}
