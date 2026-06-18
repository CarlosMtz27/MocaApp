package com.cadev.mocaapp.feature.widgets.estadoanimo

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cadev.mocaapp.feature.estadoanimo.domain.model.EstadoAnimoActual
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

private val Context.dataStore by preferencesDataStore(name = "estado_animo_widget_prefs")

data class EstadoAnimoWidgetInfo(
    val emojiPropio: String = "",
    val nombrePropio: String = "",
    val emojiPareja: String = "",
    val nombrePareja: String = "",
    val fecha: String = ""
)

object EstadoAnimoWidgetDataStore {
    private val EMOJI_PROPIO = stringPreferencesKey("emoji_propio")
    private val NOMBRE_PROPIO = stringPreferencesKey("nombre_propio")
    private val EMOJI_PAREJA = stringPreferencesKey("emoji_pareja")
    private val NOMBRE_PAREJA = stringPreferencesKey("nombre_pareja")
    private val FECHA = stringPreferencesKey("fecha")

    suspend fun guardar(
        context: Context,
        propio: EstadoAnimoActual?,
        nombrePropio: String,
        pareja: EstadoAnimoActual?,
        nombrePareja: String
    ) {
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        context.dataStore.edit { prefs ->
            prefs[EMOJI_PROPIO] = propio?.emoji ?: ""
            prefs[NOMBRE_PROPIO] = nombrePropio
            prefs[EMOJI_PAREJA] = pareja?.emoji ?: ""
            prefs[NOMBRE_PAREJA] = nombrePareja
            prefs[FECHA] = hoy
        }
    }
    
    suspend fun guardar(context: Context, estados: Pair<EstadoAnimoActual?, EstadoAnimoActual?>) {
        // Esta versión se usa desde el servicio de mensajería, 
        // pero necesitaríamos los nombres también si no están guardados.
        // Por simplicidad, solo actualizamos los emojis si ya existen nombres.
        context.dataStore.edit { prefs ->
            prefs[EMOJI_PROPIO] = estados.first?.emoji ?: ""
            prefs[EMOJI_PAREJA] = estados.second?.emoji ?: ""
            prefs[FECHA] = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
    }

    suspend fun obtener(context: Context): EstadoAnimoWidgetInfo {
        val prefs = context.dataStore.data.first()
        return EstadoAnimoWidgetInfo(
            emojiPropio = prefs[EMOJI_PROPIO] ?: "",
            nombrePropio = prefs[NOMBRE_PROPIO] ?: "Yo",
            emojiPareja = prefs[EMOJI_PAREJA] ?: "",
            nombrePareja = prefs[NOMBRE_PAREJA] ?: "Pareja",
            fecha = prefs[FECHA] ?: ""
        )
    }

    suspend fun limpiarPropio(context: Context) {
        context.dataStore.edit { prefs ->
            prefs[EMOJI_PROPIO] = ""
        }
    }
}
