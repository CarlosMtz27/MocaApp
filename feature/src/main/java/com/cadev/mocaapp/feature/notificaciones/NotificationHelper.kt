package com.cadev.mocaapp.feature.notificaciones

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import coil.ImageLoader
import coil.request.ImageRequest
import com.cadev.mocaapp.core.model.TipoNotificacion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * NUESTRO AYUDANTE DE NOTIFICACIONES VISUALES
 * 
 * Qué hace:
 * Se encarga de construir y mostrar las alertas que aparecen en la barra superior 
 * del teléfono. Configura el título, el mensaje, la vibración y lo más importante: 
 * el "Deep Link" que nos lleva a la sección correcta de la app al tocarla.
 */
object NotificationHelper {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun mostrar(
        context: Context,
        tipo: TipoNotificacion,
        titulo: String,
        cuerpo: String,
        deepLink: String,
        fotoUrl: String? = null,
        remitenteId: String? = null,
        notifId: Int = tipo.ordinal
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) return
        }

        // Cargamos la imagen en segundo plano para no bloquear el hilo de FCM
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = if (!fotoUrl.isNullOrBlank()) {
                descargarBitmap(context, fotoUrl)
            } else null

            withContext(Dispatchers.Main) {
                construirYMostrar(context, tipo, titulo, cuerpo, deepLink, bitmap, remitenteId, notifId)
            }
        }
    }

    private suspend fun descargarBitmap(context: Context, url: String): Bitmap? {
        return try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false) // Necesario para obtener el bitmap
                .build()
            val result = loader.execute(request)
            val drawable = result.drawable
            if (drawable is BitmapDrawable) {
                drawable.bitmap
            } else {
                drawable?.let { convertToBitmap(it) }
            }
        } catch (e: Exception) {
            Log.e("NotifHelper", "Error descargando foto: ${e.message}")
            null
        }
    }

    private fun convertToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun construirYMostrar(
        context: Context,
        tipo: TipoNotificacion,
        titulo: String,
        cuerpo: String,
        deepLink: String,
        fotoBitmap: Bitmap?,
        remitenteId: String?,
        notifId: Int
    ) {
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

        val colorRomantico = android.graphics.Color.parseColor("#FF69B4")
        val patronLatido = longArrayOf(0, 100, 100, 100, 600)

        val prioridad = when (tipo) {
            TipoNotificacion.CHAT,
            TipoNotificacion.EVENTO,
            TipoNotificacion.ANIVERSARIO -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }

        val builder = NotificationCompat.Builder(context, tipo.canal)
            .setSmallIcon(com.cadev.mocaapp.feature.R.drawable.ic_notif_corazon)
            .setColor(colorRomantico)
            .setColorized(true)
            .setPriority(prioridad)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(patronLatido)

        // ESTILO WHATSAPP (MESSAGING STYLE)
        if (tipo == TipoNotificacion.CHAT) {
            val icon = if (fotoBitmap != null) {
                IconCompat.createWithBitmap(fotoBitmap)
            } else {
                // Fallback icon for MessagingStyle if no photo
                try {
                    val appIcon = context.packageManager.getApplicationIcon(context.packageName)
                    IconCompat.createWithBitmap(convertToBitmap(appIcon))
                } catch (e: Exception) { null }
            }

            val sender = Person.Builder()
                .setName(titulo) // El nombre de la pareja
                .setIcon(icon)
                .setKey(remitenteId ?: "pareja")
                .build()

            val messagingStyle = NotificationCompat.MessagingStyle(sender)
                .addMessage(cuerpo, System.currentTimeMillis(), sender)
                .setConversationTitle(null) // Conversación 1 a 1

            builder.setStyle(messagingStyle)

            // AÑADIR RESPUESTA DIRECTA
            val remoteInput = RemoteInput.Builder("key_text_reply")
                .setLabel("Escribe un mensaje...")
                .build()

            val replyIntent = Intent(context, NotificationReplyReceiver::class.java).apply {
                action = "com.cadev.mocaapp.ACTION_REPLY"
                putExtra("notifId", notifId)
                putExtra("deepLink", deepLink)
                putExtra("remitenteId", remitenteId)
            }

            val replyPendingIntent = PendingIntent.getBroadcast(
                context,
                notifId + 100,
                replyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            val action = NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_send,
                "Responder",
                replyPendingIntent
            ).addRemoteInput(remoteInput).build()

            builder.addAction(action)
        } else {
            // Estilo normal para otras notificaciones
            builder.setContentTitle(titulo)
            builder.setContentText(cuerpo)
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
            
            if (fotoBitmap != null) {
                builder.setLargeIcon(fotoBitmap)
            } else {
                try {
                    val appIcon = context.packageManager.getApplicationIcon(context.packageName)
                    builder.setLargeIcon(convertToBitmap(appIcon))
                } catch (e: Exception) { }
            }
        }

        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                NotificationManagerCompat.from(context).notify(notifId, builder.build())
            }
        } catch (e: SecurityException) {
            Log.e("NotifHelper", "Error de seguridad al mostrar notificación", e)
        }
    }
}
