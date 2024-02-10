package com.sixbynine.transit.path.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.MR.strings
import com.sixbynine.transit.path.app.ui.SwitchWithText
import com.sixbynine.transit.path.app.ui.ViewModelScreen
import com.sixbynine.transit.path.app.ui.icon.IconType
import com.sixbynine.transit.path.app.ui.icon.NativeIconButton
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.BottomSheetType
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Effect.GoBack
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.RateAppClicked
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.SendFeedbackClicked
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.ShareAppClicked
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.ShowPresumedTrainsChanged
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.StationLimitClicked
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.StationSortClicked
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.TimeDisplayChanged
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.TimeDisplayClicked
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun SettingScreen() {
    ViewModelScreen(
        viewModelKey = "settings-screen",
        createViewModel = { SettingsViewModel() },
        onEffect = { effect ->
            when (effect) {
                is GoBack -> navigator.goBack()
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
                title = { Text(stringResource(strings.settings)) },
                navigationIcon = {
                    NativeIconButton(
                        IconType.Back,
                        contentDescription = stringResource(strings.back),
                        onClick = { onIntent(SettingsContract.Intent.BackClicked) })
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding).verticalScroll(rememberScrollState()),
        ) {
            TimeDisplaySection()

            FilterSection()

            StationOrderSection()

            StationLimitSection()

            Divider()

            SettingsItem(stringResource(strings.send_feedback)) { onIntent(SendFeedbackClicked) }

            SettingsItem(stringResource(strings.rate_app)) { onIntent(RateAppClicked) }

            SettingsItem(stringResource(strings.share_app)) { onIntent(ShareAppClicked) }
        }

        StationLimitBottomSheet(
            isShown = state.bottomSheet == BottomSheetType.StationLimit,
            limit = state.stationLimit,
            onDismiss = { onIntent(SettingsContract.Intent.BottomSheetDismissed) },
            onLimitClicked = { onIntent(SettingsContract.Intent.StationLimitSelected(it)) },
        )

        StationSortBottomSheet(
            isShown = state.bottomSheet == BottomSheetType.StationSort,
            sort = state.stationSort,
            onDismiss = { onIntent(SettingsContract.Intent.BottomSheetDismissed) },
            onSortClicked = { onIntent(SettingsContract.Intent.StationSortSelected(it)) },
        )

        TimeDisplayBottomSheet(
            isShown = state.bottomSheet == BottomSheetType.TimeDisplay,
            display = state.timeDisplay,
            onDismiss = { onIntent(SettingsContract.Intent.BottomSheetDismissed) },
            onTimeDisplayClicked = { onIntent(TimeDisplayChanged(it)) },
        )

        TrainFilterBottomSheet(
            isShown = state.bottomSheet == BottomSheetType.TrainFilter,
            filter = state.trainFilter,
            onDismiss = { onIntent(SettingsContract.Intent.BottomSheetDismissed) },
            onFilterClicked = { onIntent(SettingsContract.Intent.TrainFilterChanged(it)) },
        )
    }
}

@Composable
private fun SettingsScope.FilterSection() {
    SettingsItem(
        title = stringResource(strings.filter),
        subtitle = stringResource(state.trainFilter.title),
        onClick = { onIntent(SettingsContract.Intent.TrainFilterClicked) }
    )
}

@Composable
private fun SettingsScope.TimeDisplaySection() {
    SettingsItem(
        title = stringResource(strings.setting_header_time_display),
        subtitle = stringResource(state.timeDisplay.title),
        onClick = { onIntent(TimeDisplayClicked) }
    )
}

@Composable
private fun SettingsScope.StationOrderSection() {
    SettingsItem(
        title = stringResource(strings.station_order),
        subtitle = stringResource(state.stationSort.title),
        onClick = { onIntent(StationSortClicked) }
    )
}

@Composable
private fun SettingsScope.StationLimitSection() {
    SettingsItem(
        title = stringResource(strings.settings_header_station_filter),
        subtitle = stringResource(state.stationLimit.displayName),
        onClick = { onIntent(StationLimitClicked) }
    )
}

@Composable
private fun SettingsScope.ShowPresumedTrainsSection() {
    SwitchWithText(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        text = stringResource(strings.show_presumed_trains),
        subtext = stringResource(strings.presumed_trains_subtext),
        checked = state.showPresumedTrains,
        onCheckedChange = { onIntent(ShowPresumedTrainsChanged(it)) },
        textStyle = MaterialTheme.typography.titleMedium,
        textColor = MaterialTheme.colorScheme.onSurface,
        subtextStyle = MaterialTheme.typography.bodyLarge,
        subtextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SettingsItem(title: String, subtitle: String? = null, onClick: () -> Unit) {
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
