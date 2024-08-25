package com.sixbynine.transit.path

import com.sixbynine.transit.path.platform.IsDebug
import com.sixbynine.transit.path.util.IsTest
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
        if (IsTest) {
            println(message)
            return
        }
        if (!IsDebug) return
        initialize()
        Napier.d(message)
    }

    fun w(message: String, throwable: Throwable? = null) {
        if (IsTest) {
            println(message)
            return
        }
        initialize()
        Napier.w(message, throwable)
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (IsTest) {
            println(message)
            return
        }
        initialize()
        Napier.e(message, throwable)
    }
}
