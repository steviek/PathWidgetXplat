package com.desaiwang.transit.path.network

object IosNetworkManager : NetworkManager {
    override fun isConnectedToInternet(): Boolean {
        return true
    }
}

actual fun NetworkManager(): NetworkManager = IosNetworkManager
