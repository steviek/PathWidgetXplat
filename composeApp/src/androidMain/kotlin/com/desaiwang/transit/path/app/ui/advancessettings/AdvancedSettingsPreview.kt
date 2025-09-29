package com.desaiwang.transit.path.app.ui.advancessettings

import androidx.compose.runtime.Composable
import com.desaiwang.transit.path.PathWidgetPreview
import com.desaiwang.transit.path.PreviewTheme
import com.desaiwang.transit.path.app.settings.AvoidMissingTrains.OffPeak
import com.desaiwang.transit.path.app.settings.CommutingConfiguration
import com.desaiwang.transit.path.app.settings.StationLimit.Six
import com.desaiwang.transit.path.app.settings.TimeDisplay.Clock
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.State
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsScreen

@PathWidgetPreview
@Composable
fun AdvancedSettingsPreview() {
    PreviewTheme {
        AdvancedSettingsScreen(
            state = State(
                avoidMissingTrains = OffPeak,
                stationLimit = Six,
                timeDisplay = Clock,
                groupTrains = true,
                commutingConfiguration = CommutingConfiguration.default()
            ),
            onIntent = {

            }
        )
    }
}