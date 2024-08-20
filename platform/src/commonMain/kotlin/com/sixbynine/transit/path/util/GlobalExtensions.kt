package com.sixbynine.transit.path.util

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
