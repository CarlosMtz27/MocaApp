package com.cadev.mocaapp.feature.widgets.ui

import android.appwidget.AppWidgetProviderInfo
import android.graphics.drawable.Drawable

data class WidgetInfo(
    val providerInfo: AppWidgetProviderInfo,
    val label: String,
    val description: String,
    val previewImage: Drawable?,
    val minWidth: Int,
    val minHeight: Int,
    val minResizeWidth: Int,
    val minResizeHeight: Int,
    val isResizable: Boolean
)
