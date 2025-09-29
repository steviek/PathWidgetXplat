package com.desaiwang.transit.path.app.lifecycle

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration

object AppLifecycleObserver {

    private val _isActive = MutableStateFlow(false)
    val isActive = _isActive.asStateFlow()

    fun setAppIsActive(isActive: Boolean) {
        _isActive.value = isActive
    }

    suspend fun awaitActive() = isActive.first { it }

    suspend fun awaitInactive(timeout: Duration = Duration.INFINITE) {
        withTimeoutOrNull(timeout) { isActive.first { !it } }
    }
}
