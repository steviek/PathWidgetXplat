package com.desaiwang.transit.path.util

import kotlin.time.Duration

data class AgedValue<T>(val age: Duration, val value: T)

fun <T, R> AgedValue<T>.map(transform: (T) -> R): AgedValue<R> {
    return AgedValue(age, transform(value))
}

inline fun <A, B, C> AgedValue<A>.combine(
    other: AgedValue<B>,
    transform: (A, B) -> C
): AgedValue<C> {
    return AgedValue(
        age = maxOf(age, other.age),
        value = transform(value, other.value),
    )
}
