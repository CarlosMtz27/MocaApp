package com.cadev.mocaapp.feature.notificaciones.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ContadoresBadge(
    val chat: Int = 0,
    val diario: Int = 0,
    val cuestionarios: Int = 0,
    val nota: Int = 0,
    val estadoAnimo: Int = 0
)

/**
 * EL MOTOR DE LOS AVISOS (FIREBASE Y ONESIGNAL)
 * 
 * Qué hace:
 * Aquí escribimos la lógica para que las notificaciones lleguen de verdad al 
 * móvil de nuestra pareja. Usamos OneSignal para enviar los avisos por internet 
 * y Firestore para llevar la cuenta de cuántos mensajes sin leer tenemos.
 * 
 * Cómo lo podemos modificar:
 * Si queremos añadir un nuevo contador (ej: para "Citas"), debemos actualizar 
 * el objeto `ContadoresBadge` y la función `escucharNoLeidos`.
 */
class NotificacionRepository(
    private val firestore: FirebaseFirestore,
    private val oneSignalAppId: String = "",
    private val oneSignalRestKey: String = ""
) {
    private fun contadoresRef(usuarioId: String) = firestore
        .collection("notificaciones")
        .document(usuarioId)
        .collection("noLeidos")
        .document("contadores")

    suspend fun guardarToken(usuarioId: String, token: String) {
        try {
            firestore.collection("usuarios")
                .document(usuarioId)
                .set(mapOf("fcmToken" to token), SetOptions.merge())
                .await()
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun guardarOneSignalPlayerId(usuarioId: String, playerId: String) {
        try {
            firestore.collection("usuarios")
                .document(usuarioId)
                .set(mapOf("oneSignalPlayerId" to playerId), SetOptions.merge())
                .await()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun escucharNoLeidos(usuarioId: String): Flow<ContadoresBadge> = callbackFlow {
        val listener = contadoresRef(usuarioId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        close()
                    } else {
                        trySend(ContadoresBadge())
                    }
                    return@addSnapshotListener
                }
                trySend(
                    ContadoresBadge(
                        chat         = snapshot?.getLong("chat")?.toInt() ?: 0,
                        diario       = snapshot?.getLong("diario")?.toInt() ?: 0,
                        cuestionarios = snapshot?.getLong("cuestionarios")?.toInt() ?: 0,
                        nota         = snapshot?.getLong("nota")?.toInt() ?: 0,
                        estadoAnimo  = snapshot?.getLong("estadoAnimo")?.toInt() ?: 0
                    )
                )
            }
        awaitClose { listener.remove() }
    }

    suspend fun incrementarBadge(usuarioId: String, tipo: String) {
        if (usuarioId.isBlank()) return
        try {
            contadoresRef(usuarioId).set(
                mapOf(tipo to FieldValue.increment(1)),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun limpiarBadge(usuarioId: String, tipo: String) {
        if (usuarioId.isBlank()) return
        try {
            contadoresRef(usuarioId).set(
                mapOf(tipo to 0),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun enviarPush(
        parejaId: String,
        titulo: String,
        cuerpo: String,
        deepLink: String,
        tipo: String? = null,
        fotoUrl: String? = null,
        remitenteId: String? = null,
        extraData: Map<String, String> = emptyMap()
    ) = withContext(Dispatchers.IO) {
        android.util.Log.d("PUSH", "enviarPush llamado: parejaId=$parejaId appId=$oneSignalAppId")

        if (parejaId.isBlank() || oneSignalAppId.isBlank()) {
            android.util.Log.e("PUSH", "Abortado: parejaId='$parejaId' appId='$oneSignalAppId'")
            return@withContext
        }
        try {
            val doc = firestore.collection("usuarios").document(parejaId).get().await()
            val playerId = doc.getString("oneSignalPlayerId")
            android.util.Log.d("PUSH", "playerId encontrado: $playerId")

            if (playerId.isNullOrBlank()) {
                android.util.Log.e("PUSH", "playerId nulo o vacío")
                return@withContext
            }

            val dataJson = JSONObject().apply {
                put("deepLink", deepLink)
                if (tipo != null) put("tipo", tipo)
                put("titulo", titulo)
                put("cuerpo", cuerpo)
                if (fotoUrl != null) put("fotoUrl", fotoUrl)
                if (remitenteId != null) put("remitenteId", remitenteId)
                extraData.forEach { (key, value) -> put(key, value) }
            }

            val json = JSONObject().apply {
                put("app_id", oneSignalAppId)
                put("include_player_ids", JSONArray().put(playerId))
                /**
                 * NOTIFICACIONES "DATA-ONLY" DE ALTA PRIORIDAD:
                 * Al no enviar 'headings' ni 'contents', el SDK de OneSignal no muestra nada automáticamente.
                 * Enviamos 'priority': 10 para que FCM le dé importancia alta y despierte la app incluso en reposo.
                 */
                put("data", dataJson)
                put("priority", 10) 
                put("android_background_data", true)
                put("content_available", true)
            }
            android.util.Log.d("PUSH", "Enviando Push Data-Only: $json")

            val connection = URL("https://onesignal.com/api/v1/notifications")
                .openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Authorization", "Basic $oneSignalRestKey")
                doOutput = true
                outputStream.write(json.toString().toByteArray(Charsets.UTF_8))
                
                val code = responseCode
                android.util.Log.d("PUSH", "Response code: $code")
                
                val response = if (code in 200..299) {
                    inputStream.bufferedReader().readText()
                } else {
                    errorStream?.bufferedReader()?.readText() ?: "Sin detalle de error"
                }
                android.util.Log.d("PUSH", "Response body: $response")
                disconnect()
            }
        } catch (e: Exception) {
            android.util.Log.e("PUSH", "Excepción: ${e.message}", e)
        }
    }
}
