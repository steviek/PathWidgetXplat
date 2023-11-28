package com.sixbynine.transit.path.widget

import com.sixbynine.transit.path.native.widgetReloader
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

object WidgetDataUpdater {
    suspend fun updateWidgetData() {
        Napier.d("Update widget data, refreshing")
        widgetDataStore()["is_refreshing"] = true
        widgetReloader.reloadWidgets()

        delay(5.seconds)
        Napier.d("Update widget data, not refreshing")
        widgetDataStore()["is_refreshing"] = false
        widgetReloader.reloadWidgets()
    }
}

interface WidgetReloader {
    fun reloadWidgets()
}
