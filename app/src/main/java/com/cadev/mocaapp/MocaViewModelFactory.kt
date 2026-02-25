package com.cadev.mocaapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cadev.mocaapp.feature.auth.data.repository.AuthRepositoryImpl
import com.cadev.mocaapp.feature.auth.ui.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Este Factory vive en app/ — el único módulo que puede
// conocer todos los demás. Es el "ensamblador" de ViewModels.
// Cuando agreguemos Hilt más adelante, este archivo desaparece
// y Hilt lo hace automáticamente.

class MocaViewModelFactory : ViewModelProvider.Factory {

    // Firebase se instancia una sola vez aquí
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(
                    AuthRepositoryImpl(auth, firestore)
                ) as T


            else -> throw IllegalArgumentException(
                "ViewModel no registrado: ${modelClass.name}"
            )
        }
    }
}