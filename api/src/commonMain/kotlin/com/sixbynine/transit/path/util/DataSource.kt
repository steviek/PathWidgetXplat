package com.desaiwang.transit.path.util

import kotlinx.coroutines.CoroutineStart.LAZY
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant
import kotlin.time.Duration

internal class DataSource<T>(
    private val getCached: () -> TimestampedValue<T>?,
    private val fetch: suspend () -> T,
    private val maxAge: Duration,
) {

    private val fetchMutex = Mutex()
    private var ongoingFetch: Deferred<DataResult<T>>? = null

    fun get(now: Instant): FetchWithPrevious<T> {
        val lastResult = runCatching { getCached() }.getOrNull()?.toAgedValue(now)

        if (lastResult != null && lastResult.age <= maxAge) {
            return FetchWithPrevious(lastResult)
        }

        val fetch = startOrJoinFetch()
        return FetchWithPrevious(previous = lastResult, fetch = fetch)
    }

    private fun startOrJoinFetch(): Deferred<DataResult<T>> {
        return IoScope.async(start = LAZY) {
            val asyncFetch = fetchMutex.withLock {
                ongoingFetch?.takeIf { it.isActive }?.let { return@withLock it }
                IoScope.asyncCatchingDataResult { fetch() }.also { ongoingFetch = it }
            }

            asyncFetch.await().also { ongoingFetch = null }
        }
    }
}
