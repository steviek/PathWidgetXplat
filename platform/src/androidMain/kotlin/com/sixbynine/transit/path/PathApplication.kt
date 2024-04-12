package com.sixbynine.transit.path

import android.app.Application
import com.sixbynine.transit.path.flipper.FlipperUtil

abstract class PathApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        FlipperUtil.initialize(this)
    }

    companion object {
        lateinit var instance: PathApplication
    }
}
