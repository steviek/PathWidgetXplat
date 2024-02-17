package com.sixbynine.transit.path.widget.setup

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.os.Build.VERSION.SDK_INT
import com.sixbynine.transit.path.PathApplication

object PermissionHelper {

    val LOCATION_PERMISSIONS = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)

    fun hasLocationPermission(): Boolean {
        return LOCATION_PERMISSIONS.any { isPermissionGranted(it) }
    }

    private fun isPermissionGranted(permission: String): Boolean {
        if (SDK_INT < 23) return true
        return PathApplication.instance.checkSelfPermission(permission) ==
                PackageManager.PERMISSION_GRANTED
    }
}
