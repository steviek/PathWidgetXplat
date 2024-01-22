package com.sixbynine.transit.path

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow

object Logging {
    private val hasInitialized = MutableStateFlow(false)
    private var isTest = false

    fun initialize() {
        if (hasInitialized.compareAndSet(expect = false, update = true)) {
            Napier.base(DebugAntilog())
        }
    }

    fun setTest() {
        isTest = true
    }

    fun d(message: String) {
        if (isTest) {
            println(message)
            return
        }
        initialize()
        Napier.d(message)
    }
}