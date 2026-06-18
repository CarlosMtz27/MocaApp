package com.cadev.mocaapp.feature.widgets.distancia

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class DistanciaWidgetTransparentReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DistanciaWidgetTransparent()
}
