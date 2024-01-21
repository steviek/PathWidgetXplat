package com.sixbynine.transit.path.app.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.sixbynine.transit.path.PathWidgetPreview
import com.sixbynine.transit.path.PreviewTheme
import com.sixbynine.transit.path.api.StationFilter.Interstate
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.SettingsTimeDisplayChanged
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.State
import com.sixbynine.transit.path.app.ui.layout.LayoutOption.TwoColumns

@PathWidgetPreview
@Composable
fun SettingsBottomSheetPreview() {
    PreviewTheme {
        var timeDisplay by remember { mutableStateOf(TimeDisplay.Relative) }
        val scope = HomeScreenScope(
            state = State(
                isTablet = false,
                selectedStations = emptyList(),
                unselectedStations = emptyList(),
                layoutOption = TwoColumns,
                isEditing = false,
                timeDisplay = timeDisplay,
                stationFilter = Interstate
            ),
            onIntent = {
                when (it) {
                    is SettingsTimeDisplayChanged -> {
                        timeDisplay = it.timeDisplay
                    }

                    else -> {}
                }
            },
        )
        scope.SettingsBottomSheet()
    }
}
