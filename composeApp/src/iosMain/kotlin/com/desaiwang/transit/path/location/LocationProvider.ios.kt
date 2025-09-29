package com.desaiwang.transit.path.location

import com.desaiwang.transit.path.Logging
import com.desaiwang.transit.path.location.IosLocationProvider.IosLocationAuthorizationStatus.Always
import com.desaiwang.transit.path.location.IosLocationProvider.IosLocationAuthorizationStatus.Denied
import com.desaiwang.transit.path.location.IosLocationProvider.IosLocationAuthorizationStatus.NotDetermined
import com.desaiwang.transit.path.location.IosLocationProvider.IosLocationAuthorizationStatus.Restricted
import com.desaiwang.transit.path.location.IosLocationProvider.IosLocationAuthorizationStatus.WhenInUse
import com.desaiwang.transit.path.native.NativeHolder
import com.desaiwang.transit.path.time.now
import com.desaiwang.transit.path.util.DataResult
import com.desaiwang.transit.path.util.globalDataStore
import com.desaiwang.transit.path.util.launchAndReturnUnit
import com.desaiwang.transit.path.widget.WidgetDataFetcher
import kotlinx.coroutines.CancellationException
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
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import platform.CoreLocation.CLLocationManager
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource.Monotonic

@Suppress("unused")
object IosLocationProvider : LocationProvider {
    private val lastLocationRequestTimeKey = "location_lastRequestTime"
    private var lastLocationRequestTime: Instant?
        get() = globalDataStore()
            .getLong(lastLocationRequestTimeKey)
            ?.let { Instant.fromEpochMilliseconds(it) }
        set(value) {
            globalDataStore()[lastLocationRequestTimeKey] = value?.toEpochMilliseconds()
        }

    private val _isLocationSupportedByDeviceFlow = MutableStateFlow(DataResult.loading<Boolean>())
    override val isLocationSupportedByDeviceFlow = _isLocationSupportedByDeviceFlow.asStateFlow()

    private val _locationPermissionResultsChannel = Channel<LocationPermissionRequestResult>()
    override val locationPermissionResults: SharedFlow<LocationPermissionRequestResult> =
        _locationPermissionResultsChannel.receiveAsFlow().shareIn(GlobalScope, WhileSubscribed())

    private val locationManager = CLLocationManager()

    var requestDelegate: RequestDelegate? = null

    private var locationRequest: LocationRequest? = null

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

    override val defaultLocationCheckTimeout = 300.milliseconds

    override suspend fun tryToGetLocation(): LocationCheckResult {
        lastLocationRequestTime?.let {
            if (now() - it <= 30.seconds) {
                Logging.d("Skip location request, just checked")
                return LocationCheckResult.JustChecked
            }
        }

        locationRequest?.takeIf { it.deferred.isActive }?.let { return it.deferred.await() }

        val deferredRequest = LocationRequest()
        locationRequest = deferredRequest
        lastLocationRequestTime = now()

        Logging.d("Start a new location request")
        withContext(Dispatchers.Main) {
            requestDelegate?.requestLocation()
        }

        return try {
            deferredRequest.deferred.await()
        } catch (e: CancellationException) {
            locationRequest?.deferred?.cancel(e)
            throw e
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
        val request = locationRequest ?: return

        if (request.deferred.isActive) {
            request.deferred.complete(result)
        } else if (result is LocationCheckResult.Success) {
            WidgetDataFetcher.onLocationReceived(
                result.location,
                request.timeMark.elapsedNow(),
                isDuringFetch = false,
            )
            GlobalScope.launch(Dispatchers.Main) {
                NativeHolder.widgetReloader.value?.reloadWidgets()
            }
        }
    }

    interface RequestDelegate {
        fun hasLocationPermission(): Boolean

        fun requestLocationPermission()

        fun requestLocation()
    }

    enum class IosLocationAuthorizationStatus {
        Always, WhenInUse, Denied, NotDetermined, Restricted
    }

    private data class LocationRequest(
        val deferred: CompletableDeferred<LocationCheckResult> = CompletableDeferred(),
        val timeMark: TimeMark = Monotonic.markNow(),
    )
}

actual fun LocationProvider(): LocationProvider = IosLocationProvider
