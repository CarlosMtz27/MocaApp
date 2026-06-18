package com.cadev.mocaapp.feature.widgets.estadoanimo

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class EstadoAnimoWidgetTransparentReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = EstadoAnimoWidgetTransparent()
}
