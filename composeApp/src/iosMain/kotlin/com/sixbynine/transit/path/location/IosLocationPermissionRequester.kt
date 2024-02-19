package com.sixbynine.transit.path.location

import androidx.compose.runtime.Composable
import com.sixbynine.transit.path.app.ui.HandleEffects
import kotlinx.coroutines.flow.Flow

@Composable
actual fun LocationPermissionsRequester(flow: Flow<Unit>, onComplete: (Boolean) -> Unit) {
    HandleEffects(flow) {
        onComplete(false)
    }
}
