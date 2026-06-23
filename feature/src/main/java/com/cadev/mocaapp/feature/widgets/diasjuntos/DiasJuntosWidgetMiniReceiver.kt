package com.cadev.mocaapp.feature.widgets.diasjuntos

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class DiasJuntosWidgetMiniReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DiasJuntosWidgetMini()
}
