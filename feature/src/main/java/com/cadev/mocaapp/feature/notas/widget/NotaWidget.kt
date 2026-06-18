package com.cadev.mocaapp.feature.notas.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
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
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFFFF9C4)) // Color post-it, usa Color directamente
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (nota == null) {
                Text(
                    text = "No hay nota compartida",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = ColorProvider(android.R.color.darker_gray)
                    )
                )
            } else {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = nota.texto,
                        modifier = GlanceModifier.defaultWeight(),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = ColorProvider(android.R.color.black)
                        ),
                        maxLines = 5
                    )
                    
                    val fechaStr = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault())
                        .format(nota.actualizadaEn.toDate())

                    Text(
                        text = "${nota.nombreAutor} • $fechaStr",
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = ColorProvider(android.R.color.darker_gray)
                        )
                    )
                }
            }
        }
    }
}
