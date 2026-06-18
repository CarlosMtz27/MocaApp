package com.cadev.mocaapp.feature.widgets.distancia

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.math.*
import java.text.SimpleDateFormat
import java.util.*

class UbicacionWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val uid = auth.currentUser?.uid ?: return Result.failure()

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        val dataStore = DistanciaWidgetDataStore(applicationContext)

        val hasFine = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
        if (!hasFine && !hasCoarse) {
            Log.e("UbicacionWorker", "Sin permisos de ubicación")
            return Result.failure()
        }

        return try {
            // Intentar obtener ubicación actual, si falla usar la última conocida
            val location = try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()
                    ?: fusedLocationClient.lastLocation.await()
            } catch (e: Exception) {
                fusedLocationClient.lastLocation.await()
            }
            
            val miDoc = db.collection("usuarios").document(uid).get().await()
            val parejaId = miDoc.getString("parejaId")

            if (location != null) {
                val lat = location.latitude
                val lng = location.longitude
                val ahora = com.google.firebase.Timestamp.now()

                // 1. Subir mi ubicación
                db.collection("usuarios").document(uid).update(
                    "ubicacion", mapOf(
                        "lat" to lat,
                        "lng" to lng,
                        "actualizadaEn" to ahora
                    )
                ).await()

                if (parejaId == null) {
                    updateTextOnly(dataStore, "¡Vincúlate con tu pareja!")
                    return Result.success()
                }

                val parejaDoc = db.collection("usuarios").document(parejaId).get().await()
                
                val miNombre = miDoc.getString("nombre") ?: "Yo"
                val miFotoUrl = miDoc.getString("fotoPerfil") ?: miDoc.getString("fotoUrl")
                val parejaNombre = parejaDoc.getString("nombre") ?: "Pareja"
                val parejaFotoUrl = parejaDoc.getString("fotoPerfil") ?: parejaDoc.getString("fotoUrl")
                
                val parejaUbicacion = parejaDoc.get("ubicacion") as? Map<*, *>
                val pLat = parejaUbicacion?.get("lat") as? Double
                val pLng = parejaUbicacion?.get("lng") as? Double

                val distanciaTexto = if (pLat != null && pLng != null) {
                    val dist = calcularDistanciaKm(lat, lng, pLat, pLng)
                    formatearDistancia(dist)
                } else {
                    "📍 Esperando ubicación de pareja..."
                }

                // 3. Descargar fotos
                val miFotoPath = downloadImage(applicationContext, miFotoUrl, "mi_foto.jpg")
                val parejaFotoPath = downloadImage(applicationContext, parejaFotoUrl, "pareja_foto.jpg")

                // 4. Guardar en DataStore
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                val actualizadaStr = sdf.format(Date())

                val currentData = dataStore.widgetData.first()

                dataStore.saveData(
                    foto1Path = if (miFotoPath.isNotEmpty()) miFotoPath else currentData.foto1Path,
                    nombre1 = miNombre,
                    foto2Path = if (parejaFotoPath.isNotEmpty()) parejaFotoPath else currentData.foto2Path,
                    nombre2 = parejaNombre,
                    distanciaTexto = distanciaTexto,
                    actualizadaEn = actualizadaStr
                )

                // 5. Actualizar Widget
                DistanciaWidget().updateAll(applicationContext)
                
                Result.success()
            } else {
                Log.w("UbicacionWorker", "Ubicación null")
                if (parejaId != null) {
                    updateTextOnly(dataStore, "📍 Buscando señal GPS...")
                }
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("UbicacionWorker", "Error en worker", e)
            Result.failure()
        }
    }

    private suspend fun updateTextOnly(dataStore: DistanciaWidgetDataStore, texto: String) {
        val current = dataStore.widgetData.first()
        dataStore.saveData(
            current.foto1Path,
            current.nombre1,
            current.foto2Path,
            current.nombre2,
            texto,
            current.actualizadaEn
        )
        DistanciaWidget().updateAll(applicationContext)
    }

    private fun calcularDistanciaKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        return r * 2 * asin(sqrt(a))
    }

    private fun formatearDistancia(km: Double): String = when {
        km < 0.1 -> "Están juntos ❤️"
        km < 1.0 -> "${(km * 1000).toInt()} m"
        km < 10.0 -> "%.1f km".format(km)
        else -> "${km.toInt()} km"
    }

    private suspend fun downloadImage(context: Context, url: String?, fileName: String): String = withContext(Dispatchers.IO) {
        if (url.isNullOrBlank()) return@withContext ""
        try {
            val file = File(context.filesDir, fileName)
            val connection = URL(url).openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()
            val inputStream = connection.getInputStream()
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("UbicacionWorker", "Error descargando imagen: $url", e)
            ""
        }
    }
}
