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

/**
 * RASTREADOR DE DISTANCIA
 * 
 * Qué hace:
 * Es el motor que trabaja en segundo plano para obtener nuestra ubicación actual 
 * y la de nuestra pareja. Calcula cuántos kilómetros nos separan y guarda esa 
 * información para que el widget del escritorio siempre esté actualizado.
 * 
 * Cómo lo podemos modificar:
 * Si queremos que el cálculo sea más preciso, podemos cambiar la prioridad de 
 * `PRIORITY_BALANCED_POWER_ACCURACY` a `PRIORITY_HIGH_ACCURACY`.
 */
class UbicacionWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val uid = auth.currentUser?.uid ?: return Result.failure()

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        val dataStore = DistanciaWidgetDataStore(applicationContext)

        // Verificamos si tenemos los permisos necesarios para usar el GPS
        val hasFine = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
        if (!hasFine && !hasCoarse) {
            Log.e("UbicacionWorker", "Sin permisos de ubicación")
            return Result.failure()
        }

        return try {
            /**
             * OBTENER UBICACIÓN:
             * Intentamos conseguir la posición exacta de este momento. Si falla, 
             * usamos la última posición que el móvil recuerde.
             */
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

                /**
                 * 1. SUBIR MI POSICIÓN:
                 * Enviamos nuestra latitud y longitud a Firebase para que nuestra 
                 * pareja también pueda verla.
                 */
                db.collection("usuarios").document(uid).update(
                    "ubicacion", mapOf(
                        "lat" to lat,
                        "lng" to lng,
                        "actualizadaEn" to ahora
                    )
                ).await()

                if (parejaId.isNullOrBlank()) {
                    updateTextOnly(dataStore, "¡Vincúlate con tu pareja!")
                    return Result.success()
                }

                /**
                 * 2. BUSCAR A MI PAREJA:
                 * Descargamos la última posición conocida de nuestra pareja para 
                 * poder hacer la comparación.
                 */
                val parejaDoc = db.collection("usuarios").document(parejaId).get().await()
                
                val miNombre = miDoc.getString("nombre") ?: "Yo"
                val miFotoUrl = miDoc.getString("fotoPerfil") ?: miDoc.getString("fotoUrl")
                val parejaNombre = parejaDoc.getString("nombre") ?: "Pareja"
                val parejaFotoUrl = parejaDoc.getString("fotoPerfil") ?: parejaDoc.getString("fotoUrl")
                
                val parejaUbicacion = parejaDoc.get("ubicacion") as? Map<*, *>
                val pLat = parejaUbicacion?.get("lat") as? Double
                val pLng = parejaUbicacion?.get("lng") as? Double

                /**
                 * CÁLCULO MATEMÁTICO:
                 * Si tenemos ambas posiciones, calculamos la distancia "en línea recta" 
                 * y la formateamos para que sea fácil de leer (m o km).
                 */
                val distanciaTexto = if (pLat != null && pLng != null) {
                    val dist = calcularDistanciaKm(lat, lng, pLat, pLng)
                    formatearDistancia(dist)
                } else {
                    "Esperando ubicación de pareja..."
                }

                /**
                 * 3. DESCARGAR FOTOS:
                 * Bajamos las fotos de perfil para que el widget no tenga que 
                 * cargarlas de internet cada vez que se redibuja.
                 */
                val miFotoPath = downloadImage(applicationContext, miFotoUrl, "mi_foto.jpg")
                val parejaFotoPath = downloadImage(applicationContext, parejaFotoUrl, "pareja_foto.jpg")

                /**
                 * 4. GUARDAR EN MEMORIA LOCAL (DATASTORE):
                 * Registramos todo lo calculado para que el widget lo lea.
                 */
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

                /**
                 * 5. ACTUALIZAR WIDGET:
                 * Le avisamos al móvil que redibuje el widget del escritorio ahora mismo.
                 */
                DistanciaWidget().updateAll(applicationContext)
                
                Result.success()
            } else {
                Log.w("UbicacionWorker", "Ubicación null")
                if (parejaId != null) {
                    updateTextOnly(dataStore, "Buscando señal GPS...")
                }
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("UbicacionWorker", "Error en worker", e)
            Result.failure()
        }
    }

    /**
     * Actualiza solo el texto del widget sin cambiar las fotos de perfil.
     */
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

    /**
     * MÉTODOS DE CÁLCULO:
     * Implementamos la fórmula de Haversine para saber la distancia entre 
     * dos puntos del planeta Tierra.
     */
    private fun calcularDistanciaKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        return r * 2 * asin(sqrt(a))
    }

    /**
     * Convierte el número de kilómetros a un texto bonito (m si es cerca, km si es lejos).
     */
    private fun formatearDistancia(km: Double): String = when {
        km < 0.1 -> "Están juntos"
        km < 1.0 -> "${(km * 1000).toInt()} m"
        km < 10.0 -> "%.1f km".format(km)
        else -> "${km.toInt()} km"
    }

    /**
     * GESTOR DE IMÁGENES:
     * Descarga la foto de internet y la guarda en la carpeta privada de la app.
     */
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
