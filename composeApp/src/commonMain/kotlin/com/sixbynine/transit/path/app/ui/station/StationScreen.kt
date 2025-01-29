package com.sixbynine.transit.path.app.ui.station

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.sixbynine.transit.path.app.ui.ViewModelScreen
import com.sixbynine.transit.path.app.ui.icon.IconPainter
import com.sixbynine.transit.path.app.ui.icon.IconType.Back
import com.sixbynine.transit.path.app.ui.icon.IconType.Calendar
import com.sixbynine.transit.path.app.ui.icon.IconType.Clock
import com.sixbynine.transit.path.app.ui.icon.NativeIconButton
import com.sixbynine.transit.path.app.ui.station.StationContract.Effect.GoBack
import com.sixbynine.transit.path.app.ui.station.StationContract.Intent
import com.sixbynine.transit.path.app.ui.station.StationContract.Intent.BackClicked
import com.sixbynine.transit.path.app.ui.station.StationContract.State
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.back
import pathwidgetxplat.composeapp.generated.resources.live
import pathwidgetxplat.composeapp.generated.resources.schedule

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
        var selectedIndex by remember { mutableIntStateOf(0) }
        val pagerState = rememberPagerState(pageCount = { 2 })

        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collect {
                selectedIndex = it
            }
        }

        val scope = rememberCoroutineScope()
        Column(modifier = Modifier.padding(contentPadding)) {
            PrimaryTabRow(
                selectedTabIndex = selectedIndex,
            ) {
                Tab(
                    selected = selectedIndex == 0,
                    onClick = {
                        selectedIndex = 0
                        scope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    text = { Text(stringResource(string.live)) },
                    icon = { Icon(IconPainter(Clock), contentDescription = null) }
                )

                Tab(
                    selected = selectedIndex == 1,
                    onClick = {
                        selectedIndex = 1
                        scope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    text = { Text(stringResource(string.schedule)) },
                    icon = { Icon(IconPainter(Calendar), contentDescription = null) }
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { index ->
                when (index) {
                    0 -> LiveStationScreen()
                    1 -> ScheduleStationScreen()
                }
            }
        }
    }
}