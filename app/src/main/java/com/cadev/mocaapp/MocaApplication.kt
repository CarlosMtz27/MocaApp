package com.cadev.mocaapp

import android.app.Application
import androidx.work.*
import com.cadev.mocaapp.feature.notification.NotificationChannels
import com.cadev.mocaapp.feature.widgets.diasjuntos.DiasJuntosWorker
import com.cadev.mocaapp.feature.widgets.distancia.UbicacionWorker
import com.cadev.mocaapp.feature.estadoanimo.data.worker.ResetEstadoAnimoWorker
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import java.util.concurrent.TimeUnit

/**
 * ESTA ES LA CLASE PRINCIPAL DE LA APLICACIÓN 
 * 
 * Qué hace
 * Es lo primero que se ejecuta cuando abres la aplicación. Aquí se configuran los ajustes globales 
 * que deben estar listos antes de que el usuario vea cualquier pantalla. 
 * Se configuran las notificaciones y se programan las tareas que se ejecutan solas cada cierto tiempo.
 * 
 * Cómo añadir más cosas
 * Si quieres que algo se inicialice al arrancar como una librería nueva ponlo dentro de la función onCreate. 
 * Si quieres una nueva tarea repetitiva como limpiar datos viejos crea un nuevo Worker y prográmalo 
 * aquí mismo siguiendo el ejemplo de los otros.
 */
class MocaApplication : Application() {
    
    /**
     * Esta función se ejecuta automáticamente cuando la aplicación comienza a funcionar
     */
    override fun onCreate() {
        super.onCreate()
        
        // Se crean los canales de notificación para que el sistema Android sepa separar los avisos por categorías
        NotificationChannels.crearTodos(this)

        // Se configura el sistema de mensajes OneSignal para recibir notificaciones en el móvil
        OneSignal.Debug.logLevel = LogLevel.NONE
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)

        // TAREAS AUTOMÁTICAS EN SEGUNDO PLANO

        // Se prepara la tarea para actualizar la ubicación cada quince minutos necesaria para el widget de distancia
        val ubicacionRequest = PeriodicWorkRequestBuilder<UbicacionWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED) // Solo se ejecuta si hay internet
                    .build()
            )
            .build()

        // Se registra la tarea de ubicación para que Android la gestione por su cuenta
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ubicacion_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            ubicacionRequest
        )

        // Se prepara la tarea que cuenta los días que lleva la pareja para que se actualice cada día
        val diasJuntosRequest = PeriodicWorkRequestBuilder<DiasJuntosWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(calcularDelayHastaMedianoche(), TimeUnit.MILLISECONDS) // Espera a que sea medianoche para empezar
            .build()

        // Se registra la tarea de los días de relación
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "dias_juntos_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            diasJuntosRequest
        )

        // Se prepara la tarea para borrar el estado de ánimo diario cuando termine el día
        val resetEstadoRequest = PeriodicWorkRequestBuilder<ResetEstadoAnimoWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(calcularDelayHastaMedianoche(), TimeUnit.MILLISECONDS)
            .build()

        // Se registra la tarea de borrado del estado de ánimo
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "reset_estado_animo",
            ExistingPeriodicWorkPolicy.KEEP,
            resetEstadoRequest
        )
    }

    /**
     * Esta función calcula cuánto tiempo falta desde ahora mismo hasta las doce de la noche del día siguiente
     */
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
