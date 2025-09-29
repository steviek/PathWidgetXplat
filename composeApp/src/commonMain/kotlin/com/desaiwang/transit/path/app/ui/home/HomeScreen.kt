package com.desaiwang.transit.path.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.desaiwang.transit.path.app.ui.AppUiScope
import com.desaiwang.transit.path.app.ui.ViewModelScreen
import com.desaiwang.transit.path.app.ui.gutter
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Effect.NavigateToSettings
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Effect.NavigateToStation
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Intent
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Intent.ConstraintsChanged
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Intent.EditClicked
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Intent.RetryClicked
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Intent.SettingsClicked
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Intent.StopEditingClicked
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.State
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.done
import pathwidgetxplat.composeapp.generated.resources.edit
import pathwidgetxplat.composeapp.generated.resources.failed_to_fetch
import pathwidgetxplat.composeapp.generated.resources.failed_to_fetch_path_fault
import pathwidgetxplat.composeapp.generated.resources.retry
import pathwidgetxplat.composeapp.generated.resources.settings

class HomeScreenScope(
    val state: State,
    val onIntent: (Intent) -> Unit,
) : AppUiScope {
    override val isTablet get() = state.isTablet
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

                    is NavigateToStation -> {
                        navigator.navigate("/station/${effect.stationId}")
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
                HorizontalDivider()
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

            state.hasError && state.data?.stations.isNullOrEmpty() -> {
                ErrorState(isPathApiError = state.isPathApiBusted)
            }

            else -> {
                Row(Modifier.padding(horizontal = gutter() - 12.dp).fillMaxWidth()) {
                    TextButton(
                        onClick = {
                            onIntent(SettingsClicked)
                        }
                    ) {
                        Text(stringResource(string.settings))
                    }

                    Spacer(Modifier.weight(1f))

                    TextButton(
                        onClick = {
                            onIntent(if (state.isEditing) StopEditingClicked else EditClicked)
                        }
                    ) {
                        Text(stringResource(if (state.isEditing) string.done else string.edit))
                    }
                }

                state.data?.globalAlerts?.forEach {
                    AlertBox(
                        text = it.text,
                        url = it.url,
                        colors = if (it.isWarning) AlertBoxColors.Warning else AlertBoxColors.Info,
                    )
                }

                DepartureBoard()
            }
        }
    }

    AddStationBottomSheet()
}

@Composable
private fun HomeScreenScope.ErrorState(isPathApiError: Boolean) {
    Column(
        modifier = Modifier.padding(gutter()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val text = if (isPathApiError) {
            string.failed_to_fetch_path_fault
        } else {
            string.failed_to_fetch
        }
        Text(
            text = stringResource(text),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onIntent(RetryClicked) }) {
            Text(
                text = stringResource(string.retry)
            )
        }
    }
}
