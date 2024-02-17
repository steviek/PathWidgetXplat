package com.sixbynine.transit.path.util

fun <T> List<T>.secondOrNull(): T? {
    return getOrNull(1)
}
