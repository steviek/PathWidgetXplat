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
