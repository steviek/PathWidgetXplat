package com.desaiwang.transit.path.app.ui.setup

import LocalNavigator
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desaiwang.transit.path.api.Station
import com.desaiwang.transit.path.api.Stations
import com.desaiwang.transit.path.app.ui.HandleEffects
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.PopUpTo
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.choose_stations
import pathwidgetxplat.composeapp.generated.resources.confirm

@Composable
fun SetupScreen() {
    val viewModel = getViewModel(
        key = "setup-screen",
        factory = viewModelFactory { SetupScreenViewModel() }
    )
    val state by viewModel.state.collectAsState()
    SetupScreen(state, viewModel::onIntent)

    val navigator = LocalNavigator.current
    HandleEffects(viewModel.effects) { effect ->
        when (effect) {
            is SetupScreenContract.Effect.NavigateToHome -> {
                navigator.navigate("/home", NavOptions(popUpTo = PopUpTo("", true)))
            }
        }
    }
}

@Composable
fun SetupScreen(state: SetupScreenContract.State, onIntent: (SetupScreenContract.Intent) -> Unit) {
    Scaffold { contentPadding ->
        Column(
            Modifier.padding(contentPadding).fillMaxSize().verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(string.choose_stations),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Stations.All.forEach { station ->
                StationRow(
                    station,
                    isChecked = station in state.selectedStations,
                    onCheckedChange = {
                        onIntent(SetupScreenContract.Intent.StationCheckedChanged(station, it))
                    }
                )
            }

            Button(onClick = { onIntent(SetupScreenContract.Intent.ConfirmClicked) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                enabled = state.selectedStations.isNotEmpty()
            ) {
                Text(stringResource(string.confirm))
            }
        }
    }
}

@Composable
private fun StationRow(station: Station, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.heightIn(min = 48.dp)
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .toggleable(value = isChecked, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = null,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            station.displayName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}