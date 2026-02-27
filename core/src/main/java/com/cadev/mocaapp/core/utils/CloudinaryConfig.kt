package com.cadev.mocaapp.core.utils

import android.content.Context
import com.cloudinary.android.MediaManager

object CloudinaryConfig {

    private var inicializado = false

    fun inicializar(
        context: Context,
        cloudName: String,
        apiKey: String,
        apiSecret: String
    ) {
        if (inicializado) return

        val config = mapOf(
            "cloud_name" to cloudName,
            "api_key"    to apiKey,
            "api_secret" to apiSecret
        )

        MediaManager.init(context, config)
        inicializado = true
    }
}