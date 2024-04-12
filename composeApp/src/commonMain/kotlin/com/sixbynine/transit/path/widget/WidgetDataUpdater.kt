package com.sixbynine.transit.path.widget

import com.sixbynine.transit.path.native.widgetReloader
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

object WidgetDataUpdater {
    suspend fun updateWidgetData() {
        globalDataStore()["is_refreshing"] = true
        widgetReloader.reloadWidgets()

        delay(5.seconds)
        globalDataStore()["is_refreshing"] = false
        widgetReloader.reloadWidgets()
    }
}

interface WidgetReloader {
    fun reloadWidgets()
}
