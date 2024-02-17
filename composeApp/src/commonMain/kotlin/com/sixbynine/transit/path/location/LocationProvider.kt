package com.sixbynine.transit.path.location

import kotlin.time.Duration

interface LocationProvider {
    val isLocationSupportedByDevice: Boolean

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

expect fun LocationProvider(): LocationProvider
