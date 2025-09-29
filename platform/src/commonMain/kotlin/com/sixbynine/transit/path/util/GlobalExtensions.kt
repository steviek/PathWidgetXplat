package com.desaiwang.transit.path.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime

inline fun <T: Any> T?.orElse(other: () -> T): T {
    return this ?: other()
}

inline fun <T> T.runIf(condition: Boolean, block: T.() -> T): T {
    return if (condition) {
        block()
    } else {
        this
    }
}

inline fun <T> T.runUnless(condition: Boolean, block: T.() -> T) = runIf(!condition) { block() }

inline fun <A : Any, B : Any, C> ifNotNull(first: A?, second: B?, transform: (A, B) -> C): C? {
    first ?: return null
    second ?: return null
    return transform(first, second)
}

fun <T> Result<Result<T>>.flatten(): Result<T> {
    return fold(
        onSuccess = { it },
        onFailure = { Result.failure(it) }
    )
}

fun LocalDateTime.dropSubSeconds(): LocalDateTime {
    return date.atTime(time.dropSubSeconds())
}

fun LocalTime.dropSubSeconds(): LocalTime {
    return LocalTime(hour, minute, second)
}
