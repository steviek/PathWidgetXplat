package com.sixbynine.transit.path.util

actual fun globalDataStore(): GlobalDataStore {
    throw UnsupportedOperationException("GlobalDataStore not supported on jvm")
}
