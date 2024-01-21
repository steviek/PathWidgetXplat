package com.sixbynine.transit.path

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.sixbynine.transit.path.app.lifecycle.AppLifecycleObserver
import com.sixbynine.transit.path.native.NativeHolder
import com.sixbynine.transit.path.widget.WidgetReloader

class PathApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this

        NativeHolder.initialize(
            object : WidgetReloader {
                override fun reloadWidgets() {

                }
            }
        )

        registerActivityLifecycleCallbacks(LifecycleHandler())
    }

    companion object {
        lateinit var instance: PathApplication
    }
}

private class LifecycleHandler : ActivityLifecycleCallbacks {
    private val startedActivities = mutableSetOf<Activity>()
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        startedActivities += activity
        AppLifecycleObserver.setAppIsActive(startedActivities.isNotEmpty())
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {
        startedActivities -= activity
        AppLifecycleObserver.setAppIsActive(startedActivities.isNotEmpty())
    }

    override fun onActivitySaveInstanceState(activity: Activity, savedInstanceState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {}
}