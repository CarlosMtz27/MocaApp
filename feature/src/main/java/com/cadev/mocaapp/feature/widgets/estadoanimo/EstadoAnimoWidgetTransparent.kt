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
import com.cadev.mocaapp.feature.estadoanimo.domain.model.MAPA_MOODS

/**
 * WIDGET DE SENTIMIENTO (MINIMALISTA FLOTANTE)
 * 
 * Qué hace:
 * Muestra el estado de ambos flotando sobre la pantalla, ideal para quienes 
 * quieren un escritorio limpio pero estar conectados.
 */
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

        Box(
            modifier = GlanceModifier.fillMaxSize().clickable(actionStartActivity(intent)), 
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MoodItem(data.emojiPropio, data.nombrePropio)
                
                Image(
                    provider = ImageProvider(R.drawable.ic_reaccion_corazon),
                    contentDescription = null,
                    modifier = GlanceModifier.size(16.dp).padding(horizontal = 14.dp)
                )
                
                MoodItem(data.emojiPareja, data.nombrePareja)
            }
        }
    }

    @Composable
    private fun MoodItem(emoji: String, nombre: String) {
        val moodInfo = MAPA_MOODS[emoji] ?: MAPA_MOODS["unknown"]!!
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                provider = ImageProvider(moodInfo.iconRes),
                contentDescription = null,
                modifier = GlanceModifier.size(34.dp)
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = moodInfo.label,
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color.White)
                )
            )
            Text(
                text = nombre, 
                style = TextStyle(
                    fontSize = 9.sp, 
                    color = ColorProvider(Color.White.copy(alpha = 0.6f))
                )
            )
        }
    }
}
