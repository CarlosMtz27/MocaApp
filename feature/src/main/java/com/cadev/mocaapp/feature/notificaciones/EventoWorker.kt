package com.cadev.mocaapp.feature.notificaciones

import android.content.Context
import androidx.work.*
import com.cadev.mocaapp.core.model.TipoNotificacion
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class EventoWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val titulo   = inputData.getString("titulo")   ?: return Result.failure()
        val cuerpo   = inputData.getString("cuerpo")   ?: ""
        val eventoId = inputData.getString("eventoId") ?: return Result.failure()

        NotificationHelper.mostrar(
            context  = applicationContext,
            tipo     = TipoNotificacion.EVENTO,
            titulo   = titulo,
            cuerpo   = cuerpo.ifBlank { "¡Hoy es el día!" },
            deepLink = "main/detalle_evento/$eventoId"
        )
        return Result.success()
    }

    companion object {
        fun programar(
            context: Context,
            eventoId: String,
            titulo: String,
            descripcion: String,
            fecha: String,
            hora: String,
            minutosAntes: Int
        ) {
            val formato = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val fechaHora = try {
                formato.parse("$fecha $hora") ?: return
            } catch (e: Exception) { return }

            val tiempoDisparo = fechaHora.time - (minutosAntes * 60 * 1000L)
            val delay = tiempoDisparo - System.currentTimeMillis()
            if (delay <= 0) return

            val data = Data.Builder()
                .putString("titulo",   titulo)
                .putString("cuerpo",   descripcion)
                .putString("eventoId", eventoId)
                .build()

            val request = OneTimeWorkRequestBuilder<EventoWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("evento_$eventoId")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "evento_$eventoId",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        fun cancelar(context: Context, eventoId: String) {
            WorkManager.getInstance(context).cancelUniqueWork("evento_$eventoId")
        }
    }
}
