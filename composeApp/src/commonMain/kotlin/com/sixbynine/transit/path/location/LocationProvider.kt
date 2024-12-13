package com.sixbynine.transit.path.location

import com.sixbynine.transit.path.util.DataResult
import com.sixbynine.transit.path.util.DataResult.Success
import com.sixbynine.transit.path.util.isLoading
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlin.time.Duration

interface LocationProvider {
    val isLocationSupportedByDeviceFlow: StateFlow<DataResult<Boolean>>

    val locationPermissionResults: SharedFlow<LocationPermissionRequestResult>

    fun hasLocationPermission(): Boolean

    fun requestLocationPermission()

    suspend fun tryToGetLocation(timeout: Duration): LocationCheckResult
}

val LocationProvider.isLocationSupportedByDevice: Boolean
    get() = isLocationSupportedByDeviceFlow.value.data ?: true

suspend fun LocationProvider.awaitIsLocationSupportedByDevice(): Boolean {
    val result = isLocationSupportedByDeviceFlow.first { !it.isLoading() }
    return result is Success && result.data
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
