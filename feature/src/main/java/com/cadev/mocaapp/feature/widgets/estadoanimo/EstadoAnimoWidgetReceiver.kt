package com.cadev.mocaapp.feature.widgets.estadoanimo

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class EstadoAnimoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = EstadoAnimoWidget()
}
