package com.cadev.mocaapp.feature.widgets.diasjuntos

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DiasJuntosWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid ?: return Result.failure()

        return try {
            val userDoc = firestore.collection("usuarios").document(userId).get().await()
            val relacionId = userDoc.getString("relacionId")
            
            if (relacionId.isNullOrBlank()) {
                updateDataStore(0, "", false)
                DiasJuntosWidget().updateAll(applicationContext)
                return Result.success()
            }

            val relacionDoc = firestore.collection("relaciones").document(relacionId).get().await()
            val timestamp = relacionDoc.getTimestamp("fechaInicio")

            if (timestamp != null) {
                val fechaInicio = timestamp.toDate()
                val dias = calcularDiasJuntos(fechaInicio)
                val fechaTexto = SimpleDateFormat("d 'de' MMMM, yyyy", Locale("es", "ES")).format(fechaInicio)
                updateDataStore(dias, fechaTexto, true)
            } else {
                updateDataStore(0, "", false)
            }

            DiasJuntosWidget().updateAll(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun updateDataStore(dias: Int, fechaTexto: String, configurado: Boolean) {
        val dataStore = DiasJuntosWidgetDataStore(applicationContext)
        dataStore.saveData(dias, fechaTexto, configurado)
    }

    private fun calcularDiasJuntos(inicio: Date): Int {
        val hoy = Date()
        val diff = hoy.time - inicio.time
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }
}
