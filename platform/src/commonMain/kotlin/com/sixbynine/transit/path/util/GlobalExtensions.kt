package com.sixbynine.transit.path.util

inline fun <T: Any> T?.orElse(other: () -> T): T {
    return this ?: other()
}
