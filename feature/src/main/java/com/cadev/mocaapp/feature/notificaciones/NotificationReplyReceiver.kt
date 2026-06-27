package com.cadev.mocaapp.feature.notificaciones

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.cadev.mocaapp.feature.chat.data.repository.ChatRepositoryImpl
import com.cadev.mocaapp.feature.chat.domain.model.Mensaje
import com.cadev.mocaapp.feature.chat.domain.model.TipoMensaje
import com.cadev.mocaapp.feature.notificaciones.data.NotificacionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * RECEPTOR DE RESPUESTAS RÁPIDAS
 * 
 * Qué hace:
 * Permite responder mensajes directamente desde la notificación sin tener 
 * que abrir la aplicación. Envía el texto a Firestore y actualiza los contadores.
 */
class NotificationReplyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "com.cadev.mocaapp.ACTION_REPLY") return

        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        val respuesta = remoteInput?.getCharSequence("key_text_reply")?.toString()
        val notifId = intent.getIntExtra("notifId", 0)
        val parejaId = intent.getStringExtra("remitenteId") ?: ""
        val usuarioId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (respuesta.isNullOrBlank() || parejaId.isBlank() || usuarioId.isBlank()) return

        val firestore = FirebaseFirestore.getInstance()
        val chatRepo = ChatRepositoryImpl(firestore)
        val notifRepo = NotificacionRepository(
            firestore = firestore,
            oneSignalAppId = com.cadev.mocaapp.feature.BuildConfig.ONESIGNAL_APP_ID,
            oneSignalRestKey = com.cadev.mocaapp.feature.BuildConfig.ONESIGNAL_REST_KEY
        )

        val conversacionId = chatRepo.obtenerConversacionId(usuarioId, parejaId)
        val mensaje = Mensaje(
            conversacionId = conversacionId,
            remitenteId = usuarioId,
            texto = respuesta,
            tipo = TipoMensaje.TEXTO.name
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Obtenemos los datos del usuario desde Firestore en lugar de confiar en el perfil de Auth
                val userDoc = firestore.collection("usuarios").document(usuarioId).get().await()
                val miNombre = userDoc.getString("nombre") ?: "Alguien"
                val miFoto = userDoc.getString("fotoPerfil") ?: userDoc.getString("fotoUrl")

                // Enviar mensaje a Firestore
                chatRepo.enviarMensaje(mensaje)

                notifRepo.incrementarBadge(parejaId, "chat")
                notifRepo.enviarPush(
                    parejaId = parejaId,
                    titulo = miNombre,
                    cuerpo = respuesta,
                    deepLink = "main/chat",
                    tipo = "chat",
                    fotoUrl = miFoto,
                    remitenteId = usuarioId
                )

                // Actualizar la notificación local para mostrar que se envió
                NotificationManagerCompat.from(context).cancel(notifId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
