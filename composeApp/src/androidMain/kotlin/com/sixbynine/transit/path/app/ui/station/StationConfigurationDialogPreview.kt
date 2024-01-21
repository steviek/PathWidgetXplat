package com.sixbynine.transit.path.app.ui.station

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.sixbynine.transit.path.PreviewTheme
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.app.station.StationSelection

@Preview(apiLevel = 33)
@Composable
fun StationConfigurationDialogPreview() {
    PreviewTheme {
        var stationSelection by remember {
            mutableStateOf(
                StationSelection(
                    selectedStations = Stations.All,
                    unselectedStations = emptyList()
                )
            )

        }
        StationConfigurationBottomSheet(
            selection = stationSelection,
            onStationCheckedChanged = { station, isChecked ->
                stationSelection = stationSelection.copy(
                    selectedStations = if (isChecked) {
                        stationSelection.selectedStations + station
                    } else {
                        stationSelection.selectedStations - station
                    },
                    unselectedStations = if (isChecked) {
                        stationSelection.unselectedStations - station
                    } else {
                        listOf(station) + stationSelection.unselectedStations
                    }
                )
            },
            onMoveUpClicked = { station ->
                val newSelectedStations = stationSelection.selectedStations.toMutableList()
                val newUnselectedStations = stationSelection.unselectedStations.toMutableList()

                fun MutableList<Station>.doSwap() {
                    indexOf(station).takeIf { it > 0 }?.let { index ->
                        removeAt(index)
                        add(index - 1, station)
                    }
                }

                newSelectedStations.doSwap()
                newUnselectedStations.doSwap()


                stationSelection = stationSelection.copy(
                    selectedStations = newSelectedStations,
                    unselectedStations = newUnselectedStations
                )
            },
            onMoveDownClicked = { station ->
                val newSelectedStations = stationSelection.selectedStations.toMutableList()
                val newUnselectedStations = stationSelection.unselectedStations.toMutableList()

                fun MutableList<Station>.doSwap() {
                    indexOf(station).takeIf { it >= 0 }?.let { index ->
                        removeAt(index)
                        add(index + 1, station)
                    }
                }

                newSelectedStations.doSwap()
                newUnselectedStations.doSwap()


                stationSelection = stationSelection.copy(
                    selectedStations = newSelectedStations,
                    unselectedStations = newUnselectedStations
                )
            },
            onDismiss = {},
        )
    }
}