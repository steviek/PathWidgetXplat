package com.sixbynine.transit.path.app.ui.station

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sixbynine.transit.path.app.ui.ViewModelScreen
import com.sixbynine.transit.path.app.ui.icon.IconType.Back
import com.sixbynine.transit.path.app.ui.icon.NativeIconButton
import com.sixbynine.transit.path.app.ui.station.StationContract.Effect.GoBack
import com.sixbynine.transit.path.app.ui.station.StationContract.Intent
import com.sixbynine.transit.path.app.ui.station.StationContract.Intent.BackClicked
import com.sixbynine.transit.path.app.ui.station.StationContract.State
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.back

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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(state.station?.displayName.orEmpty()) },
                navigationIcon = {
                    NativeIconButton(
                        Back,
                        contentDescription = stringResource(string.back),
                        onClick = { onIntent(BackClicked) })
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding).verticalScroll(rememberScrollState()),
        ) {

        }
    }
}