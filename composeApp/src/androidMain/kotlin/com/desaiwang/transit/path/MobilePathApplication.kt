package com.desaiwang.transit.path

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.desaiwang.transit.path.analytics.Analytics
import com.desaiwang.transit.path.app.ui.ActivityRegistry
import com.desaiwang.transit.path.native.NativeHolder
import com.desaiwang.transit.path.widget.WidgetRefreshWorker
import com.desaiwang.transit.path.widget.WidgetReloader
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class MobilePathApplication : PathApplication() {
    override fun onCreate() {
        super.onCreate()
        instance = this

        Analytics.appLaunched()

        NativeHolder.initialize(
            object : WidgetReloader {
                override fun reloadWidgets() {
                    GlobalScope.launch {
                        WidgetRefreshWorker.scheduleOneTime()
                    }
                }
            },
            Firebase.crashlytics::recordException
        )

        ActivityRegistry.register(this)

        GlobalScope.launch {
            delay(1.seconds)
            WidgetRefreshWorker.schedule()
        }
    }

    companion object {
        lateinit var instance: MobilePathApplication
    }
}
