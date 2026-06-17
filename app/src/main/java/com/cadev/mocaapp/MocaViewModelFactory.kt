package com.cadev.mocaapp

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
import com.cadev.mocaapp.feature.notificaciones.data.NotificacionRepository
import com.cadev.mocaapp.feature.notificaciones.ui.NotificacionViewModel
import com.cadev.mocaapp.feature.pareja.data.repository.ParejaRepositoryImpl
import com.cadev.mocaapp.feature.pareja.ui.ParejaViewModel
import com.cadev.mocaapp.feature.perfil.data.repository.PerfilRepositoryImpl
import com.cadev.mocaapp.feature.perfil.ui.PerfilViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MocaViewModelFactory : ViewModelProvider.Factory {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Una sola instancia compartida con las keys inyectadas
    private val notificacionRepository = NotificacionRepository(
        firestore        = firestore,
        oneSignalAppId   = BuildConfig.ONESIGNAL_APP_ID,
        oneSignalRestKey = BuildConfig.ONESIGNAL_REST_KEY
    )

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(
                    AuthRepositoryImpl(auth, firestore)
                ) as T

            modelClass.isAssignableFrom(ParejaViewModel::class.java) ->
                ParejaViewModel(
                    ParejaRepositoryImpl(firestore)
                ) as T

            modelClass.isAssignableFrom(ChatViewModel::class.java) ->
                ChatViewModel(
                    ChatRepositoryImpl(firestore),
                    notificacionRepository       // instancia compartida
                ) as T

            modelClass.isAssignableFrom(PerfilViewModel::class.java) ->
                PerfilViewModel(
                    PerfilRepositoryImpl(auth, firestore)
                ) as T

            modelClass.isAssignableFrom(DiarioViewModel::class.java) ->
                DiarioViewModel(
                    DiarioRepositoryImpl(firestore),
                    notificacionRepository       // instancia compartida
                ) as T

            modelClass.isAssignableFrom(CuestionarioViewModel::class.java) ->
                CuestionarioViewModel(
                    CuestionarioRepositoryImpl(firestore),
                    notificacionRepository       // instancia compartida
                ) as T

            modelClass.isAssignableFrom(NotificacionViewModel::class.java) ->
                NotificacionViewModel(
                    notificacionRepository       // instancia compartida
                ) as T

            modelClass.isAssignableFrom(EventoViewModel::class.java) ->
                EventoViewModel(
                    EventoRepositoryImpl(firestore),
                    notificacionRepository
                ) as T

            else -> throw IllegalArgumentException(
                "ViewModel no registrado: ${modelClass.name}"
            )
        }
    }
}