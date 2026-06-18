package com.cadev.mocaapp.feature.notification

import android.Manifest
import androidx.annotation.RequiresPermission
import com.cadev.mocaapp.core.model.TipoNotificacion
import com.cadev.mocaapp.feature.notas.data.repository.NotaRepositoryImpl
import com.cadev.mocaapp.feature.notas.widget.NotaWidget
import com.cadev.mocaapp.feature.notas.widget.NotaWidgetDataStore
import com.cadev.mocaapp.feature.notificaciones.data.NotificacionRepository
import com.cadev.mocaapp.feature.estadoanimo.data.repository.EstadoAnimoRepositoryImpl
import com.cadev.mocaapp.feature.widgets.estadoanimo.EstadoAnimoWidget
import com.cadev.mocaapp.feature.widgets.estadoanimo.EstadoAnimoWidgetDataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.glance.appwidget.updateAll
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
        val tipoStr = data["tipo"]
        val tipo = when (tipoStr) {
            "chat"         -> TipoNotificacion.CHAT
            "diario"       -> TipoNotificacion.DIARIO
            "cuestionario" -> TipoNotificacion.CUESTIONARIO
            "aniversario"  -> TipoNotificacion.ANIVERSARIO
            "nota"         -> TipoNotificacion.NOTA
            "evento"       -> TipoNotificacion.EVENTO
            "estado_animo" -> TipoNotificacion.CHAT
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

        if (tipoStr == "nota") {
            val relacionId = data["relacionId"] ?: ""
            val autorId = data["autorId"] ?: ""
            if (relacionId.isNotBlank() && autorId.isNotBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val repo = NotaRepositoryImpl(FirebaseFirestore.getInstance())
                        val result = repo.obtenerNota(relacionId, autorId)
                        result.onSuccess { nota ->
                            NotaWidgetDataStore.guardar(applicationContext, nota)
                            NotaWidget().updateAll(applicationContext)
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }
        }

        if (tipoStr == "estado_animo") {
            val relacionId = data["relacionId"] ?: ""
            val uidPropio = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (relacionId.isNotBlank() && uidPropio.isNotBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val repo = EstadoAnimoRepositoryImpl(FirebaseFirestore.getInstance())
                        val estados = repo.obtenerEstados(relacionId, uidPropio)
                        EstadoAnimoWidgetDataStore.guardar(applicationContext, estados)
                        EstadoAnimoWidget().updateAll(applicationContext)
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }
        }
    }
}
