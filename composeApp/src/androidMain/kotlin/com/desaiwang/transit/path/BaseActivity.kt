package com.desaiwang.transit.path

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import com.desaiwang.transit.path.location.AndroidLocationProvider

abstract class BaseActivity : ComponentActivity() {

    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationPermissionRequest =
            registerForActivityResult(RequestMultiplePermissions()) { permissions ->
                AndroidLocationProvider.onLocationPermissionResult(
                    permissions[ACCESS_FINE_LOCATION] == true ||
                            permissions[ACCESS_COARSE_LOCATION] == true
                )
            }
    }

    fun requestLocationPermissions() {
        locationPermissionRequest.launch(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))
    }
}
