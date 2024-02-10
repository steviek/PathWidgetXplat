package com.sixbynine.transit.path

import android.app.Application
import com.sixbynine.transit.path.analytics.Analytics
import com.sixbynine.transit.path.app.ui.ActivityRegistry
import com.sixbynine.transit.path.native.NativeHolder
import com.sixbynine.transit.path.widget.WidgetReloader

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
    }

    companion object {
        lateinit var instance: PathApplication
    }
}
