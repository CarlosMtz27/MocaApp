package com.cadev.mocaapp.feature.widgets.distancia

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "distancia_widget_prefs")

class DistanciaWidgetDataStore(private val context: Context) {

    companion object {
        val FOTO1_PATH = stringPreferencesKey("foto1_path")
        val NOMBRE1 = stringPreferencesKey("nombre1")
        val FOTO2_PATH = stringPreferencesKey("foto2_path")
        val NOMBRE2 = stringPreferencesKey("nombre2")
        val DISTANCIA_TEXTO = stringPreferencesKey("distancia_texto")
        val ACTUALIZADA_EN = stringPreferencesKey("actualizada_en")
    }

    suspend fun saveData(
        foto1Path: String,
        nombre1: String,
        foto2Path: String,
        nombre2: String,
        distanciaTexto: String,
        actualizadaEn: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[FOTO1_PATH] = foto1Path
            prefs[NOMBRE1] = nombre1
            prefs[FOTO2_PATH] = foto2Path
            prefs[NOMBRE2] = nombre2
            prefs[DISTANCIA_TEXTO] = distanciaTexto
            prefs[ACTUALIZADA_EN] = actualizadaEn
        }
    }

    val widgetData: Flow<WidgetData> = context.dataStore.data.map { prefs ->
        WidgetData(
            foto1Path = prefs[FOTO1_PATH] ?: "",
            nombre1 = prefs[NOMBRE1] ?: "Usuario",
            foto2Path = prefs[FOTO2_PATH] ?: "",
            nombre2 = prefs[NOMBRE2] ?: "Pareja",
            distanciaTexto = prefs[DISTANCIA_TEXTO] ?: "Ubicación no disponible",
            actualizadaEn = prefs[ACTUALIZADA_EN] ?: ""
        )
    }
}

data class WidgetData(
    val foto1Path: String,
    val nombre1: String,
    val foto2Path: String,
    val nombre2: String,
    val distanciaTexto: String,
    val actualizadaEn: String
)
