package com.sixbynine.transit.path

import android.app.Application

abstract class PathApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: PathApplication
    }
}
