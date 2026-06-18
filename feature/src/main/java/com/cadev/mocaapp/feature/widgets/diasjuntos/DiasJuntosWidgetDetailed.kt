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

class DiasJuntosWidgetDetailed : GlanceAppWidget() {

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
                .background(ColorProvider(Color(0xFF880E4F))) // Dark magenta background
                .cornerRadius(28.dp)
                .clickable(actionStartActivity(intent)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier.padding(16.dp)
            ) {
                if (data.configurado) {
                    Text(
                        text = "Nuestra Historia",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorProvider(Color.White.copy(alpha = 0.7f))
                        )
                    )
                    Spacer(GlanceModifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${data.diasJuntos}",
                            style = TextStyle(
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(Color.White)
                            )
                        )
                        Spacer(GlanceModifier.width(8.dp))
                        Text(
                            text = "días",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(Color.White)
                            )
                        )
                    }
                    Spacer(GlanceModifier.height(8.dp))
                    Text(
                        text = "Desde: ${data.fechaInicioTexto}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = ColorProvider(Color.White.copy(alpha = 0.9f))
                        )
                    )
                    Spacer(GlanceModifier.height(12.dp))
                    Text(
                        text = "❤ Cada día cuenta ❤",
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = ColorProvider(Color(0xFFF8BBD0))
                        )
                    )
                } else {
                    Text(
                        text = "Configura tu relación",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color.White)
                        )
                    )
                }
            }
        }
    }
}
