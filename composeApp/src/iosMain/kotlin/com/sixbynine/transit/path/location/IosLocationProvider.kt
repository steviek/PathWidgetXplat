package com.sixbynine.transit.path.location

import kotlin.time.Duration

actual fun LocationProvider(): LocationProvider = object : LocationProvider {
    override val isLocationSupportedByDevice: Boolean = false

    override suspend fun tryToGetLocation(timeout: Duration) = LocationCheckResult.NoProvider
}
