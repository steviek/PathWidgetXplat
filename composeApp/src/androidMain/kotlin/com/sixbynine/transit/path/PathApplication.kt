package com.sixbynine.transit.path

import android.app.Application
import com.sixbynine.transit.path.analytics.Analytics
import com.sixbynine.transit.path.app.ui.ActivityRegistry
import com.sixbynine.transit.path.native.NativeHolder
import com.sixbynine.transit.path.widget.WidgetRefreshWorker
import com.sixbynine.transit.path.widget.WidgetReloader
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class PathApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this

        Analytics.appLaunched()

        NativeHolder.initialize(
            object : WidgetReloader {
                override fun reloadWidgets() {

                }
            }
        )

        ActivityRegistry.register(this)

        GlobalScope.launch {
            delay(2.seconds)
            WidgetRefreshWorker.schedule()
        }
    }

    companion object {
        lateinit var instance: PathApplication
    }
}
