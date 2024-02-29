package com.sixbynine.transit.path.network

import android.content.Context
import android.net.ConnectivityManager
import com.sixbynine.transit.path.PathApplication

object AndroidNetworkManager : NetworkManager {
    private val connectivityManager: ConnectivityManager
        get() {
            return PathApplication.instance.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager
        }

    override fun isConnectedToInternet(): Boolean {
        return connectivityManager.activeNetworkInfo?.isConnectedOrConnecting == true
    }
}

actual fun NetworkManager(): NetworkManager = AndroidNetworkManager
