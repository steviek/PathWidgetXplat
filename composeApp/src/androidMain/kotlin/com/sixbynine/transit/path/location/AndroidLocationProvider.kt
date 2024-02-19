package com.sixbynine.transit.path.location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Build.VERSION
import android.os.CancellationSignal
import androidx.annotation.RequiresPermission
import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.PathApplication
import com.sixbynine.transit.path.app.ui.ActivityRegistry
import com.sixbynine.transit.path.location.LocationCheckResult.Failure
import com.sixbynine.transit.path.location.LocationCheckResult.NoPermission
import com.sixbynine.transit.path.location.LocationCheckResult.NoProvider
import com.sixbynine.transit.path.location.LocationCheckResult.Success
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

object AndroidLocationProvider : LocationProvider {

    private val context = PathApplication.instance

    private val locationPermissions = listOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

    override val isLocationSupportedByDevice: Boolean
        get() = locationManager != null && VERSION.SDK_INT >= 23

    private val _locationPermissionResultsChannel = Channel<LocationPermissionRequestResult>()
    override val locationPermissionResults: SharedFlow<LocationPermissionRequestResult> =
        _locationPermissionResultsChannel.receiveAsFlow().shareIn(GlobalScope, WhileSubscribed())

    override fun hasLocationPermission(): Boolean {
        if (VERSION.SDK_INT < 23 || !isLocationSupportedByDevice) return false
        return locationPermissions.any {
            context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        GlobalScope.launch {
            _locationPermissionResultsChannel.send(
                if (granted) {
                    LocationPermissionRequestResult.Granted
                } else {
                    LocationPermissionRequestResult.Denied
                }
            )
        }
    }

    override fun requestLocationPermission() {
        val activity = ActivityRegistry.peekCreatedActivity()
        if (VERSION.SDK_INT < 23 || activity == null) {
            GlobalScope.launch {
                _locationPermissionResultsChannel.send(LocationPermissionRequestResult.Denied)
            }
            return
        }

        activity.requestLocationPermissions()
    }

    override suspend fun tryToGetLocation(timeout: Duration): LocationCheckResult {
        if (locationManager == null || VERSION.SDK_INT < 23) {
            return NoProvider
        }

        // This is duplicated code, but it prevents Android Studio lint complaining.
        if (
            locationPermissions.none {
                context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            Logging.d("Don't have permission to get the user's location")
            return NoPermission
        }

        val criteria = Criteria().apply {
            accuracy = Criteria.ACCURACY_FINE
            isCostAllowed = false
        }
        val provider =
            locationManager.getBestProvider(criteria, /* enabledOnly = */ true) ?: return NoProvider
        val lastKnownLocation = locationManager.getLastKnownLocation(provider)
        Logging.d("Last known location is ${lastKnownLocation?.toLatLngStringWithGoogleApiLink()}")

        return try {
            withTimeout(timeout) {
                val currentLocation = locationManager.getCurrentLocation(provider)
                Logging.d(
                    "Retrieved current location as " +
                            "${currentLocation.toLatLngStringWithGoogleApiLink()} from $provider"
                )
                Success(currentLocation.toSharedLocation())
            }
        } catch (e: TimeoutCancellationException) {
            Logging.w("Timed out trying to get the user's location")
            lastKnownLocation?.let { Success(it.toSharedLocation()) } ?: Failure(e)
        } catch (t: Throwable) {
            Logging.w("Unexpected error getting the user's location", t)
            lastKnownLocation?.let { Success(it.toSharedLocation()) } ?: Failure(t)
        }
    }

    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    private suspend fun LocationManager.getCurrentLocation(
        provider: String
    ) = suspendCancellableCoroutine<Location> { continuation ->
        if (VERSION.SDK_INT >= 30) {
            val cancellationSignal = CancellationSignal()
            getCurrentLocation(
                provider,
                cancellationSignal,
                context.mainExecutor
            ) { location ->
                continuation.resumeWith(Result.success(location))
            }
            continuation.invokeOnCancellation { cancellationSignal.cancel() }
        } else {
            @Suppress("DEPRECATION") // I know...
            requestSingleUpdate(
                provider,
                { continuation.resumeWith(Result.success(it)) },
                /* looper = */ null
            )
        }
    }

    private fun Location.toLatLngStringWithGoogleApiLink(): String {
        return "($latitude, $longitude) [https://www.google.com/maps/search/" +
                "?api=1&query=$latitude%2C$longitude]"
    }

    private fun Location.toSharedLocation() = Location(latitude, longitude)
}


var Location.elapsedRealtime: Duration
    get() = elapsedRealtimeNanos.nanoseconds
    set(value) {
        elapsedRealtimeNanos = value.inWholeNanoseconds
    }

actual fun LocationProvider(): LocationProvider = AndroidLocationProvider

