package com.cadev.mocaapp.feature.notification

import android.Manifest
import androidx.annotation.RequiresPermission
import com.cadev.mocaapp.core.model.TipoNotificacion
import com.cadev.mocaapp.feature.notificaciones.data.NotificacionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MocaFirebaseMessagingService : FirebaseMessagingService() {

    private val repository by lazy {
        NotificacionRepository(FirebaseFirestore.getInstance())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            repository.guardarToken(uid, token)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val tipo = when (data["tipo"]) {
            "chat"         -> TipoNotificacion.CHAT
            "diario"       -> TipoNotificacion.DIARIO
            "cuestionario" -> TipoNotificacion.CUESTIONARIO
            "aniversario"  -> TipoNotificacion.ANIVERSARIO
            else           -> return
        }

        val titulo   = data["titulo"]   ?: message.notification?.title ?: return
        val cuerpo   = data["cuerpo"]   ?: message.notification?.body  ?: return
        val deepLink = data["deepLink"] ?: ""

        NotificationHelper.mostrar(
            context  = applicationContext,
            tipo     = tipo,
            titulo   = titulo,
            cuerpo   = cuerpo,
            deepLink = deepLink
        )
    }
}