package com.sixbynine.transit.path.util

import kotlinx.coroutines.CancellationException

suspend fun <T> runCatchingSuspend(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e : CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
}
