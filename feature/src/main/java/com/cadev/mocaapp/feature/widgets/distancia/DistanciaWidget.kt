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
        
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(actionStartActivity(intent)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WidgetImage(data.foto1Path)
                
                Text(
                    text = " ◀── ",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Text(
                    text = "❤️",
                    style = TextStyle(fontSize = 22.sp)
                )

                Text(
                    text = " ──▶ ",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                WidgetImage(data.foto2Path)
            }
            
            Spacer(modifier = GlanceModifier.height(4.dp))
            
            Text(
                text = data.distanciaTexto,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            )
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
                    .size(48.dp)
                    .cornerRadius(24.dp)
            )
        } else {
            Box(
                modifier = GlanceModifier
                    .size(48.dp)
                    .cornerRadius(24.dp)
                    .background(ColorProvider(Color.White.copy(alpha = 0.2f))),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", style = TextStyle(color = ColorProvider(Color.White)))
            }
        }
    }
}
