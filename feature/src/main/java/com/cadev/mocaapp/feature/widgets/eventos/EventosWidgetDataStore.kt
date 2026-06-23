package com.cadev.mocaapp.feature.widgets.eventos

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "widget_eventos_prefs")

data class EventoWidgetInfo(
    val titulo: String = "",
    val fecha: String = "",
    val hora: String = "",
    val id: String = ""
)

data class EventosWidgetState(
    val lista: List<EventoWidgetInfo> = emptyList(),
    val indiceActual: Int = 0
)

object EventosWidgetDataStore {
    private val EVENTOS_JSON = stringPreferencesKey("eventos_json")
    private val INDICE_ACTUAL = stringPreferencesKey("indice_actual")

    suspend fun guardarEventos(context: Context, lista: List<EventoWidgetInfo>) {
        val array = JSONArray()
        lista.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("titulo", it.titulo)
            obj.put("fecha", it.fecha)
            obj.put("hora", it.hora)
            array.put(obj)
        }
        context.dataStore.edit { prefs ->
            prefs[EVENTOS_JSON] = array.toString()
            prefs[INDICE_ACTUAL] = "0"
        }
    }

    suspend fun cambiarIndice(context: Context, nuevoIndice: Int) {
        context.dataStore.edit { prefs ->
            prefs[INDICE_ACTUAL] = nuevoIndice.toString()
        }
    }

    fun obtener(context: Context): Flow<EventosWidgetState> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[EVENTOS_JSON] ?: "[]"
            val indice = prefs[INDICE_ACTUAL]?.toIntOrNull() ?: 0
            
            val lista = mutableListOf<EventoWidgetInfo>()
            try {
                val array = JSONArray(json)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    lista.add(EventoWidgetInfo(
                        id = obj.getString("id"),
                        titulo = obj.getString("titulo"),
                        fecha = obj.getString("fecha"),
                        hora = obj.getString("hora")
                    ))
                }
            } catch (e: Exception) { }

            EventosWidgetState(lista, if (lista.isEmpty()) 0 else indice % lista.size)
        }
    }
}
