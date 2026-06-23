package com.cadev.mocaapp.feature.widgets.ui

import android.app.Application
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WidgetsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<WidgetsUiState>(WidgetsUiState.Loading)
    val uiState: StateFlow<WidgetsUiState> = _uiState.asStateFlow()

    private val appWidgetManager = AppWidgetManager.getInstance(application)

    init {
        loadWidgets()
    }

    fun loadWidgets() {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                
                // Usamos installedProviders y filtramos por nuestro paquete para compatibilidad con API 23+
                val providers = appWidgetManager.installedProviders.filter { 
                    it.provider.packageName == context.packageName 
                }
                
                val widgets = providers.map { info ->
                    WidgetInfo(
                        providerInfo = info,
                        label = info.loadLabel(context.packageManager).toString(),
                        description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            info.loadDescription(context)?.toString() ?: ""
                        } else {
                            // En versiones anteriores a S, no hay un método directo loadDescription
                            ""
                        },
                        previewImage = info.loadPreviewImage(context, 0),
                        minWidth = info.minWidth,
                        minHeight = info.minHeight,
                        minResizeWidth = info.minResizeWidth,
                        minResizeHeight = info.minResizeHeight,
                        isResizable = info.resizeMode != 0 // RESIZE_NONE es 0
                    )
                }
                
                _uiState.value = WidgetsUiState.Success(widgets)
            } catch (e: Exception) {
                _uiState.value = WidgetsUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun pinWidget(info: WidgetInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                appWidgetManager.requestPinAppWidget(info.providerInfo.provider, null, null)
            }
        }
    }
}

sealed class WidgetsUiState {
    object Loading : WidgetsUiState()
    data class Success(val widgets: List<WidgetInfo>) : WidgetsUiState()
    data class Error(val message: String) : WidgetsUiState()
}
