package com.sixbynine.transit.path.util

import kotlin.time.Duration

data class AgedValue<T>(val age: Duration, val value: T)

fun <T, R> AgedValue<T>.map(transform: (T) -> R): AgedValue<R> {
    return AgedValue(age, transform(value))
}
