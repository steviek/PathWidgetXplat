package com.sixbynine.transit.path.util

import kotlin.time.Instant

data class TimestampedValue<T>(val timestamp: Instant, val value: T)

fun <T> TimestampedValue<T>.toAgedValue(now: Instant): AgedValue<T> {
    val age = now - timestamp
    return AgedValue(age, value)
}
