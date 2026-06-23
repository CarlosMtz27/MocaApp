@file:Suppress("RestrictedApi")

package com.cadev.mocaapp.feature.widgets.diasjuntos

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.cadev.mocaapp.feature.R

/**
 * WIDGET DE NUESTRA HISTORIA (CONTADOR)
 * 
 * Qué hace:
 * Muestra cuántos días llevamos juntos con un diseño minimalista pero tierno. 
 * Es el recordatorio diario de nuestro camino compartido.
 */
class DiasJuntosWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = DiasJuntosWidgetDataStore(context)
        
        provideContent {
            val data by dataStore.widgetData.collectAsState(initial = DiasJuntosWidgetData(0, "", false))
            
            GlanceTheme {
                Content(data)
            }
        }
    }

    @Composable
    private fun Content(data: DiasJuntosWidgetData) {
        val context = LocalContext.current
        val intent = Intent().apply {
            setClassName(context.packageName, "com.cadev.mocaapp.MainActivity")
            putExtra("deepLink", "main/home")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFFFFF0F5))) // Lavender Blush
                .cornerRadius(24.dp)
                .clickable(actionStartActivity(intent)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier.padding(8.dp)
            ) {
                if (data.configurado) {
                    // Añadimos un pequeño texto superior para que no sea solo el número
                    Text(
                        text = "Llevamos",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = ColorProvider(Color(0xFFDB7093)),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    
                    Spacer(GlanceModifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_reaccion_corazon),
                            contentDescription = null,
                            modifier = GlanceModifier.size(16.dp)
                        )
                        Spacer(GlanceModifier.width(6.dp))
                        Text(
                            text = "${data.diasJuntos}",
                            style = TextStyle(
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(Color(0xFFD81B60))
                            )
                        )
                        Spacer(GlanceModifier.width(6.dp))
                        Image(
                            provider = ImageProvider(R.drawable.ic_reaccion_corazon),
                            contentDescription = null,
                            modifier = GlanceModifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "días juntos",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorProvider(Color(0xFFAD1457))
                        )
                    )
                } else {
                    Text(
                        text = "Toca para configurar",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color(0xFFD81B60))
                        )
                    )
                }
            }
        }
    }
}
