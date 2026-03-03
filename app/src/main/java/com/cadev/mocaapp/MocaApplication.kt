package com.cadev.mocaapp
import android.app.Application
import com.cadev.mocaapp.feature.notification.NotificationChannels
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
class MocaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.crearTodos(this)

        // OneSignal init
        OneSignal.Debug.logLevel = LogLevel.NONE
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)
    }
}