package com.cadev.mocaapp.feature.widgets.diasjuntos

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cadev.mocaapp.feature.perfil.data.repository.PerfilRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * CONTADOR DE DÍAS EN SEGUNDO PLANO
 * 
 * Qué hace:
 * Se encarga de revisar cada día cuántos días llevamos juntos. Consulta la 
 * base de datos, calcula el tiempo transcurrido y actualiza la información 
 * para que el widget siempre muestre el número correcto.
 */
class DiasJuntosWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val uid = auth.currentUser?.uid ?: return Result.failure()

        val repo = PerfilRepositoryImpl(auth, db)
        val dataStore = DiasJuntosWidgetDataStore(applicationContext)

        return try {
            // Buscamos la fecha en la que empezó todo
            val result = repo.obtenerFechaRelacion(uid)
            result.onSuccess { fecha ->
                if (fecha != null) {
                    val dias = calcularDias(fecha)
                    dataStore.saveData(dias, fecha, true)
                    
                    // Pedimos a todos los widgets de días que se refresquen
                    DiasJuntosWidget().updateAll(applicationContext)
                    DiasJuntosWidgetTransparent().updateAll(applicationContext)
                    DiasJuntosWidgetMini().updateAll(applicationContext)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun calcularDias(fecha: String): Long {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val inicio = sdf.parse(fecha) ?: return 0
            val hoy = java.util.Date()
            val diff = hoy.time - inicio.time
            java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff)
        } catch (e: Exception) { 0 }
    }
}
