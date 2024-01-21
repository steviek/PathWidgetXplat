package com.sixbynine.transit.path

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow

object Logging {
    private val hasInitialized = MutableStateFlow(false)

    fun initialize() {
        if (hasInitialized.compareAndSet(expect = false, update = true)) {
            Napier.base(DebugAntilog())
        }
    }

    fun d(message: String) {
        initialize()
        Napier.d(message)
    }
}