package com.cadev.mocaapp.feature.estadoanimo.data.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cadev.mocaapp.feature.widgets.estadoanimo.EstadoAnimoWidget
import com.cadev.mocaapp.feature.widgets.estadoanimo.EstadoAnimoWidgetDataStore
import java.text.SimpleDateFormat
import java.util.*

class ResetEstadoAnimoWorker(ctx: Context, params: WorkerParameters) 
    : CoroutineWorker(ctx, params) {
    
    override suspend fun doWork(): Result {
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dataStore = EstadoAnimoWidgetDataStore.obtener(applicationContext)
        
        // Si la fecha guardada != hoy, limpiar emoji propio
        if (dataStore.fecha != hoy) {
            EstadoAnimoWidgetDataStore.limpiarPropio(applicationContext)
            EstadoAnimoWidget().updateAll(applicationContext)
        }
        
        return Result.success()
    }
}
