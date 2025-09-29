package com.desaiwang.transit.path.app.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.desaiwang.transit.path.PathWidgetPreview
import com.desaiwang.transit.path.PreviewTheme
import com.desaiwang.transit.path.api.LocationSetting
import com.desaiwang.transit.path.api.Stations
import com.desaiwang.transit.path.api.TrainFilter
import com.desaiwang.transit.path.app.settings.TimeDisplay
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Intent.EditClicked
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Intent.RetryClicked
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Intent.StationSelectionDialogDismissed
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Intent.StopEditingClicked
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Intent.UpdateNowClicked
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.State
import com.desaiwang.transit.path.app.ui.home.HomeScreenViewModel.Companion.toDepartureBoardData
import com.desaiwang.transit.path.app.ui.layout.LayoutOption.TwoColumns
import com.desaiwang.transit.path.widget.Fixtures
import kotlinx.coroutines.runBlocking

@PathWidgetPreview
@Composable
fun HomeScreenPreview() {
    PreviewTheme {
        var isEditing by remember { mutableStateOf(true) }
        HomeScreen(
            state = State(
                isTablet = false,
                isEditing = isEditing,
                selectedStations = listOf(Stations.Hoboken),
                unselectedStations = emptyList(),
                layoutOption = TwoColumns,
                isLoading = false,
                hasError = false,
                data = runBlocking { Fixtures.widgetData().toDepartureBoardData(
                    timeDisplay = TimeDisplay.Relative,
                    trainFilter = TrainFilter.All,
                    locationSetting = LocationSetting.Disabled,
                ) },
                timeDisplay = TimeDisplay.Relative,
            ),
            onIntent = {
                when (it) {
                    is EditClicked -> {
                        isEditing = true
                    }

                    StopEditingClicked -> {
                        isEditing = false
                    }

                    is RetryClicked -> {
                    }


                    UpdateNowClicked -> {}

                    is StationSelectionDialogDismissed -> TODO()
                    else -> {}
                }
            })
    }
}