package com.desaiwang.transit.path.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import com.desaiwang.transit.path.api.Station
import com.desaiwang.transit.path.api.StationSort
import com.desaiwang.transit.path.app.ui.common.AppUiTrainData
import com.desaiwang.transit.path.app.ui.common.TrainLineContentWithWithBackfillBottomSheet
import com.desaiwang.transit.path.app.ui.gutter
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Intent.AddStationClicked
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Intent.MoveStationDownClicked
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Intent.MoveStationUpClicked
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Intent.RemoveStationClicked
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Intent.StationClicked
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Intent.StationLongClicked
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.StationData
import com.desaiwang.transit.path.app.ui.home.TrainGrouper.groupTrains
import com.desaiwang.transit.path.app.ui.icon.IconType
import com.desaiwang.transit.path.app.ui.icon.NativeIconButton
import com.desaiwang.transit.path.util.conditional
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.add_station
import pathwidgetxplat.composeapp.generated.resources.cannot_move_explanation_closest
import pathwidgetxplat.composeapp.generated.resources.cannot_move_explanation_state
import pathwidgetxplat.composeapp.generated.resources.delete
import pathwidgetxplat.composeapp.generated.resources.move_down
import pathwidgetxplat.composeapp.generated.resources.move_up
import pathwidgetxplat.composeapp.generated.resources.station_empty
import pathwidgetxplat.composeapp.generated.resources.station_empty_filters

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
    val firstStation = remember { MutableStateFlow(null as String?) }
    firstStation.value = data?.stations?.firstOrNull()?.id
    LaunchedEffect(listState, firstStation) {
        firstStation.filterNotNull().drop(1).collect {
            listState.scrollToItem(0)
        }
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

    Card(
        modifier = modifier.combinedClickable(
            onLongClick = { onIntent(StationLongClicked(station.id)) },
            onClick = { onIntent(StationClicked(station.id)) }
        ),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isSystemInDarkTheme()) {
                Color(0xFF0E0E0E) // Dark theme
            } else {
                Color(0xFFEEEEEE) // Light theme
            }
        )
    ) {
        // Top border only
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(horizontal = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isSystemInDarkTheme()) {
                            Color.White // White border in dark theme
                        } else {
                            Color.Black // Black border in light theme
                        }
                    )
            )
        }
        Column(Modifier.padding(bottom = 8.dp)) {
            StationHeader(
                modifier = Modifier.fillMaxSize(),
                canMoveDown = canMoveDown,
                canMoveUp = canMoveUp,
                canDelete = !station.isClosest,
                data = station
            )

            AlertBox(
                text = station.alertText,
                url = station.alertUrl,
                colors = if (station.alertIsWarning) {
                    AlertBoxColors.Warning
                } else {
                    AlertBoxColors.Info
                },
            )

            StationTrains(station)
        }

        var showEmptyText by remember { mutableStateOf(false) }
        LaunchedEffect(station.trains.isEmpty(), state.isLoading) {
            if (station.trains.isEmpty()) {
                if (!state.isLoading) {
                    showEmptyText = true
                }
            } else {
                showEmptyText = false
            }
        }
        if (showEmptyText) {
            Box(
                Modifier.fillMaxWidth().padding(horizontal = gutter()).padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(
                        if (station.hasTrainsBeforeFilters) {
                            string.station_empty_filters
                        } else {
                            string.station_empty
                        }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            }
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

    val groupedTrains = groupTrains(station.station, station.trains)
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
        modifier = modifier.heightIn(40.dp).fillMaxWidth().padding(horizontal = gutter()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            Text(
                text = data.station.displayName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        AnimatedVisibility(state.isEditing) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                } else {
                    Spacer(Modifier.width(48.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeScreenScope.TrainLine(
    station: Station,
    data: AppUiTrainData,
    modifier: Modifier = Modifier
) {
    TrainLine(station, listOf(data), modifier)
}

@Composable
private fun HomeScreenScope.TrainLine(
    station: Station,
    data: List<AppUiTrainData>,
    modifier: Modifier = Modifier
) {
    TrainLineContentWithWithBackfillBottomSheet(
        data = data,
        timeDisplay = state.timeDisplay,
        station = station,
        modifier = modifier.padding(horizontal = gutter(), vertical = 4.dp),
    )
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