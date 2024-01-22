package com.sixbynine.transit.path.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.MR.strings
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.app.ui.AppUiScope
import com.sixbynine.transit.path.app.ui.filter.FilterBottomSheet
import com.sixbynine.transit.path.app.ui.gutter
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.AddStationClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.EditClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.MoveStationDownClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.MoveStationUpClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.RemoveStationClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.RetryClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.SettingsClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.StationFilterDialogDismissed
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.StationSelectionDialogDismissed
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.StopEditingClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.State
import com.sixbynine.transit.path.app.ui.home.TimeDisplay.Clock
import com.sixbynine.transit.path.app.ui.home.TimeDisplay.Relative
import com.sixbynine.transit.path.app.ui.icon.IconType
import com.sixbynine.transit.path.app.ui.icon.NativeIconButton
import com.sixbynine.transit.path.app.ui.station.AddStationBottomSheet
import com.sixbynine.transit.path.app.ui.station.StationConfigurationBottomSheet
import com.sixbynine.transit.path.app.ui.theme.Dimensions
import com.sixbynine.transit.path.widget.WidgetData.StationData
import com.sixbynine.transit.path.widget.WidgetData.TrainData
import com.sixbynine.transit.path.widget.WidgetDataFormatter
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.datetime.Instant

class HomeScreenScope(
    val state: State,
    val onIntent: (Intent) -> Unit,
) : AppUiScope {
    override val isTablet = state.isTablet
}

@Composable
fun HomeScreen() {
    BoxWithConstraints {
        val viewModel = getViewModel(
            key = "home-screen",
            factory = viewModelFactory {
                HomeScreenViewModel(
                    maxWidth = maxWidth,
                    maxHeight = maxHeight
                )
            }
        )
        LaunchedEffect(maxWidth, maxHeight) {
            viewModel.onConstraintsChanged(maxWidth, maxHeight)
        }
        val state by viewModel.state.collectAsState()
        HomeScreen(state, viewModel::onIntent)
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

    if (state.showStationSelectionDialog) {
        StationConfigurationBottomSheet(
            onDismiss = { onIntent(StationSelectionDialogDismissed(it)) }
        )
    }

    if (state.showFilterDialog) {
        FilterBottomSheet(
            onDismiss = { onIntent(StationFilterDialogDismissed) }
        )
    }

    if (state.showSettingsBottomSheet) {
        SettingsBottomSheet()
    }

    if (state.showAddStationBottomSheet) {
        AddStationBottomSheet()
    }
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
        verticalArrangement = Arrangement.spacedBy(24.dp),
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
                StationView(
                    modifier = Modifier.fillMaxSize().animateItemPlacement(),
                    fetchTime = data.fetchTime,
                    canMoveDown = canMoveDown,
                    canMoveUp = canMoveUp,
                    data = station
                )
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
                Box(Modifier.padding(horizontal = gutter())) {
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
private fun HomeScreenScope.StationView(
    fetchTime: Instant,
    data: StationData,
    canMoveDown: Boolean,
    canMoveUp: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.heightIn(48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f).padding(start = gutter()),
                text = data.displayName,
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

        Column(
            modifier = Modifier.padding(horizontal = gutter()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            data.trains.forEach {
                TrainLine(fetchTime, it)
            }
        }

    }
}

@Composable
private fun HomeScreenScope.TrainLine(fetchTime: Instant, data: TrainData) {
    Row {
        Box(Modifier.size(24.dp)) {
            data.colors.firstOrNull()?.let {
                Box(Modifier.size(24.dp).clip(CircleShape).background(it.color))
            }

            data.colors.getOrNull(1)?.let {
                Box(
                    Modifier.size(24.dp)
                        .clip(CircleShape)
                        .padding(top = 12.dp)
                        .clip(RectangleShape)
                        .background(it.color)
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = data.title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            modifier = Modifier.widthIn(min = 60.dp),
            textAlign = TextAlign.End,
            text = when (state.timeDisplay) {
                Relative -> WidgetDataFormatter.formatRelativeTime(fetchTime, data.projectedArrival)
                Clock -> WidgetDataFormatter.formatTime(data.projectedArrival)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
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
