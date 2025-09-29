package com.desaiwang.transit.path.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desaiwang.transit.path.app.ui.SwitchWithText
import com.desaiwang.transit.path.app.ui.ViewModelScreen
import com.desaiwang.transit.path.app.ui.icon.IconType
import com.desaiwang.transit.path.app.ui.icon.NativeIconButton
import com.desaiwang.transit.path.app.ui.settings.SettingsContract.BottomSheetType
import com.desaiwang.transit.path.app.ui.settings.SettingsContract.Effect.GoBack
import com.desaiwang.transit.path.app.ui.settings.SettingsContract.Effect.GoToAdvancedSettings
import com.desaiwang.transit.path.app.ui.settings.SettingsContract.Intent.*
import com.desaiwang.transit.path.app.ui.settings.SettingsContract.LocationSettingState
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.*
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.advanced_settings
import pathwidgetxplat.composeapp.generated.resources.back
import pathwidgetxplat.composeapp.generated.resources.buy_me_a_coffee
import pathwidgetxplat.composeapp.generated.resources.closest_station_setting_subtitle
import pathwidgetxplat.composeapp.generated.resources.closest_station_setting_title
import pathwidgetxplat.composeapp.generated.resources.filter
import pathwidgetxplat.composeapp.generated.resources.lines
import pathwidgetxplat.composeapp.generated.resources.presumed_trains_subtext
import pathwidgetxplat.composeapp.generated.resources.rate_app
import pathwidgetxplat.composeapp.generated.resources.send_feedback
import pathwidgetxplat.composeapp.generated.resources.settings
import pathwidgetxplat.composeapp.generated.resources.share_app
import pathwidgetxplat.composeapp.generated.resources.show_presumed_trains
import pathwidgetxplat.composeapp.generated.resources.station_order

@Composable
fun SettingScreen() {
    ViewModelScreen(
        viewModelKey = "settings-screen",
        createViewModel = { SettingsViewModel() },
        onEffect = { effect ->
            when (effect) {
                is GoBack -> navigator.goBack()
                is GoToAdvancedSettings -> navigator.navigate("/advanced_settings")
            }
        }
    ) {
        Content()
    }
}

@Composable
fun SettingsScope.Content() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(string.settings),
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onIntent(HeaderTapped)
                        }
                    )
                },
                navigationIcon = {
                    NativeIconButton(
                        IconType.Back,
                        contentDescription = stringResource(string.back),
                        onClick = { onIntent(SettingsContract.Intent.BackClicked) })
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding).verticalScroll(rememberScrollState()),
        ) {
            if (state.locationSetting != LocationSettingState.NotAvailable) {
                LocationSettingSection()
            }

            FilterSection()

            LineFilterSection()

            StationOrderSection()

            SettingsItem(stringResource(string.advanced_settings)) {
                onIntent(
                    AdvancedSettingsClicked
                )
            }

            HorizontalDivider()

            SettingsItem(stringResource(string.rate_app)) { onIntent(RateAppClicked) }

            SettingsItem(stringResource(string.share_app)) { onIntent(ShareAppClicked) }

            SettingsItem(stringResource(string.send_feedback)) { onIntent(SendFeedbackClicked) }

            SettingsItem(stringResource(string.buy_me_a_coffee)) { onIntent(BuyMeACoffeeClicked) }

            if (state.devOptionsEnabled) {
                SettingsItem(stringResource(string.dev_options)) { onIntent(DevOptionsClicked) }
            }
        }

        StationSortBottomSheet(
            isShown = state.bottomSheet == BottomSheetType.StationSort,
            sort = state.stationSort,
            onDismiss = { onIntent(SettingsContract.Intent.BottomSheetDismissed) },
            onSortClicked = { onIntent(SettingsContract.Intent.StationSortSelected(it)) },
        )

        TrainFilterBottomSheet(
            isShown = state.bottomSheet == BottomSheetType.TrainFilter,
            filter = state.trainFilter,
            onDismiss = { onIntent(SettingsContract.Intent.BottomSheetDismissed) },
            onFilterClicked = { onIntent(SettingsContract.Intent.TrainFilterChanged(it)) },
        )

        LineFilterBottomSheet(
            isShown = state.bottomSheet == BottomSheetType.Lines,
            lines = state.lines,
            onDismiss = { onIntent(SettingsContract.Intent.BottomSheetDismissed) },
            onLineCheckedChange = { line, isChecked ->
                onIntent(SettingsContract.Intent.LineFilterToggled(line, isChecked))
            },
        )
    }
}

@Composable
private fun SettingsScope.FilterSection() {
    SettingsItem(
        title = stringResource(string.filter),
        subtitle = stringResource(state.trainFilter.title),
        onClick = { onIntent(SettingsContract.Intent.TrainFilterClicked) }
    )
}

@Composable
private fun SettingsScope.LineFilterSection() {
    SettingsItem(
        title = stringResource(string.lines),
        subtitle = state.lines.title,
        onClick = { onIntent(SettingsContract.Intent.LinesClicked) }
    )
}

@Composable
private fun SettingsScope.StationOrderSection() {
    SettingsItem(
        title = stringResource(string.station_order),
        subtitle = stringResource(state.stationSort.title),
        onClick = { onIntent(StationSortClicked) }
    )
}

@Composable
private fun SettingsScope.ShowPresumedTrainsSection() {
    SwitchWithText(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        text = stringResource(string.show_presumed_trains),
        subtext = stringResource(string.presumed_trains_subtext),
        checked = state.showPresumedTrains,
        onCheckedChange = { onIntent(ShowPresumedTrainsChanged(it)) },
        textStyle = MaterialTheme.typography.titleMedium,
        textColor = MaterialTheme.colorScheme.onSurface,
        subtextStyle = MaterialTheme.typography.bodyLarge,
        subtextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SettingsScope.LocationSettingSection() {
    SwitchWithText(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        text = stringResource(string.closest_station_setting_title),
        subtext = stringResource(string.closest_station_setting_subtitle)
            .takeUnless { state.hasLocationPermission },
        checked = state.locationSetting == LocationSettingState.Enabled,
        onCheckedChange = { onIntent(LocationSettingChanged(it)) },
        textStyle = MaterialTheme.typography.titleMedium,
        textColor = MaterialTheme.colorScheme.onSurface,
        subtextStyle = MaterialTheme.typography.bodyLarge,
        subtextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun SettingsItem(title: String, subtitle: String? = null, onClick: () -> Unit) {
    Column(
        Modifier.clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .heightIn(if (subtitle == null) 48.dp else 56.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
    ) {
        SettingsHeader(title)
        if (subtitle != null) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
