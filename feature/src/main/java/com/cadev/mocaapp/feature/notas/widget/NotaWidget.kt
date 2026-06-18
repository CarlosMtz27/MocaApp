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
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.cadev.mocaapp.feature.notas.domain.model.NotaActual
import java.text.SimpleDateFormat
import java.util.Locale

class NotaWidget : GlanceAppWidget() {

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

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFFFFF9C4))) // Classic post-it yellow
                .cornerRadius(12.dp)
                .clickable(actionStartActivity(intent))
                .padding(12.dp)
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📌",
                        style = TextStyle(fontSize = 14.sp)
                    )
                    Spacer(GlanceModifier.width(4.dp))
                    Text(
                        text = "Nota compartida",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color(0xFFFBC02D))
                        )
                    )
                }
                
                Spacer(GlanceModifier.height(8.dp))

                if (nota == null) {
                    Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Toca para escribir algo...",
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = ColorProvider(Color.Gray)
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
                            color = ColorProvider(Color.Black)
                        ),
                        maxLines = 6
                    )
                    
                    val fechaStr = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault())
                        .format(nota.actualizadaEn.toDate())

                    Text(
                        text = "De: ${nota.nombreAutor} • $fechaStr",
                        style = TextStyle(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color.Black.copy(alpha = 0.5f))
                        )
                    )
                }
            }
        }
    }
}
