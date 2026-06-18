package com.cadev.mocaapp.feature.widgets.estadoanimo

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class EstadoAnimoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            var data by remember { mutableStateOf(EstadoAnimoWidgetInfo()) }
            val localContext = LocalContext.current

            LaunchedEffect(Unit) {
                data = EstadoAnimoWidgetDataStore.obtener(localContext)
            }

            GlanceTheme {
                Content(data)
            }
        }
    }

    @Composable
    private fun Content(data: EstadoAnimoWidgetInfo) {
        val context = LocalContext.current
        val intent = Intent().apply {
            setClassName(context.packageName, "com.cadev.mocaapp.MainActivity")
            putExtra("deepLink", "main/home")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFFE8EAF6))) // Light indigo
                .cornerRadius(24.dp)
                .clickable(actionStartActivity(intent))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier.fillMaxSize().padding(12.dp)
            ) {
                Text(
                    "¿Cómo están?",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorProvider(Color(0xFF3F51B5))
                    )
                )
                Spacer(GlanceModifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    EmojiColumn(data.emojiPropio, data.nombrePropio)
                    
                    Text(
                        "❤️", 
                        style = TextStyle(fontSize = 18.sp),
                        modifier = GlanceModifier.padding(horizontal = 12.dp)
                    )
                    
                    EmojiColumn(data.emojiPareja, data.nombrePareja)
                }
            }
        }
    }

    @Composable
    private fun EmojiColumn(emoji: String, nombre: String) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = GlanceModifier
                    .size(44.dp)
                    .background(ColorProvider(Color.White))
                    .cornerRadius(22.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji.ifBlank { "❓" }, 
                    style = TextStyle(fontSize = 24.sp)
                )
            }
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = nombre, 
                style = TextStyle(
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color(0xFF283593))
                )
            )
        }
    }
}
