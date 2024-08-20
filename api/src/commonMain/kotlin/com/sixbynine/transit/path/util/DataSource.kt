package com.sixbynine.transit.path.util

import kotlinx.datetime.Instant
import kotlin.time.Duration

internal class DataSource<T>(
    private val getCached: () -> TimestampedValue<T>?,
    private val fetch: suspend () -> T,
    private val maxAge: Duration,
) {

    fun get(now: Instant): FetchWithPrevious<T> {
        val lastResult = runCatching { getCached() }.getOrNull()?.toAgedValue(now)

        if (lastResult != null && lastResult.age <= maxAge) {
            return FetchWithPrevious(lastResult)
        }

        val fetch = IoScope.asyncCatchingDataResult { fetch() }
        return FetchWithPrevious(previous = lastResult, fetch = fetch)
    }
}
