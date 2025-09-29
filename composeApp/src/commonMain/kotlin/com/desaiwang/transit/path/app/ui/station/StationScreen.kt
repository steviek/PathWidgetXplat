package com.desaiwang.transit.path.app.ui.station

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.desaiwang.transit.path.app.ui.ViewModelScreen
import com.desaiwang.transit.path.app.ui.common.TrainLineContentWithWithBackfillBottomSheet
import com.desaiwang.transit.path.app.ui.home.AlertBox
import com.desaiwang.transit.path.app.ui.home.AlertBoxColors
import com.desaiwang.transit.path.app.ui.home.TrainGrouper.groupTrains
import com.desaiwang.transit.path.app.ui.icon.IconType.Back
import com.desaiwang.transit.path.app.ui.icon.NativeIconButton
import com.desaiwang.transit.path.app.ui.station.StationContract.Effect.GoBack
import com.desaiwang.transit.path.app.ui.station.StationContract.Intent
import com.desaiwang.transit.path.app.ui.station.StationContract.Intent.BackClicked
import com.desaiwang.transit.path.app.ui.station.StationContract.State
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.back
import pathwidgetxplat.composeapp.generated.resources.other_trains

@Composable
fun StationScreen(stationId: String?) {
    ViewModelScreen(
        viewModelKey = "station_$stationId",
        createViewModel = { StationViewModel(stationId) },
        onEffect = { effect ->
            when (effect) {
                is GoBack -> navigator.goBack()
            }
        },
        content = { Content() }
    )
}

@Composable
fun StationScreen(state: State, onIntent: (Intent) -> Unit) {
    val scope = StationScope(state, onIntent)
    scope.Content()
}

@Composable
private fun StationScope.Content() {
    val stationData = state.station ?: return
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stationData.station.displayName) },
                navigationIcon = {
                    NativeIconButton(
                        Back,
                        contentDescription = stringResource(string.back),
                        onClick = { onIntent(BackClicked) })
                }
            )
        }
    ) { contentPadding ->
        Column(Modifier.padding(contentPadding)) {
            LazyColumn(Modifier.weight(1f)) {
                if (state.groupByDestination) {
                    groupTrains(
                        state.station.station,
                        state.trainsMatchingFilters,
                    ).fastForEach { trains ->
                        item(trains.firstOrNull()?.id) {
                            TrainLineContentWithWithBackfillBottomSheet(
                                data = trains,
                                timeDisplay = state.timeDisplay,
                                station = state.station.station,
                                modifier = Modifier.heightIn(48.dp)
                                    .animateItemPlacement()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }

                    if (state.trainsMatchingFilters.isNotEmpty() &&
                        state.otherTrains.isNotEmpty()
                    ) {
                        otherTrainsHeader()
                    }

                    groupTrains(state.station.station, state.otherTrains).fastForEach { trains ->
                        item(trains.firstOrNull()?.id) {
                            TrainLineContentWithWithBackfillBottomSheet(
                                data = trains,
                                timeDisplay = state.timeDisplay,
                                station = state.station.station,
                                modifier = Modifier.heightIn(48.dp)
                                    .animateItemPlacement()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }
                } else {
                    state.trainsMatchingFilters.fastForEach { train ->
                        item(train.id) {
                            TrainLineContentWithWithBackfillBottomSheet(
                                data = listOf(train),
                                timeDisplay = state.timeDisplay,
                                station = state.station.station,
                                modifier = Modifier.heightIn(48.dp)
                                    .animateItemPlacement()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }

                    if (state.trainsMatchingFilters.isNotEmpty() &&
                        state.otherTrains.isNotEmpty()
                    ) {
                        otherTrainsHeader()
                    }

                    state.otherTrains.fastForEach { train ->
                        item(train.id) {
                            TrainLineContentWithWithBackfillBottomSheet(
                                data = listOf(train),
                                timeDisplay = state.timeDisplay,
                                station = state.station.station,
                                modifier = Modifier.heightIn(48.dp)
                                    .animateItemPlacement()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }
                }
            }

            if (stationData.alertText != null) {
                Spacer(Modifier.height(8.dp))
                AlertBox(
                    text = stationData.alertText,
                    url = stationData.alertUrl,
                    colors = if (stationData.alertIsWarning) {
                        AlertBoxColors.Warning
                    } else {
                        AlertBoxColors.Info
                    },
                    isAlwaysExpanded = true,
                )
            }
        }
    }
}

private fun LazyListScope.otherTrainsHeader() {
    item("other_train_header") {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(string.other_trains),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}