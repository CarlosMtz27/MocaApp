package com.cadev.mocaapp.feature.notas.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
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
import com.cadev.mocaapp.feature.notas.domain.model.NotaActual

class NotaWidgetTransparent : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val nota = NotaWidgetDataStore.obtener(context).collectAsState(initial = null).value
            NotaWidgetContent(nota)
        }
    }

    @Composable
    private fun NotaWidgetContent(nota: NotaActual?) {
        val context = LocalContext.current
        val intent = Intent().apply {
            setClassName(context.packageName, "com.cadev.mocaapp.MainActivity")
            putExtra("deepLink", "main/notas")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        Box(modifier = GlanceModifier.fillMaxSize().clickable(actionStartActivity(intent)).padding(8.dp)) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Text("📌", style = TextStyle(fontSize = 14.sp))
                Spacer(GlanceModifier.height(4.dp))
                if (nota != null) {
                    Text(nota.texto, style = TextStyle(fontSize = 15.sp, color = ColorProvider(Color.White)), modifier = GlanceModifier.defaultWeight(), maxLines = 5)
                    Text("De: ${nota.nombreAutor}", style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White.copy(alpha = 0.6f))))
                } else {
                    Text("Toca para escribir...", style = TextStyle(fontSize = 13.sp, color = ColorProvider(Color.White.copy(alpha = 0.5f))))
                }
            }
        }
    }
}
