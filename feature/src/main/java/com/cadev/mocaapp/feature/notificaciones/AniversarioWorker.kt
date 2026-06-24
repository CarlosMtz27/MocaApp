package com.cadev.mocaapp.feature.notificaciones

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.work.*
import androidx.work.WorkerParameters
import com.cadev.mocaapp.core.model.TipoNotificacion
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AniversarioWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val titulo   = inputData.getString("titulo")   ?: return Result.success()
        val cuerpo   = inputData.getString("cuerpo")   ?: return Result.success()
        val deepLink = inputData.getString("deepLink") ?: ""

        NotificationHelper.mostrar(
            context  = applicationContext,
            tipo     = TipoNotificacion.ANIVERSARIO,
            titulo   = titulo,
            cuerpo   = cuerpo,
            deepLink = deepLink,
            notifId  = 999
        )
        return Result.success()
    }

    companion object {
        fun programar(context: Context, fechaRelacion: String) {
            try {
                val partes = fechaRelacion.split("-")
                val anioInicio    = partes[0].toInt()
                val mesAniversario = partes[1].toInt()
                val diaAniversario = partes[2].toInt()

                val ahora = Calendar.getInstance()
                val proximo = Calendar.getInstance().apply {
                    set(Calendar.MONTH, mesAniversario - 1)
                    set(Calendar.DAY_OF_MONTH, diaAniversario)
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (!after(ahora)) add(Calendar.YEAR, 1)
                }

                val demora = proximo.timeInMillis - ahora.timeInMillis
                val anios  = proximo.get(Calendar.YEAR) - anioInicio

                val data = workDataOf(
                    "titulo"   to "¡Feliz aniversario!",
                    "cuerpo"   to "Hoy cumplen $anios año${if (anios != 1) "s" else ""} juntos. ¡Celébralo con tu pareja!",
                    "deepLink" to "main/perfil"
                )

                WorkManager.getInstance(context)
                    .enqueueUniqueWork(
                        "aniversario_anual",
                        ExistingWorkPolicy.REPLACE,
                        OneTimeWorkRequestBuilder<AniversarioWorker>()
                            .setInitialDelay(demora, TimeUnit.MILLISECONDS)
                            .setInputData(data)
                            .addTag("aniversario")
                            .build()
                    )
            } catch (e: Exception) { }
        }
    }
}
