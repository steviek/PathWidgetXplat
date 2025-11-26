package com.sixbynine.transit.path.network

actual fun NetworkManager() = object : NetworkManager {
    override fun isConnectedToInternet(): Boolean {
        return true
    }
}
