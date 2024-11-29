package com.sixbynine.transit.path.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.app.ui.gutter
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.AddStationClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.MoveStationDownClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.MoveStationUpClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.RemoveStationClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.StationData
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.TrainData
import com.sixbynine.transit.path.app.ui.icon.IconType
import com.sixbynine.transit.path.app.ui.icon.NativeIconButton
import com.sixbynine.transit.path.util.conditional
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.add_station
import pathwidgetxplat.composeapp.generated.resources.cannot_move_explanation_closest
import pathwidgetxplat.composeapp.generated.resources.cannot_move_explanation_state
import pathwidgetxplat.composeapp.generated.resources.delete
import pathwidgetxplat.composeapp.generated.resources.move_down
import pathwidgetxplat.composeapp.generated.resources.move_up
import pathwidgetxplat.composeapp.generated.resources.station_empty

@Composable
fun HomeScreenScope.DepartureBoard() {
    val data = state.data
    val listState = rememberLazyListState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        data?.stations?.forEachIndexed { index, station ->
            val canMoveUp = run {
                if (station.isClosest) return@run false
                val prevStation = data.stations.getOrNull(index - 1) ?: return@run false
                if (state.stationSort == StationSort.Alphabetical) return@run true
                prevStation.state == station.state
            }

            val nextStation = data.stations.getOrNull(index + 1)
            val canMoveDown = run {
                if (station.isClosest) return@run false
                nextStation ?: return@run false
                if (state.stationSort == StationSort.Alphabetical) return@run true
                nextStation.state == station.state
            }

            item(station.id) {
                StationHeader(
                    modifier = Modifier.fillMaxSize().animateItemPlacement()
                        .padding(top = if (index == 0) 0.dp else 16.dp),
                    canMoveDown = canMoveDown,
                    canMoveUp = canMoveUp,
                    canDelete = !station.isClosest,
                    data = station
                )
            }

            item(station.id + "-alerts") {
                StationAlertBox(
                    text = station.alertText,
                    url = station.alertUrl,
                    colors = if (station.alertIsWarning) {
                        StationAlertBoxColors.Warning
                    } else {
                        StationAlertBoxColors.Info
                    }
                )
            }

            StationTrains(this, station)

            if (station.trains.isEmpty()) {
                item(station.id + "-empty") {
                    Box(
                        Modifier.fillMaxWidth().padding(horizontal = gutter()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(string.station_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                    }
                }
            }

            if (state.isEditing && nextStation != null && !canMoveDown) {
                item {
                    val text = if (station.isClosest) {
                        stringResource(
                            string.cannot_move_explanation_closest,
                            station.station.displayName
                        )
                    } else {
                        stringResource(string.cannot_move_explanation_state)
                    }
                    CannotMoveExplanation(text)
                }
            }
        }

        if (state.unselectedStations.isNotEmpty() && data != null) {
            item("add") {
                Box(Modifier.padding(horizontal = gutter(), vertical = 16.dp)) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onIntent(AddStationClicked) }) {
                        Text(stringResource(string.add_station))
                    }
                }
            }
        }
    }

    // Scroll the list to the top whenever the first station changes.
    LaunchedEffect(listState, data?.stations?.firstOrNull()?.id) {
        listState.scrollToItem(0)
    }
}

private fun HomeScreenScope.StationTrains(listScope: LazyListScope, station: StationData) {
    if (!state.groupByDestination) {
        station.trains.fastForEach { train ->
            listScope.item(station.id + train.id) {
                TrainLine(
                    station.station,
                    train,
                    modifier = Modifier.fillMaxSize().animateItemPlacement()
                )
            }
        }
        return
    }

    val groupedTrains = arrayListOf<ArrayList<TrainData>>()

    station.trains.fastForEach { train ->
        val group = groupedTrains.find { it.firstOrNull()?.title == train.title }
        if (group == null) {
            groupedTrains += arrayListOf(train)
        } else {
            group.add(train)
        }
    }

    groupedTrains.fastForEachIndexed { index, trains ->
        val isLastGroup = index == groupedTrains.lastIndex
        listScope.item(station.id + trains.first().id) {
            TrainLine(
                station.station,
                trains,
                modifier = Modifier.fillMaxSize()
                    .conditional(!isLastGroup) { padding(bottom = 8.dp) }
                    .animateItemPlacement()
            )
        }
    }
}


@Composable
private fun HomeScreenScope.StationHeader(
    data: StationData,
    canMoveDown: Boolean,
    canMoveUp: Boolean,
    canDelete: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.heightIn(48.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f).padding(start = gutter()),
            text = data.station.displayName,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        AnimatedVisibility(
            state.isEditing,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(Modifier.padding(end = gutter() - 12.dp)) {
                if (canMoveDown) {
                    NativeIconButton(
                        icon = IconType.ArrowDown,
                        contentDescription = stringResource(string.move_down),
                        onClick = { onIntent(MoveStationDownClicked(data.id)) }
                    )
                } else {
                    Spacer(Modifier.width(48.dp))
                }

                if (canMoveUp) {
                    NativeIconButton(
                        icon = IconType.ArrowUp,
                        contentDescription = stringResource(string.move_up),
                        onClick = { onIntent(MoveStationUpClicked(data.id)) }
                    )
                } else {
                    Spacer(Modifier.width(48.dp))
                }

                if (canDelete) {
                    NativeIconButton(
                        icon = IconType.Delete,
                        contentDescription = stringResource(string.delete),
                        onClick = { onIntent(RemoveStationClicked(data.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeScreenScope.TrainLine(
    station: Station,
    data: TrainData,
    modifier: Modifier = Modifier
) {
    TrainLine(station, listOf(data), modifier)
}

@Composable
private fun HomeScreenScope.TrainLine(
    station: Station,
    data: List<TrainData>,
    modifier: Modifier = Modifier
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    TrainLineContent(
        data,
        modifier = Modifier.run {
            if (data.firstOrNull()?.isBackfilled == true) {
                clickable { showBottomSheet = true }
            } else {
                this
            }
        }
            .then(modifier)
            .padding(horizontal = gutter(), vertical = 4.dp)
            .fillMaxWidth()
    )

    val firstTrain = data.firstOrNull()
    val backfill = firstTrain?.backfill
    if (backfill != null) {
        BackfillBottomSheet(
            isShown = showBottomSheet,
            station = station,
            trainData = firstTrain,
            source = backfill,
            onDismiss = { showBottomSheet = false }
        )
    }
}


@Composable
private fun HomeScreenScope.CannotMoveExplanation(text: String) {
    Box(
        Modifier.fillMaxWidth().padding(horizontal = gutter()).padding(top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}