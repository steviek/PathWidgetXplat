package com.sixbynine.transit.path.app.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.sixbynine.transit.path.PathWidgetPreview
import com.sixbynine.transit.path.PreviewTheme
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.app.settings.TimeDisplay
import com.sixbynine.transit.path.app.ui.home.ConfigurationItem.Settings
import com.sixbynine.transit.path.app.ui.home.ConfigurationItem.Station
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.ConfigurationChipClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.EditClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.RetryClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.StationSelectionDialogDismissed
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.StopEditingClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.UpdateNowClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.State
import com.sixbynine.transit.path.app.ui.home.HomeScreenViewModel.Companion.toDepartureBoardData
import com.sixbynine.transit.path.app.ui.layout.LayoutOption.TwoColumns
import com.sixbynine.transit.path.widget.Fixtures

@PathWidgetPreview
@Composable
fun HomeScreenPreview() {
    PreviewTheme {
        var showConfiguration by remember { mutableStateOf(true) }
        var showSettingsBottomSheet by remember { mutableStateOf(false) }
        HomeScreen(
            state = State(
                isTablet = false,
                isEditing = showConfiguration,
                selectedStations = listOf(Stations.Hoboken),
                unselectedStations = emptyList(),
                layoutOption = TwoColumns,
                isLoading = false,
                hasError = false,
                data = Fixtures.widgetData().toDepartureBoardData(TimeDisplay.Relative),
                timeDisplay = TimeDisplay.Relative,
            ),
            onIntent = {
                when (it) {
                    is EditClicked -> {
                        showConfiguration = true
                    }

                    StopEditingClicked -> {
                        showConfiguration = false
                    }

                    is RetryClicked -> {
                    }


                    UpdateNowClicked -> {}

                    is ConfigurationChipClicked -> {
                        when (it.item) {
                            Station -> {
                                showSettingsBottomSheet = true
                            }
                            Settings -> {}
                        }
                    }
                    is StationSelectionDialogDismissed -> TODO()
                    else -> {}
                }
            })
    }
}