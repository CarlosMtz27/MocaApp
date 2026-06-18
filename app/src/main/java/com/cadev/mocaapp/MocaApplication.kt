package com.cadev.mocaapp
import android.app.Application
import androidx.work.*
import com.cadev.mocaapp.feature.notification.NotificationChannels
import com.cadev.mocaapp.feature.widgets.diasjuntos.DiasJuntosWorker
import com.cadev.mocaapp.feature.widgets.distancia.UbicacionWorker
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import java.util.concurrent.TimeUnit

class MocaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.crearTodos(this)

        // OneSignal init
        OneSignal.Debug.logLevel = LogLevel.NONE
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)

        // Programar worker de ubicación para el widget
        val ubicacionRequest = PeriodicWorkRequestBuilder<UbicacionWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ubicacion_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            ubicacionRequest
        )

        // Programar worker de días juntos
        val diasJuntosRequest = PeriodicWorkRequestBuilder<DiasJuntosWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(calcularDelayHastaMedianoche(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "dias_juntos_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            diasJuntosRequest
        )
    }

    private fun calcularDelayHastaMedianoche(): Long {
        val ahora = java.util.Calendar.getInstance()
        val medianoche = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.DAY_OF_YEAR, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return medianoche.timeInMillis - ahora.timeInMillis
    }
}
