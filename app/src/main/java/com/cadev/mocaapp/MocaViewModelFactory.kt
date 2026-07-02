package com.cadev.mocaapp

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cadev.mocaapp.BuildConfig
import com.cadev.mocaapp.feature.auth.data.repository.AuthRepositoryImpl
import com.cadev.mocaapp.feature.auth.ui.AuthViewModel
import com.cadev.mocaapp.feature.chat.data.repository.ChatRepositoryImpl
import com.cadev.mocaapp.feature.chat.ui.ChatViewModel
import com.cadev.mocaapp.feature.cuestionarios.data.repository.CuestionarioRepositoryImpl
import com.cadev.mocaapp.feature.cuestionarios.ui.CuestionarioViewModel
import com.cadev.mocaapp.feature.diario.data.repository.DiarioRepositoryImpl
import com.cadev.mocaapp.feature.diario.ui.DiarioViewModel
import com.cadev.mocaapp.feature.eventos.data.repository.EventoRepositoryImpl
import com.cadev.mocaapp.feature.eventos.ui.EventoViewModel
import com.cadev.mocaapp.feature.estadoanimo.data.repository.EstadoAnimoRepositoryImpl
import com.cadev.mocaapp.feature.estadoanimo.ui.EstadoAnimoViewModel
import com.cadev.mocaapp.feature.notas.data.repository.NotaRepositoryImpl
import com.cadev.mocaapp.feature.notas.ui.NotaViewModel
import com.cadev.mocaapp.feature.notificaciones.data.NotificacionRepository
import com.cadev.mocaapp.feature.notificaciones.ui.NotificacionViewModel
import com.cadev.mocaapp.feature.pareja.data.repository.ParejaRepositoryImpl
import com.cadev.mocaapp.feature.pareja.ui.ParejaViewModel
import com.cadev.mocaapp.feature.perfil.data.repository.PerfilRepositoryImpl
import com.cadev.mocaapp.feature.perfil.ui.PerfilViewModel
import com.cadev.mocaapp.feature.widgets.ui.WidgetsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * LA FÁBRICA DE GESTORES DE DATOS
 * 
 * Qué hace
 * Los gestores de datos o ViewModels son los que preparan la información para las pantallas. 
 * Esta clase se encarga de crearlos dándoles las herramientas necesarias como el acceso a la base de datos 
 * de Firestore o al sistema de notificaciones.
 * 
 * Cómo añadir más cosas
 * Cuando crees una funcionalidad nueva con su propio gestor de datos debes venir aquí y añadirlo al listado. 
 * Crea una nueva sección dentro de la función indicando el nombre de tu nuevo gestor y pásale el almacén de datos 
 * que corresponda.
 */
class MocaViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    // Se obtienen las herramientas de identificación y de base de datos de Google
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Se prepara el sistema de notificaciones para que todos los gestores puedan usar el mismo
    private val notificacionRepository = NotificacionRepository(
        firestore        = firestore,
        oneSignalAppId   = BuildConfig.ONESIGNAL_APP_ID,
        oneSignalRestKey = BuildConfig.ONESIGNAL_REST_KEY
    )

    /**
     * Esta función crea el gestor de datos específico que solicita cada pantalla
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            // Gestor para entrar y registrarse en la aplicación
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(
                    AuthRepositoryImpl(auth, firestore)
                ) as T

            // Gestor para conectar y gestionar la relación de pareja
            modelClass.isAssignableFrom(ParejaViewModel::class.java) ->
                ParejaViewModel(
                    ParejaRepositoryImpl(firestore)
                ) as T

            // Gestor para el chat de mensajes entre la pareja
            modelClass.isAssignableFrom(ChatViewModel::class.java) ->
                ChatViewModel(
                    ChatRepositoryImpl(firestore),
                    notificacionRepository
                ) as T

            // Gestor para ver y editar la información del perfil
            modelClass.isAssignableFrom(PerfilViewModel::class.java) ->
                PerfilViewModel(
                    PerfilRepositoryImpl(auth, firestore),
                    notificacionRepository
                ) as T

            // Gestor para escribir en el diario y ver el calendario
            modelClass.isAssignableFrom(DiarioViewModel::class.java) ->
                DiarioViewModel(
                    DiarioRepositoryImpl(firestore),
                    notificacionRepository,
                    EventoRepositoryImpl(firestore)
                ) as T

            // Gestor para crear y responder los tests de pareja
            modelClass.isAssignableFrom(CuestionarioViewModel::class.java) ->
                CuestionarioViewModel(
                    CuestionarioRepositoryImpl(firestore),
                    notificacionRepository
                ) as T

            // Gestor para manejar los avisos y notificaciones dentro de la app
            modelClass.isAssignableFrom(NotificacionViewModel::class.java) ->
                NotificacionViewModel(
                    notificacionRepository
                ) as T

            // Gestor para organizar los eventos importantes de la pareja
            modelClass.isAssignableFrom(EventoViewModel::class.java) ->
                EventoViewModel(
                    EventoRepositoryImpl(firestore),
                    notificacionRepository
                ) as T

            // Gestor para el muro de notas compartidas
            modelClass.isAssignableFrom(NotaViewModel::class.java) ->
                NotaViewModel(
                    NotaRepositoryImpl(firestore),
                    notificacionRepository
                ) as T

            // Gestor para registrar cómo se siente el usuario cada día
            modelClass.isAssignableFrom(EstadoAnimoViewModel::class.java) ->
                EstadoAnimoViewModel(
                    EstadoAnimoRepositoryImpl(firestore),
                    notificacionRepository
                ) as T

            // Gestor para el catálogo de widgets
            modelClass.isAssignableFrom(WidgetsViewModel::class.java) ->
                WidgetsViewModel(application) as T

            // Si se pide un gestor que no está en la lista la aplicación dará un error avisando de ello
            else -> throw IllegalArgumentException(
                "El gestor de datos no está registrado en la fábrica"
            )
        }
    }
}
