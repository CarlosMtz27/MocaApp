package com.cadev.mocaapp.feature.notas.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cadev.mocaapp.feature.notas.domain.model.NotaActual
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nota_widget_prefs")

object NotaWidgetDataStore {
    private val TEXTO = stringPreferencesKey("texto")
    private val AUTOR = stringPreferencesKey("autor")
    private val FECHA = longPreferencesKey("fecha")

    suspend fun guardar(context: Context, nota: NotaActual?) {
        context.dataStore.edit { prefs ->
            if (nota != null) {
                prefs[TEXTO] = nota.texto
                prefs[AUTOR] = nota.nombreAutor
                prefs[FECHA] = nota.actualizadaEn.toDate().time
            } else {
                prefs.clear()
            }
        }
    }

    fun obtener(context: Context): Flow<NotaActual?> {
        return context.dataStore.data.map { prefs ->
            val texto = prefs[TEXTO] ?: return@map null
            val autor = prefs[AUTOR] ?: "Desconocido"
            val fecha = prefs[FECHA] ?: 0L
            NotaActual(
                texto = texto,
                nombreAutor = autor,
                actualizadaEn = com.google.firebase.Timestamp(java.util.Date(fecha))
            )
        }
    }
}
