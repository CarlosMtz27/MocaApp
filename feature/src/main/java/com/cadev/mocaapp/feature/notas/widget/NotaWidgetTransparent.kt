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

class NotaWidgetTransparent : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val data = NotaWidgetDataStore.obtener(context).collectAsState(initial = NotaWidgetData(null, "#FFFFFF")).value
            NotaWidgetContent(data)
        }
    }

    @Composable
    private fun NotaWidgetContent(data: NotaWidgetData) {
        val context = LocalContext.current
        val nota = data.nota
        // En transparente usamos blanco por defecto si falla el parseo
        val colorTexto = try { Color(android.graphics.Color.parseColor(data.colorTexto)) } catch (e: Exception) { Color.White }

        val intent = Intent().apply {
            setClassName(context.packageName, "com.cadev.mocaapp.MainActivity")
            putExtra("deepLink", "main/notas")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(actionStartActivity(intent))
                .padding(12.dp)
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Image(
                    provider = ImageProvider(R.drawable.ic_reaccion_corazon),
                    contentDescription = null,
                    modifier = GlanceModifier.size(16.dp)
                )
                
                Spacer(GlanceModifier.height(6.dp))
                
                if (nota != null) {
                    Text(
                        text = nota.texto, 
                        modifier = GlanceModifier.defaultWeight(),
                        style = TextStyle(
                            fontSize = 16.sp, 
                            fontWeight = FontWeight.Medium, 
                            color = ColorProvider(colorTexto)
                        ), 
                        maxLines = 5
                    )
                    Text(
                        text = "— ${nota.nombreAutor}", 
                        style = TextStyle(
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = ColorProvider(colorTexto.copy(alpha = 0.7f))
                        )
                    )
                } else {
                    Text(
                        text = "Toca para escribir...", 
                        style = TextStyle(
                            fontSize = 13.sp, 
                            color = ColorProvider(Color.White.copy(alpha = 0.5f))
                        )
                    )
                }
            }
        }
    }
}
