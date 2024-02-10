package com.sixbynine.transit.path.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.MR.strings
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.app.ui.AppUiScope
import com.sixbynine.transit.path.app.ui.ViewModelScreen
import com.sixbynine.transit.path.app.ui.gutter
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Effect.NavigateToSettings
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.AddStationClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.ConstraintsChanged
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.EditClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.MoveStationDownClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.MoveStationUpClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.RemoveStationClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.RetryClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.SettingsClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.StopEditingClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.State
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.StationData
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.TrainData
import com.sixbynine.transit.path.app.ui.icon.IconType
import com.sixbynine.transit.path.app.ui.icon.NativeIconButton
import com.sixbynine.transit.path.app.ui.station.AddStationBottomSheet
import com.sixbynine.transit.path.app.ui.theme.Dimensions
import dev.icerock.moko.resources.compose.stringResource

class HomeScreenScope(
    val state: State,
    val onIntent: (Intent) -> Unit,
) : AppUiScope {
    override val isTablet = state.isTablet
}

@Composable
fun HomeScreen() {
    BoxWithConstraints {
        ViewModelScreen(
            viewModelKey = "home-screen",
            createViewModel = {
                HomeScreenViewModel(
                    maxWidth = maxWidth,
                    maxHeight = maxHeight
                )
            },
            onEffect = { effect ->
                when (effect) {
                    is NavigateToSettings -> {
                        navigator.navigate("/settings")
                    }
                }
            }
        ) {
            LaunchedEffect(maxWidth, maxHeight) {
                onIntent(ConstraintsChanged(maxWidth, maxHeight))
            }

            HomeScreen(state, onIntent)
        }

    }
}

@Composable
fun HomeScreen(state: State, onIntent: (Intent) -> Unit) {
    val scope = HomeScreenScope(state, onIntent)
    Scaffold(
        bottomBar = {
            Column {
                Divider()
                scope.DepartureBoardFooter()
            }
        }
    ) { contentPadding ->
        scope.MainContent(Modifier.padding(contentPadding))
    }
}

@Composable
private fun HomeScreenScope.MainContent(modifier: Modifier) {
    Column(
        modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when {
            state.isLoading && state.data == null -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(60.dp),
                    strokeWidth = 6.dp,
                )
            }

            state.hasError && state.data == null -> {
                ErrorState()
            }

            else -> {
                Row(Modifier.padding(horizontal = gutter() - 12.dp).fillMaxWidth()) {
                    TextButton(
                        onClick = {
                            onIntent(SettingsClicked)
                        }
                    ) {
                        Text(stringResource(strings.settings))
                    }

                    Spacer(Modifier.weight(1f))

                    TextButton(
                        onClick = {
                            onIntent(if (state.isEditing) StopEditingClicked else EditClicked)
                        }
                    ) {
                        Text(stringResource(if (state.isEditing) strings.done else strings.edit))
                    }
                }

                DepartureBoard()
            }
        }
    }

    AddStationBottomSheet()
}

@Composable
private fun HomeScreenScope.ErrorState() {
    Column(
        modifier = Modifier.padding(gutter()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(strings.failed_to_fetch),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onIntent(RetryClicked) }) {
            Text(
                text = stringResource(strings.retry)
            )
        }
    }
}

@Composable
private fun HomeScreenScope.DepartureBoard() {
    val data = state.data ?: return
    val gridState = rememberLazyGridState()
    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Adaptive(minSize = 300.dp),
        state = gridState,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.gutter(isTablet = isTablet)),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        data.stations.forEachIndexed { index, station ->
            val canMoveUp = run {
                val prevStation = data.stations.getOrNull(index - 1) ?: return@run false
                if (state.stationSort == StationSort.Alphabetical) return@run true
                prevStation.state == station.state
            }

            val nextStation = data.stations.getOrNull(index + 1)
            val canMoveDown = run {
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
                    data = station
                )
            }

            station.trains.forEach { train ->
                item(station.id + train.id) {
                    StationTrain(
                        data = station,
                        train = train,
                        modifier = Modifier.fillMaxSize().animateItemPlacement()
                    )
                }
            }

            if (state.isEditing &&
                state.stationSort != StationSort.Alphabetical &&
                nextStation != null &&
                station.state != nextStation.state
            ) {
                item {
                    CannotMoveExplanation()
                }
            }
        }

        if (state.unselectedStations.isNotEmpty()) {
            item("add", span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.padding(horizontal = gutter(), vertical = 16.dp)) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onIntent(AddStationClicked) }) {
                        Text(stringResource(strings.add_station))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreenScope.StationHeader(
    data: StationData,
    canMoveDown: Boolean,
    canMoveUp: Boolean,
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
                        contentDescription = stringResource(strings.move_down),
                        onClick = { onIntent(MoveStationDownClicked(data.id)) }
                    )
                } else {
                    Spacer(Modifier.width(48.dp))
                }

                if (canMoveUp) {
                    NativeIconButton(
                        icon = IconType.ArrowUp,
                        contentDescription = stringResource(strings.move_up),
                        onClick = { onIntent(MoveStationUpClicked(data.id)) }
                    )
                } else {
                    Spacer(Modifier.width(48.dp))
                }

                NativeIconButton(
                    icon = IconType.Delete,
                    contentDescription = stringResource(strings.delete),
                    onClick = { onIntent(RemoveStationClicked(data.id)) }
                )
            }
        }
    }
}

@Composable
private fun HomeScreenScope.StationTrain(
    data: StationData,
    train: TrainData,
    modifier: Modifier = Modifier
) {
    TrainLine(data.station, train, modifier)
}

@Composable
private fun HomeScreenScope.TrainLine(
    station: Station,
    data: TrainData,
    modifier: Modifier = Modifier
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    TrainLineContent(
        data,
        modifier = Modifier.run {
            if (data.isBackfilled) {
                clickable { showBottomSheet = true }
            } else {
                this
            }
        }
            .then(modifier)
            .padding(horizontal = gutter(), vertical = 4.dp)
            .fillMaxWidth()
    )

    if (data.backfill != null) {
        BackfillBottomSheet(
            isShown = showBottomSheet,
            station = station,
            trainData = data,
            source = data.backfill,
            onDismiss = { showBottomSheet = false }
        )
    }
}

@Composable
private fun HomeScreenScope.CannotMoveExplanation() {
    Box(Modifier.fillMaxWidth().padding(gutter()), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(strings.cannot_move_explanation),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
