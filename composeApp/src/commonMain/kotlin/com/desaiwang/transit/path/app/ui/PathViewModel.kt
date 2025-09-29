package com.desaiwang.transit.path.app.ui

import com.desaiwang.transit.path.util.launchAndReturnUnit
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource.Monotonic

abstract class PathViewModel<State : Any, Intent : Any, Effect : Any> : ViewModel() {

    private var lastIntent: Intent? = null
    private var lastIntentTime: ComparableTimeMark? = null

    protected val lightweightScope = CoroutineScope(Dispatchers.Default)

    protected open val rateLimitedIntents: Set<Intent> = emptySet()

    override fun onCleared() {
        super.onCleared()
        lightweightScope.cancel()
    }

    abstract val state: StateFlow<State>
    abstract val effects: Flow<Effect>

    protected abstract suspend fun performIntent(intent: Intent)

    fun onIntent(intent: Intent) = lightweightScope.launchAndReturnUnit {
        val lastIntent = lastIntent
        val lastIntentTime = lastIntentTime
        val now = Monotonic.markNow()
        if (lastIntent == intent &&
            lastIntentTime != null &&
            lastIntentTime > (now - 500.milliseconds) &&
            intent in rateLimitedIntents
        ) {
            return@launchAndReturnUnit
        }
        this.lastIntent = intent
        this.lastIntentTime = now
        performIntent(intent)
    }
}
