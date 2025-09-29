package com.desaiwang.transit.path.util

fun <T> List<T>.secondOrNull(): T? {
    return getOrNull(1)
}

fun <T> Set<T>.withElementPresent(element: T, present: Boolean): Set<T> {
    return if (present) {
        plus(element)
    } else {
        minus(element)
    }
}
