package com.cadev.mocaapp.feature.widgets.distancia

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
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
 * WIDGET DE CERCANÍA
 * 
 * Qué hace:
 * Muestra la distancia en tiempo real que nos separa de nuestra pareja. 
 * Es ideal para sentir que, aunque estemos lejos, siempre estamos conectados.
 */
class DistanciaWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = DistanciaWidgetDataStore(context)
        
        provideContent {
            val data by dataStore.widgetData.collectAsState(initial = WidgetData("", "", "", "", "Cargando...", ""))
            
            GlanceTheme {
                Content(data)
            }
        }
    }

    @Composable
    private fun Content(data: WidgetData) {
        val context = LocalContext.current
        val intent = Intent().apply {
            setClassName(context.packageName, "com.cadev.mocaapp.MainActivity")
            putExtra("deepLink", "main/perfil")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ImageProvider(R.drawable.bg_map_widget))
                .cornerRadius(24.dp)
                .clickable(actionStartActivity(intent)),
            contentAlignment = Alignment.Center
        ) {
            // Capa de oscurecimiento sutil para que el texto resalte sobre el mapa
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(Color.Black.copy(alpha = 0.3f)))
                    .cornerRadius(24.dp)
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Distancia",
                        style = TextStyle(
                            color = ColorProvider(Color.White.copy(alpha = 0.8f)),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        WidgetImage(data.foto1Path)
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = GlanceModifier.padding(horizontal = 8.dp)
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_reaccion_corazon),
                                contentDescription = null,
                                modifier = GlanceModifier.size(16.dp)
                            )
                            Spacer(GlanceModifier.height(4.dp))
                            Text(
                                text = data.distanciaTexto,
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        
                        WidgetImage(data.foto2Path)
                    }
                }
            }
        }
    }

    @Composable
    private fun WidgetImage(path: String) {
        val bitmap = if (path.isNotEmpty()) {
            try {
                BitmapFactory.decodeFile(path)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

        if (bitmap != null) {
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = null,
                modifier = GlanceModifier
                    .size(46.dp)
                    .cornerRadius(23.dp)
            )
        } else {
            Box(
                modifier = GlanceModifier
                    .size(46.dp)
                    .cornerRadius(23.dp)
                    .background(ColorProvider(Color.White.copy(alpha = 0.15f))),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_reaccion_hola),
                    contentDescription = null,
                    modifier = GlanceModifier.size(24.dp)
                )
            }
        }
    }
}
