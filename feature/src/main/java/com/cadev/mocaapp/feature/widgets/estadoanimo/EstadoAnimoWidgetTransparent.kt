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
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class EstadoAnimoWidgetTransparent : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            var data by remember { mutableStateOf(EstadoAnimoWidgetInfo()) }
            val localContext = LocalContext.current
            LaunchedEffect(Unit) { data = EstadoAnimoWidgetDataStore.obtener(localContext) }
            GlanceTheme { Content(data) }
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

        Box(modifier = GlanceModifier.fillMaxSize().clickable(actionStartActivity(intent)), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiItem(data.emojiPropio, data.nombrePropio)
                Text("❤️", style = TextStyle(fontSize = 18.sp), modifier = GlanceModifier.padding(horizontal = 16.dp))
                EmojiItem(data.emojiPareja, data.nombrePareja)
            }
        }
    }

    @Composable
    private fun EmojiItem(emoji: String, nombre: String) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji.ifBlank { "❓" }, style = TextStyle(fontSize = 30.sp))
            Text(nombre, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White)))
        }
    }
}
