package com.cadev.mocaapp.feature.widgets.eventos

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class EventosWidgetTransparentReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = EventosWidgetTransparent()
}
