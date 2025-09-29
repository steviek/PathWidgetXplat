package com.desaiwang.transit.path.util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration

fun CoroutineScope.launchAndReturnUnit(block: suspend () -> Unit) {
    launch { block() }
}

inline fun <T> CoroutineScope.asyncCatching(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    crossinline block: suspend () -> T
): Deferred<Result<T>> = async(context, start) {
    suspendRunCatching { block() }
}

inline fun <T> CoroutineScope.asyncCatchingDataResult(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    crossinline block: suspend () -> T
): Deferred<DataResult<T>> = async(context, start) {
    suspendRunCatching { block() }.toDataResult()
}

inline fun <T> suspendRunCatching(block: () -> T): Result<T> {
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

suspend inline fun <T> withTimeoutCatching(
    timeout: Duration,
    crossinline block: suspend () -> T,
): Result<T> {
    return suspendRunCatching {
        withTimeout(timeout) {
            block()
        }
    }
}
