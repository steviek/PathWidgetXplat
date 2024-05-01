package com.sixbynine.transit.path.network

interface NetworkManager {
    fun isConnectedToInternet(): Boolean
}

expect fun NetworkManager(): NetworkManager
