package com.sixbynine.transit.path.util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration

fun CoroutineScope.launchAndReturnUnit(block: suspend () -> Unit): Unit {
    launch { block() }
}

suspend fun <T> suspendRunCatching(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: TimeoutCancellationException) {
        Result.failure(e)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

suspend fun repeatEvery(duration: Duration, block: suspend () -> Unit) {
    while (true) {
        block()
        delay(duration)
    }
}

inline fun <T> Flow<T>.collect(scope: CoroutineScope, collector: FlowCollector<T>) {
    scope.launch { collect(collector) }
}

inline fun <T> Flow<T>.collectLatest(
    scope: CoroutineScope,
    crossinline action: suspend (T) -> Unit
) {
    scope.launch { collectLatest { action(it) } }
}
