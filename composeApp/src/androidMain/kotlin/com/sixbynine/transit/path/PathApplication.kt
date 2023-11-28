package com.sixbynine.transit.path

import android.app.Application
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
    }

    companion object {
        lateinit var instance: PathApplication
    }
}