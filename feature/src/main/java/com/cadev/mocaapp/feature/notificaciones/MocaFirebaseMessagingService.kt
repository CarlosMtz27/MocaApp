package com.cadev.mocaapp.feature.notificaciones

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import com.cadev.mocaapp.core.model.TipoNotificacion
import com.cadev.mocaapp.feature.notas.data.repository.NotaRepositoryImpl
import com.cadev.mocaapp.feature.notas.widget.NotaWidget
import com.cadev.mocaapp.feature.notas.widget.NotaWidgetDataStore
import com.cadev.mocaapp.feature.notificaciones.data.NotificacionRepository
import com.cadev.mocaapp.feature.estadoanimo.data.repository.EstadoAnimoRepositoryImpl
import com.cadev.mocaapp.feature.widgets.estadoanimo.EstadoAnimoWidget
import com.cadev.mocaapp.feature.widgets.estadoanimo.EstadoAnimoWidgetDataStore
import com.cadev.mocaapp.feature.widgets.estadoanimo.EstadoAnimoWidgetTransparent
import com.cadev.mocaapp.feature.eventos.data.repository.EventoRepositoryImpl
import com.cadev.mocaapp.feature.widgets.eventos.EventosWidget
import com.cadev.mocaapp.feature.widgets.eventos.EventosWidgetDataStore
import com.cadev.mocaapp.feature.widgets.eventos.EventoWidgetInfo
import com.cadev.mocaapp.feature.widgets.eventos.EventosWidgetTransparent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * NUESTRO RECEPTOR DE MENSAJES EN LA NUBE
 * 
 * Qué hace:
 * Es el encargado de recibir los avisos que vienen de internet (Firebase). 
 * Cuando nuestra pareja nos escribe o cambia su estado, este servicio despierta 
 * la app, muestra la notificación y actualiza los widgets del escritorio 
 * automáticamente.
 */
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
        Log.d("MOCA_FCM", "Mensaje recibido de: ${message.from}")

        val data = message.data
        val tipoStr = data["tipo"] ?: ""
        Log.d("MOCA_FCM", "Datos recibidos: $data")

        val tipo = when (tipoStr) {
            "chat"         -> TipoNotificacion.CHAT
            "diario"       -> TipoNotificacion.DIARIO
            "cuestionario" -> TipoNotificacion.CUESTIONARIO
            "aniversario"  -> TipoNotificacion.ANIVERSARIO
            "nota"         -> TipoNotificacion.NOTA
            "evento"       -> TipoNotificacion.EVENTO
            "estado_animo" -> TipoNotificacion.ESTADO_ANIMO
            else           -> {
                Log.w("MOCA_FCM", "Tipo de notificación desconocido o nulo: $tipoStr")
                null
            }
        }

        val titulo = data["titulo"] ?: message.notification?.title ?: "MocaApp"
        val cuerpo = data["cuerpo"] ?: message.notification?.body ?: "Tienes una novedad"
        val deepLink = data["deepLink"] ?: ""

        if (tipo != null) {
            NotificationHelper.mostrar(
                context  = applicationContext,
                tipo     = tipo,
                titulo   = titulo,
                cuerpo   = cuerpo,
                deepLink = deepLink
            )
        }

        // Sincronización automática de Notas
        if (tipoStr == "nota") {
            val relacionId = data["relacionId"] ?: ""
            val autorId = data["autorId"] ?: ""
            if (relacionId.isNotBlank() && autorId.isNotBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        Log.d("MOCA_FCM", "Sincronizando nota...")
                        val repo = NotaRepositoryImpl(FirebaseFirestore.getInstance())
                        val result = repo.obtenerNota(relacionId, autorId)
                        result.onSuccess { nota ->
                            NotaWidgetDataStore.guardar(applicationContext, nota)
                            NotaWidget().updateAll(applicationContext)
                        }
                    } catch (e: Exception) { Log.e("MOCA_FCM", "Error sincronizando nota", e) }
                }
            }
        }

        // Sincronización automática de Estado de Ánimo
        if (tipoStr == "estado_animo") {
            val relacionId = data["relacionId"] ?: ""
            val uidPropio = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            
            if (relacionId.isNotBlank() && uidPropio.isNotBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        Log.d("MOCA_FCM", "Sincronizando estado de ánimo...")
                        val repo = EstadoAnimoRepositoryImpl(FirebaseFirestore.getInstance())
                        val estados = repo.obtenerEstados(relacionId, uidPropio)
                        
                        Log.d("MOCA_FCM", "Nuevos estados obtenidos: $estados")
                        
                        // Guardamos solo los emojis para no pisar los nombres en el widget
                        EstadoAnimoWidgetDataStore.actualizarSoloEmojis(applicationContext, estados)
                        
                        // Refrescamos todos los widgets de sentimientos
                        EstadoAnimoWidget().updateAll(applicationContext)
                        EstadoAnimoWidgetTransparent().updateAll(applicationContext)
                        
                        Log.d("MOCA_FCM", "Widgets de estado actualizados con éxito")
                    } catch (e: Exception) { Log.e("MOCA_FCM", "Error sincronizando estado", e) }
                }
            } else {
                Log.w("MOCA_FCM", "Faltan datos para sincronizar estado: rel=$relacionId, uid=$uidPropio")
            }
        }
        // Sincronización automática de Eventos
        if (tipoStr == "evento") {
            val relacionId = data["relacionId"] ?: ""
            if (relacionId.isNotBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val repo = EventoRepositoryImpl(FirebaseFirestore.getInstance())
                        repo.obtenerEventos(relacionId).onSuccess { lista ->
                            val hoy = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                            val proximos = lista.filter { it.fecha >= hoy }.map { 
                                EventoWidgetInfo(it.titulo, it.fecha, it.hora, it.id)
                            }
                            EventosWidgetDataStore.guardarEventos(applicationContext, proximos)
                            EventosWidget().updateAll(applicationContext)
                            EventosWidgetTransparent().updateAll(applicationContext)
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }
        }
    }
}
