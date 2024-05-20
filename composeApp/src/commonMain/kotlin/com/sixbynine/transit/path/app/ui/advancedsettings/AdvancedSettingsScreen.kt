package com.sixbynine.transit.path.app.ui.advancedsettings

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
import com.sixbynine.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.BottomSheetType.AvoidMissingTrains
import com.sixbynine.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Effect.GoBack
import com.sixbynine.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.AvoidMissingTrainsChanged
import com.sixbynine.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.AvoidMissingTrainsClicked
import com.sixbynine.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.BackClicked
import com.sixbynine.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.BottomSheetDismissed
import com.sixbynine.transit.path.app.ui.icon.IconType.Back
import com.sixbynine.transit.path.app.ui.icon.NativeIconButton
import com.sixbynine.transit.path.app.ui.settings.AvoidMissingTrainsBottomSheet
import com.sixbynine.transit.path.app.ui.settings.SettingsItem
import com.sixbynine.transit.path.app.ui.settings.displayName
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.advanced_settings
import pathwidgetxplat.composeapp.generated.resources.avoid_missing_trains
import pathwidgetxplat.composeapp.generated.resources.back

@Composable
fun AdvancedSettingsScreen() {
    ViewModelScreen(
        viewModelKey = "advanced_settings",
        createViewModel = { AdvancedSettingsViewModel() },
        onEffect = { effect ->
            when (effect) {
                is GoBack -> navigator.goBack()
            }
        },
        content = { Content() }
    )
}

@Composable
private fun AdvancedSettingsScope.Content() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(string.advanced_settings)) },
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
            AvoidMissingTrainsSection()
        }

        AvoidMissingTrainsBottomSheet(
            isShown = state.bottomSheet == AvoidMissingTrains,
            option = state.avoidMissingTrains,
            onDismiss = { onIntent(BottomSheetDismissed) },
            onOptionClicked = { onIntent(AvoidMissingTrainsChanged(it)) },
        )
    }
}

@Composable
private fun AdvancedSettingsScope.AvoidMissingTrainsSection() {
    SettingsItem(
        title = stringResource(string.avoid_missing_trains),
        subtitle = state.avoidMissingTrains.displayName,
        onClick = { onIntent(AvoidMissingTrainsClicked) }
    )
}