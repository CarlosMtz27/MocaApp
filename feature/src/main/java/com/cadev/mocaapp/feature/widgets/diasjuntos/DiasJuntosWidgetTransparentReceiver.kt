package com.cadev.mocaapp.feature.widgets.diasjuntos

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class DiasJuntosWidgetTransparentReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DiasJuntosWidgetTransparent()
}
