package com.sixbynine.transit.path.location

import com.sixbynine.transit.path.util.DataResult
import com.sixbynine.transit.path.util.stateFlowOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.time.Duration

object JvmLocationProvider : LocationProvider {
    override val isLocationSupportedByDeviceFlow: StateFlow<DataResult<Boolean>>
        get() = stateFlowOf(DataResult.success(false))

    private val _locationPermissionResults = MutableSharedFlow<LocationPermissionRequestResult>(replay = 1)
    override val locationPermissionResults: SharedFlow<LocationPermissionRequestResult>
        get() = _locationPermissionResults.asSharedFlow()

    override fun hasLocationPermission() = false

    override fun requestLocationPermission() {
        _locationPermissionResults.tryEmit(LocationPermissionRequestResult.Denied)
    }

    override suspend fun tryToGetLocation(): LocationCheckResult {
        return LocationCheckResult.NoProvider
    }

    override val defaultLocationCheckTimeout: Duration
        get() = Duration.INFINITE
}

actual fun LocationProvider() : LocationProvider = JvmLocationProvider