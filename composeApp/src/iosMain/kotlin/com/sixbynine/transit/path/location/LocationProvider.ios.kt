package com.sixbynine.transit.path.location

import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.location.IosLocationProvider.IosLocationAuthorizationStatus.Always
import com.sixbynine.transit.path.location.IosLocationProvider.IosLocationAuthorizationStatus.Denied
import com.sixbynine.transit.path.location.IosLocationProvider.IosLocationAuthorizationStatus.NotDetermined
import com.sixbynine.transit.path.location.IosLocationProvider.IosLocationAuthorizationStatus.Restricted
import com.sixbynine.transit.path.location.IosLocationProvider.IosLocationAuthorizationStatus.WhenInUse
import com.sixbynine.transit.path.location.LocationCheckResult.Failure
import com.sixbynine.transit.path.util.DataResult
import com.sixbynine.transit.path.util.launchAndReturnUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import platform.CoreLocation.CLLocationManager
import kotlin.time.Duration

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

    override suspend fun tryToGetLocation(timeout: Duration): LocationCheckResult {
        locationRequest?.takeIf { it.isActive }?.let { return it.await() }

        val deferredRequest = CompletableDeferred<LocationCheckResult>()
        locationRequest = deferredRequest

        Logging.d("Start a new location request")
        withContext(Dispatchers.Main) {
            requestDelegate?.requestLocation()
        }

        return try {
            withTimeout(timeout) {
                deferredRequest.await()
            }
        } catch (e: TimeoutCancellationException) {
            Failure(e)
        } catch (e : CancellationException) {
            locationRequest?.cancel(e)
            locationRequest = null
            throw e
        } catch (e: Exception) {
            Failure(e)
        }
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
        Logging.d("iOS called back for location with $result")
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
