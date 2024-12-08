package com.sixbynine.transit.path.location

import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.location.IosLocationProvider.IosLocationAuthorizationStatus.Always
import com.sixbynine.transit.path.location.IosLocationProvider.IosLocationAuthorizationStatus.Denied
import com.sixbynine.transit.path.location.IosLocationProvider.IosLocationAuthorizationStatus.NotDetermined
import com.sixbynine.transit.path.location.IosLocationProvider.IosLocationAuthorizationStatus.Restricted
import com.sixbynine.transit.path.location.IosLocationProvider.IosLocationAuthorizationStatus.WhenInUse
import com.sixbynine.transit.path.util.DataResult
import com.sixbynine.transit.path.util.launchAndReturnUnit
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import platform.CoreLocation.CLLocationManager

@Suppress("unused")
object IosLocationProvider : LocationProvider {
    private val _isLocationSupportedByDeviceFlow = MutableStateFlow(DataResult.loading<Boolean>())
    override val isLocationSupportedByDeviceFlow = _isLocationSupportedByDeviceFlow.asStateFlow()

    private val _locationPermissionResultsChannel = Channel<LocationPermissionRequestResult>()
    override val locationPermissionResults: SharedFlow<LocationPermissionRequestResult> =
        _locationPermissionResultsChannel.receiveAsFlow().shareIn(GlobalScope, WhileSubscribed())

    private val locationManager = CLLocationManager()

    var requestDelegate: RequestDelegate? = null

    private var locationRequest: CompletableDeferred<LocationCheckResult>? = null

    init {
        GlobalScope.launch(Dispatchers.IO) {
            _isLocationSupportedByDeviceFlow.value =
                DataResult.success(CLLocationManager.locationServicesEnabled())
        }
    }

    override fun hasLocationPermission(): Boolean = requestDelegate?.hasLocationPermission() == true

    override fun requestLocationPermission() {
        requestDelegate?.requestLocationPermission()
    }

    override suspend fun tryToGetLocation(): LocationCheckResult {
        locationRequest?.takeIf { it.isActive }?.let { return it.await() }

        val deferredRequest = CompletableDeferred<LocationCheckResult>()
        locationRequest = deferredRequest

        requestDelegate?.requestLocation()

        return deferredRequest.await()
    }

    fun onAuthorizationChanged(
        status: IosLocationAuthorizationStatus,
    ) = GlobalScope.launchAndReturnUnit {
        Logging.d("location authorization is now $status")
        val permissionResult = when (status) {
            Always, WhenInUse -> LocationPermissionRequestResult.Granted
            Denied, Restricted -> LocationPermissionRequestResult.Denied
            NotDetermined -> return@launchAndReturnUnit
        }
        _locationPermissionResultsChannel.send(permissionResult)
    }

    fun onLocationCheckCompleted(result: LocationCheckResult) {
        locationRequest?.complete(result)
    }

    interface RequestDelegate {
        fun hasLocationPermission(): Boolean

        fun requestLocationPermission()

        fun requestLocation()
    }

    enum class IosLocationAuthorizationStatus {
        Always, WhenInUse, Denied, NotDetermined, Restricted
    }
}

actual fun LocationProvider(): LocationProvider = IosLocationProvider
