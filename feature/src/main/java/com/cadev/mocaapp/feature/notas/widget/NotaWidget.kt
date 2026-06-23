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
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.cadev.mocaapp.feature.R
import com.cadev.mocaapp.feature.notas.domain.model.NotaActual

class NotaWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val data = NotaWidgetDataStore.obtener(context).collectAsState(initial = NotaWidgetData(null, "#4A4A4A")).value
            NotaWidgetContent(data)
        }
    }

    @Composable
    private fun NotaWidgetContent(data: NotaWidgetData) {
        val context = LocalContext.current
        val nota = data.nota
        val colorTexto = try { Color(android.graphics.Color.parseColor(data.colorTexto)) } catch (e: Exception) { Color(0xFF4A4A4A) }

        val intent = Intent().apply {
            setClassName(context.packageName, "com.cadev.mocaapp.MainActivity")
            putExtra("deepLink", "main/notas")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFFFFF0F5)))
                .cornerRadius(24.dp)
                .clickable(actionStartActivity(intent))
                .padding(16.dp)
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_reaccion_corazon),
                        contentDescription = null,
                        modifier = GlanceModifier.size(18.dp)
                    )
                    Spacer(GlanceModifier.width(6.dp))
                    Text(
                        text = "Para ti",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color(0xFFFF69B4))
                        )
                    )
                }
                
                Spacer(GlanceModifier.height(10.dp))

                if (nota == null) {
                    Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Toca para escribir algo lindo...",
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = ColorProvider(Color(0xFFDB7093).copy(alpha = 0.6f))
                            )
                        )
                    }
                } else {
                    Text(
                        text = nota.texto,
                        modifier = GlanceModifier.defaultWeight(),
                        style = TextStyle(
                            fontSize = 15.sp,
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
                            color = ColorProvider(Color(0xFFFF69B4).copy(alpha = 0.7f))
                        )
                    )
                }
            }
        }
    }
}
