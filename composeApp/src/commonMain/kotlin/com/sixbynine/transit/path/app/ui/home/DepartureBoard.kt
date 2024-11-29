package com.sixbynine.transit.path.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.StationClicked
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
            item(station.id) {
                Station(
                    station,
                    index,
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp).animateItemPlacement()
                )
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

@Composable
private fun HomeScreenScope.Station(
    station: StationData,
    index: Int,
    modifier: Modifier = Modifier
) {
    val data = state.data ?: return
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

    Card(modifier.clickable { onIntent(StationClicked(station.id)) }) {
        Column(Modifier.padding(bottom = 8.dp)) {
            StationHeader(
                modifier = Modifier.fillMaxSize(),
                canMoveDown = canMoveDown,
                canMoveUp = canMoveUp,
                canDelete = !station.isClosest,
                data = station
            )

            StationAlertBox(
                text = station.alertText,
                url = station.alertUrl,
                colors = if (station.alertIsWarning) {
                    StationAlertBoxColors.Warning
                } else {
                    StationAlertBoxColors.Info
                }
            )

            StationTrains(station)
        }

        if (station.trains.isEmpty()) {
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

        if (state.isEditing && nextStation != null && !canMoveDown) {
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

@Composable
private fun HomeScreenScope.StationTrains(station: StationData) {
    if (!state.groupByDestination) {
        station.trains.fastForEach { train ->
            TrainLine(
                station.station,
                train,
                modifier = Modifier.fillMaxSize()
            )
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
        TrainLine(
            station.station,
            trains,
            modifier = Modifier.fillMaxSize()
                .conditional(!isLastGroup) { padding(bottom = 8.dp) }
        )
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
        modifier = modifier.heightIn(40.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1f).padding(start = gutter()),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = data.station.displayName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

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