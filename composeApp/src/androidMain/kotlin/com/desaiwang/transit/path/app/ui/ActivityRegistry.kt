package com.desaiwang.transit.path.app.ui

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.desaiwang.transit.path.BaseActivity
import com.desaiwang.transit.path.app.lifecycle.AppLifecycleObserver

object ActivityRegistry {

    private val createdActivities = mutableSetOf<BaseActivity>()
    private val startedActivities = mutableSetOf<Activity>()

    fun peekCreatedActivity() = createdActivities.lastOrNull()

    fun register(application: Application) {
        application.registerActivityLifecycleCallbacks(
            object : ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    if (activity is BaseActivity) {
                        createdActivities += activity
                    }
                }

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

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

                override fun onActivityDestroyed(activity: Activity) {
                    if (activity is BaseActivity) {
                        createdActivities -= activity
                    }
                }
            }
        )
    }
}
