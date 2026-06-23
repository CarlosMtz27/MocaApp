package com.cadev.mocaapp.feature.widgets.diasjuntos

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "widget_dias_juntos")

/**
 * ALMACÉN DE DATOS DE NUESTRA HISTORIA
 * 
 * Qué hace:
 * Guarda de forma segura en la memoria del teléfono los datos que necesita 
 * el widget: el número de días, la fecha de inicio y si ya está configurado. 
 * Esto permite que el widget funcione rápido sin internet.
 */
data class DiasJuntosWidgetData(
    val diasJuntos: Int,
    val fechaInicioTexto: String,
    val configurado: Boolean
)

class DiasJuntosWidgetDataStore(private val context: Context) {
    companion object {
        private val DIAS_JUNTOS = intPreferencesKey("dias_juntos")
        private val FECHA_INICIO = stringPreferencesKey("fecha_inicio")
        private val CONFIGURADO = booleanPreferencesKey("configurado")
    }

    val widgetData: Flow<DiasJuntosWidgetData> = context.dataStore.data.map { prefs ->
        DiasJuntosWidgetData(
            diasJuntos = prefs[DIAS_JUNTOS] ?: 0,
            fechaInicioTexto = prefs[FECHA_INICIO] ?: "",
            configurado = prefs[CONFIGURADO] ?: false
        )
    }

    suspend fun saveData(dias: Long, fecha: String, configurado: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DIAS_JUNTOS] = dias.toInt()
            prefs[FECHA_INICIO] = fecha
            prefs[CONFIGURADO] = configurado
        }
    }
}
