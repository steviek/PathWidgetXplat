package com.desaiwang.transit.path.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build.VERSION
import com.desaiwang.transit.path.PathApplication
import com.desaiwang.transit.path.util.IsTest

object AndroidNetworkManager : NetworkManager {
    private val connectivityManager: ConnectivityManager
        get() {
            return PathApplication.instance.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager
        }

    override fun isConnectedToInternet(): Boolean {
        if (IsTest) return true
        if (VERSION.SDK_INT < 23) {
            return connectivityManager.activeNetworkInfo?.isConnectedOrConnecting == true
        }

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}

actual fun NetworkManager(): NetworkManager = AndroidNetworkManager
