package com.cadev.mocaapp.feature.widgets.diasjuntos

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dias_juntos_widget_prefs")

class DiasJuntosWidgetDataStore(private val context: Context) {

    companion object {
        val DIAS_JUNTOS = intPreferencesKey("dias_juntos")
        val FECHA_INICIO_TEXTO = stringPreferencesKey("fecha_inicio_texto")
        val CONFIGURADO = booleanPreferencesKey("configurado")
    }

    suspend fun saveData(
        diasJuntos: Int,
        fechaInicioTexto: String,
        configurado: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[DIAS_JUNTOS] = diasJuntos
            prefs[FECHA_INICIO_TEXTO] = fechaInicioTexto
            prefs[CONFIGURADO] = configurado
        }
    }

    val widgetData: Flow<DiasJuntosWidgetData> = context.dataStore.data.map { prefs ->
        DiasJuntosWidgetData(
            diasJuntos = prefs[DIAS_JUNTOS] ?: 0,
            fechaInicioTexto = prefs[FECHA_INICIO_TEXTO] ?: "",
            configurado = prefs[CONFIGURADO] ?: false
        )
    }
}

data class DiasJuntosWidgetData(
    val diasJuntos: Int,
    val fechaInicioTexto: String,
    val configurado: Boolean
)
