package com.desaiwang.transit.path.app.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.desaiwang.transit.path.PathWidgetPreview
import com.desaiwang.transit.path.PreviewTheme
import com.desaiwang.transit.path.api.Line
import com.desaiwang.transit.path.api.StationSort
import com.desaiwang.transit.path.api.TrainFilter.Interstate
import com.desaiwang.transit.path.app.settings.TimeDisplay.Relative
import com.desaiwang.transit.path.app.ui.settings.SettingsContract.LocationSettingState
import com.desaiwang.transit.path.app.ui.settings.SettingsContract.State

@PathWidgetPreview
@Composable
fun SettingsScreenPreview() {
    PreviewTheme {
        var timeDisplay by remember { mutableStateOf(Relative) }
        val settingsScope = SettingsScope(
            state = State(
                locationSetting = LocationSettingState.Disabled,
                trainFilter = Interstate,
                lines = Line.permanentLines.toSet(),
                stationSort = StationSort.Alphabetical,
                showPresumedTrains = false,
                hasLocationPermission = false,
                devOptionsEnabled = false,
            ),
            onIntent = { intent ->

            }
        )
        settingsScope.Content()
    }
}
