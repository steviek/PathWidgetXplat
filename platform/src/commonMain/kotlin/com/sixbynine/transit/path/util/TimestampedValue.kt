package com.desaiwang.transit.path.util

import kotlinx.datetime.Instant

data class TimestampedValue<T>(val timestamp: Instant, val value: T)

fun <T> TimestampedValue<T>.toAgedValue(now: Instant): AgedValue<T> {
    val age = now - timestamp
    return AgedValue(age, value)
}
