package com.cadev.mocaapp.feature.widgets.eventos

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.first

class CambiarPaginaAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val direccion = parameters[DIRECCION_KEY] ?: 1
        val state = EventosWidgetDataStore.obtener(context).first()
        
        if (state.lista.isNotEmpty()) {
            var nuevoIndice = state.indiceActual + direccion
            if (nuevoIndice < 0) nuevoIndice = state.lista.size - 1
            if (nuevoIndice >= state.lista.size) nuevoIndice = 0
            
            EventosWidgetDataStore.cambiarIndice(context, nuevoIndice)
            EventosWidget().updateAll(context)
            EventosWidgetTransparent().updateAll(context)
        }
    }

    companion object {
        val DIRECCION_KEY = ActionParameters.Key<Int>("direccion")
    }
}
