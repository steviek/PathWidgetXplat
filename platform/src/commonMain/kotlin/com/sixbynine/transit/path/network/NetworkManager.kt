package com.desaiwang.transit.path.network

interface NetworkManager {
    fun isConnectedToInternet(): Boolean
}

expect fun NetworkManager(): NetworkManager
