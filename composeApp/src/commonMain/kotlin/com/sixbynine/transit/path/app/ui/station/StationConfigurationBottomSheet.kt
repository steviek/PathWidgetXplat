package com.sixbynine.transit.path.app.ui.station

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.MR.strings
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.app.station.StationSelection
import com.sixbynine.transit.path.app.station.StationSelectionManager
import com.sixbynine.transit.path.app.ui.AppUiScope
import com.sixbynine.transit.path.app.ui.PathBottomSheet
import com.sixbynine.transit.path.app.ui.gutter
import com.sixbynine.transit.path.app.ui.icon.IconType.ArrowDown
import com.sixbynine.transit.path.app.ui.icon.IconType.ArrowUp
import com.sixbynine.transit.path.app.ui.icon.NativeIconButton
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun AppUiScope.StationConfigurationBottomSheet(onDismiss: (StationSelection) -> Unit) {
    var currentSelection by remember {
        mutableStateOf(StationSelectionManager.selection.value)
    }
    StationConfigurationBottomSheet(
        selection = currentSelection,
        onStationCheckedChanged = { station, isChecked ->
            currentSelection = currentSelection.copy(
                selectedStations = if (isChecked) {
                    currentSelection.selectedStations + station
                } else {
                    currentSelection.selectedStations - station
                },
                unselectedStations = if (isChecked) {
                    currentSelection.unselectedStations - station
                } else {
                    listOf(station) + currentSelection.unselectedStations
                }
            )
        },
        onMoveUpClicked = { station ->
            currentSelection = currentSelection.copy(
                selectedStations = currentSelection.selectedStations.toMutableList().apply {
                    indexOf(station).takeIf { it > 0 }?.let { index ->
                        removeAt(index)
                        add(index - 1, station)
                    }
                },
            )
        },
        onMoveDownClicked = { station ->
            currentSelection = currentSelection.copy(
                selectedStations = currentSelection.selectedStations.toMutableList().apply {
                    indexOf(station).takeIf { it >= 0 }?.let { index ->
                        removeAt(index)
                        add(index + 1, station)
                    }
                },
            )
        },
        onDismiss = onDismiss
    )
}

@Composable
fun AppUiScope.StationConfigurationBottomSheet(
    selection: StationSelection,
    onStationCheckedChanged: (Station, Boolean) -> Unit,
    onMoveUpClicked: (Station) -> Unit,
    onMoveDownClicked: (Station) -> Unit,
    onDismiss: (StationSelection) -> Unit,
) {
    PathBottomSheet(onDismissRequest = { onDismiss(selection) }) {
        StationList(selection, onStationCheckedChanged, onMoveUpClicked, onMoveDownClicked)
    }
}

@Composable
private fun AppUiScope.StationList(
    selection: StationSelection,
    onStationCheckedChanged: (Station, Boolean) -> Unit,
    onMoveUpClicked: (Station) -> Unit,
    onMoveDownClicked: (Station) -> Unit,
) {
    LazyColumn {
        selection.selectedStations.forEachIndexed { index, station ->
            item(station.pathApiName) {
                StationRow(
                    modifier = Modifier.animateItemPlacement(),
                    station = station,
                    canMoveUp = index > 0,
                    canMoveDown = index < selection.selectedStations.lastIndex,
                    isChecked = true,
                    onCheckedChange = { onStationCheckedChanged(station, it) },
                    onMoveUpClicked = { onMoveUpClicked(station) },
                    onMoveDownClicked = { onMoveDownClicked(station) },
                )
            }

        }

        selection.unselectedStations.forEachIndexed { index, station ->
            item(station.pathApiName) {
                StationRow(
                    modifier = Modifier.animateItemPlacement(),
                    station = station,
                    canMoveUp = index > 0,
                    canMoveDown = index < selection.unselectedStations.lastIndex,
                    isChecked = false,
                    onCheckedChange = { onStationCheckedChanged(station, it) },
                    onMoveUpClicked = { onMoveUpClicked(station) },
                    onMoveDownClicked = { onMoveDownClicked(station) },
                )
            }

        }
    }

}

@Composable
private fun AppUiScope.StationRow(
    station: Station,
    isChecked: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onMoveUpClicked: () -> Unit,
    onMoveDownClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.heightIn(min = 48.dp)
            .fillMaxWidth()
            .toggleable(value = isChecked, onValueChange = onCheckedChange)
            .padding(vertical = 8.dp, horizontal = gutter()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = null,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = station.displayName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (isChecked && canMoveUp) {
            NativeIconButton(
                icon = ArrowUp,
                contentDescription = stringResource(strings.move_up),
                tint = MaterialTheme.colorScheme.primary,
                onClick = onMoveUpClicked,
                buttonSize = 32.dp
            )
        } else {
            Spacer(Modifier.width(32.dp))
        }
        if (isChecked && canMoveDown) {
            NativeIconButton(
                icon = ArrowDown,
                contentDescription = stringResource(strings.move_down),
                tint = MaterialTheme.colorScheme.primary,
                onClick = onMoveDownClicked,
                buttonSize = 32.dp
            )
        } else {
            Spacer(Modifier.width(32.dp))
        }
    }
}
