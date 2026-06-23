package com.cadev.mocaapp.feature.widgets.eventos

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.cadev.mocaapp.feature.R

class EventosWidgetTransparent : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = EventosWidgetDataStore.obtener(context).collectAsState(initial = EventosWidgetState()).value
            Content(state)
        }
    }

    @Composable
    private fun Content(state: EventosWidgetState) {
        val context = LocalContext.current
        val lista = state.lista
        val indice = state.indiceActual
        
        val intent = Intent().apply {
            setClassName(context.packageName, "com.cadev.mocaapp.MainActivity")
            putExtra("deepLink", "main/calendario")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (lista.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = GlanceModifier.clickable(actionStartActivity(intent))
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_reaccion_calendario),
                        contentDescription = null,
                        modifier = GlanceModifier.size(24.dp)
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        text = "Sin planes",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White))
                    )
                }
            } else {
                val evento = lista[indice]
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (lista.size > 1) {
                            Box(modifier = GlanceModifier.padding(4.dp).clickable(
                                actionRunCallback<CambiarPaginaAction>(
                                    actionParametersOf(CambiarPaginaAction.DIRECCION_KEY to -1)
                                )
                            )) {
                                Text("<", style = TextStyle(fontWeight = FontWeight.Bold, color = ColorProvider(Color.White)))
                            }
                        }
                        
                        Spacer(GlanceModifier.width(8.dp))
                        Image(
                            provider = ImageProvider(R.drawable.ic_reaccion_calendario),
                            contentDescription = null,
                            modifier = GlanceModifier.size(16.dp)
                        )
                        Spacer(GlanceModifier.width(4.dp))
                        Text(
                            text = "(${indice + 1}/${lista.size})",
                            style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color.White.copy(alpha = 0.7f)))
                        )
                        Spacer(GlanceModifier.width(8.dp))

                        if (lista.size > 1) {
                            Box(modifier = GlanceModifier.padding(4.dp).clickable(
                                actionRunCallback<CambiarPaginaAction>(
                                    actionParametersOf(CambiarPaginaAction.DIRECCION_KEY to 1)
                                )
                            )) {
                                Text(">", style = TextStyle(fontWeight = FontWeight.Bold, color = ColorProvider(Color.White)))
                            }
                        }
                    }

                    Spacer(GlanceModifier.height(4.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = GlanceModifier.clickable(actionStartActivity(intent))
                    ) {
                        Text(
                            text = evento.titulo,
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White)),
                            maxLines = 1
                        )
                        Text(
                            text = "${evento.fecha} · ${evento.hora}",
                            style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color.White.copy(alpha = 0.8f)))
                        )
                    }
                }
            }
        }
    }
}
