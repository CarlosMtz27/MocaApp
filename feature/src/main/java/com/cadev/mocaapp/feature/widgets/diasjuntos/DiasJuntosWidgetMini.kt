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
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.cadev.mocaapp.feature.R

class DiasJuntosWidgetMini : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = DiasJuntosWidgetDataStore(context)
        provideContent {
            val data by dataStore.widgetData.collectAsState(initial = DiasJuntosWidgetData(0, "", false))
            Content(data)
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
            Image(
                provider = ImageProvider(R.drawable.ic_reaccion_corazon),
                contentDescription = null,
                modifier = GlanceModifier.fillMaxSize()
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${data.diasJuntos}",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(Color.White)
                    )
                )
                Text(
                    text = "días",
                    style = TextStyle(
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Normal,
                        color = ColorProvider(Color.White.copy(alpha = 0.9f))
                    )
                )
            }
        }
    }
}
