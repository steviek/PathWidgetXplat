package com.sixbynine.transit.path.util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlin.time.Duration

suspend fun <T> runCatchingSuspend(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e : CancellationException) {
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
