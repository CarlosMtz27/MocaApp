package com.cadev.mocaapp.core.utils

import android.content.Context
import com.cloudinary.android.MediaManager

/**
 * ESTE OBJETO CONFIGURA EL ALMACÉN DE FOTOS
 * 
 * Qué hace
 * Aquí se preparan los ajustes necesarios para poder subir y guardar imágenes en internet. 
 * Se asegura de que la configuración solo se realice una vez al abrir la aplicación.
 */
object CloudinaryConfig {

    /**
     * Esta variable sirve para saber si ya se han configurado los ajustes previamente
     */
    private var inicializado = false

    /**
     * Esta función activa la conexión con el servicio de guardado de imágenes usando las llaves de acceso
     */
    fun inicializar(
        context: Context,
        cloudName: String,
        apiKey: String,
        apiSecret: String
    ) {
        /**
         * Si ya se había activado antes no hace falta volver a hacerlo
         */
        if (inicializado) return

        /**
         * Se preparan los datos de acceso para el servicio de fotos
         */
        val config = mapOf(
            "cloud_name" to cloudName,
            "api_key"    to apiKey,
            "api_secret" to apiSecret
        )

        /**
         * Se inicia formalmente el gestor de medios con la configuración preparada
         */
        MediaManager.init(context, config)
        inicializado = true
    }
}
