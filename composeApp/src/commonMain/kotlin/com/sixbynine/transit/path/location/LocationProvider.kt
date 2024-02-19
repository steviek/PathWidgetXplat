package com.sixbynine.transit.path.location

import kotlinx.coroutines.flow.SharedFlow
import kotlin.time.Duration

interface LocationProvider {
    val isLocationSupportedByDevice: Boolean

    val locationPermissionResults: SharedFlow<LocationPermissionRequestResult>

    fun hasLocationPermission(): Boolean

    fun requestLocationPermission()

    suspend fun tryToGetLocation(timeout: Duration): LocationCheckResult
}

sealed interface LocationCheckResult {
    data object NoPermission : LocationCheckResult
    data object NoProvider : LocationCheckResult
    data class Failure(val throwable: Throwable) : LocationCheckResult
    data class Success(val location: Location) : LocationCheckResult {
        override fun toString() = "(${location.latitude}, ${location.longitude})"
    }
}

sealed interface LocationPermissionRequestResult {
    data object Granted : LocationPermissionRequestResult
    data object Denied : LocationPermissionRequestResult
}

expect fun LocationProvider(): LocationProvider
