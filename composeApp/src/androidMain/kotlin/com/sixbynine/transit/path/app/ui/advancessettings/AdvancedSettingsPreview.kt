package com.sixbynine.transit.path.app.ui.advancessettings

import androidx.compose.runtime.Composable
import com.sixbynine.transit.path.PathWidgetPreview
import com.sixbynine.transit.path.PreviewTheme
import com.sixbynine.transit.path.app.settings.AvoidMissingTrains.OffPeak
import com.sixbynine.transit.path.app.settings.CommutingConfiguration
import com.sixbynine.transit.path.app.settings.StationLimit.Six
import com.sixbynine.transit.path.app.settings.TimeDisplay.Clock
import com.sixbynine.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.State
import com.sixbynine.transit.path.app.ui.advancedsettings.AdvancedSettingsScreen

@PathWidgetPreview
@Composable
fun AdvancedSettingsPreview() {
    PreviewTheme {
        AdvancedSettingsScreen(
            state = State(
                avoidMissingTrains = OffPeak,
                stationLimit = Six,
                timeDisplay = Clock,
                commutingConfiguration = CommutingConfiguration.default()
            ),
            onIntent = {

            }
        )
    }
}