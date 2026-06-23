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
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.cadev.mocaapp.feature.R

/**
 * WIDGET DE NUESTRA HISTORIA (MINIMALISTA FLOTANTE)
 * 
 * Qué hace:
 * Versión ultra-minimalista sin fondo, para que el contador parezca 
 * grabado directamente sobre el fondo de pantalla del móvil.
 */
class DiasJuntosWidgetTransparent : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = DiasJuntosWidgetDataStore(context)
        provideContent {
            val data by dataStore.widgetData.collectAsState(initial = DiasJuntosWidgetData(0, "", false))
            GlanceTheme { Content(data) }
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
            modifier = GlanceModifier.fillMaxSize().clickable(actionStartActivity(intent)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (data.configurado) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_corazon),
                        contentDescription = null,
                        modifier = GlanceModifier.size(24.dp)
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        text = "${data.diasJuntos}", 
                        style = TextStyle(
                            fontSize = 32.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = ColorProvider(Color.White)
                        )
                    )
                    Text(
                        text = "días juntos", 
                        style = TextStyle(
                            fontSize = 12.sp, 
                            color = ColorProvider(Color.White.copy(alpha = 0.8f))
                        )
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Configurar", style = TextStyle(fontSize = 14.sp, color = ColorProvider(Color.White)))
                        Spacer(GlanceModifier.width(4.dp))
                        Image(
                            provider = ImageProvider(R.drawable.ic_reaccion_corazon),
                            contentDescription = null,
                            modifier = GlanceModifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
