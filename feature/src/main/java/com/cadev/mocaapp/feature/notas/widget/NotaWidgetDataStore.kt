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
    private val COLOR_TEXTO = stringPreferencesKey("color_texto")

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

    suspend fun actualizarColor(context: Context, hex: String) {
        context.dataStore.edit { prefs ->
            prefs[COLOR_TEXTO] = hex
        }
    }

    fun obtener(context: Context): Flow<NotaWidgetData> {
        return context.dataStore.data.map { prefs ->
            val texto = prefs[TEXTO]
            val autor = prefs[AUTOR] ?: "Desconocido"
            val fecha = prefs[FECHA] ?: 0L
            val color = prefs[COLOR_TEXTO] ?: "#4A4A4A"
            
            val nota = if (texto != null) {
                NotaActual(
                    texto = texto,
                    nombreAutor = autor,
                    actualizadaEn = com.google.firebase.Timestamp(java.util.Date(fecha))
                )
            } else null

            NotaWidgetData(nota, color)
        }
    }
}

data class NotaWidgetData(
    val nota: NotaActual?,
    val colorTexto: String
)
