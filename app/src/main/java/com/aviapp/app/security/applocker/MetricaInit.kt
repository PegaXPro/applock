package com.aviapp.app.security.applocker

import android.app.Application
import android.os.Build
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import kotlinx.coroutines.*


object MetricaInit {

    private var sIsLocationTrackingEnabled = true

    fun setLocationTrackingEnabled(value: Boolean) {
        sIsLocationTrackingEnabled = value
    }

    fun isIsLocationTrackingEnabled(): Boolean {
        return sIsLocationTrackingEnabled
    }

    fun init(context: Application) {
        val config = YandexMetricaConfig.newConfigBuilder("d707139c-b434-4a62-9503-1214ad1fd2cb").withLogs().build()
        YandexMetrica.activate(context, config)
        YandexMetrica.enableActivityAutoTracking(context)
    }
}