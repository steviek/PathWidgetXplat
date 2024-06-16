package com.sixbynine.transit.path.app.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.sixbynine.transit.path.PathWidgetPreview
import com.sixbynine.transit.path.PreviewTheme
import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.TrainFilter.Interstate
import com.sixbynine.transit.path.app.settings.TimeDisplay.Relative
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.LocationSettingState
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.State

@PathWidgetPreview
@Composable
fun SettingsScreenPreview() {
    PreviewTheme {
        var timeDisplay by remember { mutableStateOf(Relative) }
        val settingsScope = SettingsScope(
            state = State(
                locationSetting = LocationSettingState.Disabled,
                trainFilter = Interstate,
                lines = Line.entries.toSet(),
                stationSort = StationSort.Alphabetical,
                showPresumedTrains = false,
                hasLocationPermission = false,
            ),
            onIntent = { intent ->

            }
        )
        settingsScope.Content()
    }
}
