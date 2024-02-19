package com.sixbynine.transit.path.location

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlin.time.Duration

actual fun LocationProvider(): LocationProvider = object : LocationProvider {
    override val isLocationSupportedByDevice: Boolean = false

    private val _locationPermissionResults = Channel<LocationPermissionRequestResult>()
    override val locationPermissionResults: SharedFlow<LocationPermissionRequestResult> =
        _locationPermissionResults.receiveAsFlow().shareIn(GlobalScope, WhileSubscribed())

    override fun hasLocationPermission(): Boolean = false

    override fun requestLocationPermission() {
        GlobalScope.launch {
            _locationPermissionResults.send(LocationPermissionRequestResult.Denied)
        }
    }

    override suspend fun tryToGetLocation(timeout: Duration) = LocationCheckResult.NoProvider
}
