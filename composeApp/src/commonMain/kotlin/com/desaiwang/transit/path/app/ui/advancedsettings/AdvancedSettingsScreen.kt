package com.desaiwang.transit.path.app.ui.advancedsettings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.desaiwang.transit.path.app.settings.CommutingConfiguration
import com.desaiwang.transit.path.app.ui.ViewModelScreen
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.BottomSheetType.AvoidMissingTrains
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.BottomSheetType.CommutingSchedule
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.BottomSheetType.StationLimit
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.BottomSheetType.TimeDisplay
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Effect.GoBack
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.AvoidMissingTrainsChanged
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.AvoidMissingTrainsClicked
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.BackClicked
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.BottomSheetDismissed
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.CommutingConfigurationChanged
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.CommutingScheduleClicked
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.StationLimitClicked
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.StationLimitSelected
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.TimeDisplayChanged
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.TimeDisplayClicked
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.TrainGroupingClicked
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.State
import com.desaiwang.transit.path.app.ui.icon.IconType.Back
import com.desaiwang.transit.path.app.ui.icon.NativeIconButton
import com.desaiwang.transit.path.app.ui.settings.AvoidMissingTrainsBottomSheet
import com.desaiwang.transit.path.app.ui.settings.SettingsItem
import com.desaiwang.transit.path.app.ui.settings.StationLimitBottomSheet
import com.desaiwang.transit.path.app.ui.settings.TimeDisplayBottomSheet
import com.desaiwang.transit.path.app.ui.settings.displayName
import com.desaiwang.transit.path.app.ui.settings.title
import com.desaiwang.transit.path.time.UserPreferenceDayOfWeekComparator
import com.desaiwang.transit.path.time.plusDays
import com.desaiwang.transit.path.widget.WidgetDataFormatter.displayLabel
import com.desaiwang.transit.path.widget.WidgetDataFormatter.formatted
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.advanced_settings
import pathwidgetxplat.composeapp.generated.resources.avoid_missing_trains
import pathwidgetxplat.composeapp.generated.resources.back
import pathwidgetxplat.composeapp.generated.resources.commuting_schedule
import pathwidgetxplat.composeapp.generated.resources.setting_header_time_display
import pathwidgetxplat.composeapp.generated.resources.setting_header_train_grouping
import pathwidgetxplat.composeapp.generated.resources.settings_header_station_filter
import pathwidgetxplat.composeapp.generated.resources.train_grouping_off
import pathwidgetxplat.composeapp.generated.resources.train_grouping_on

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
fun AdvancedSettingsScreen(state: State, onIntent: (Intent) -> Unit) {
    val scope = AdvancedSettingsScope(state, onIntent)
    scope.Content()
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
            TimeDisplaySection()

            TrainGroupingSection()

            AvoidMissingTrainsSection()

            StationLimitSection()

            CommutingScheduleSection()
        }

        AvoidMissingTrainsBottomSheet(
            isShown = state.bottomSheet == AvoidMissingTrains,
            option = state.avoidMissingTrains,
            onDismiss = { onIntent(BottomSheetDismissed) },
            onOptionClicked = { onIntent(AvoidMissingTrainsChanged(it)) },
        )

        StationLimitBottomSheet(
            isShown = state.bottomSheet == StationLimit,
            limit = state.stationLimit,
            onDismiss = { onIntent(BottomSheetDismissed) },
            onLimitClicked = { onIntent(StationLimitSelected(it)) },
        )

        TimeDisplayBottomSheet(
            isShown = state.bottomSheet == TimeDisplay,
            display = state.timeDisplay,
            onDismiss = { onIntent(BottomSheetDismissed) },
            onTimeDisplayClicked = { onIntent(TimeDisplayChanged(it)) },
        )

        CommutingScheduleBottomSheet(
            isShown = state.bottomSheet == CommutingSchedule,
            configuration = state.commutingConfiguration,
            onDismiss = { onIntent(BottomSheetDismissed) },
            onChanged = { onIntent(CommutingConfigurationChanged(it)) },
        )
    }
}

@Composable
private fun AdvancedSettingsScope.TimeDisplaySection() {
    SettingsItem(
        title = stringResource(string.setting_header_time_display),
        subtitle = stringResource(state.timeDisplay.title),
        onClick = { onIntent(TimeDisplayClicked) }
    )
}

@Composable
private fun AdvancedSettingsScope.TrainGroupingSection() {
    SettingsItem(
        title = stringResource(string.setting_header_train_grouping),
        subtitle = stringResource(
            if (state.groupTrains) string.train_grouping_on else string.train_grouping_off
        ),
        onClick = { onIntent(TrainGroupingClicked(state.groupTrains)) }
    )
}

@Composable
private fun AdvancedSettingsScope.AvoidMissingTrainsSection() {
    SettingsItem(
        title = stringResource(string.avoid_missing_trains),
        subtitle = state.avoidMissingTrains.displayName,
        onClick = { onIntent(AvoidMissingTrainsClicked) }
    )
}

@Composable
private fun AdvancedSettingsScope.StationLimitSection() {
    SettingsItem(
        title = stringResource(string.settings_header_station_filter),
        subtitle = stringResource(state.stationLimit.displayName),
        onClick = { onIntent(StationLimitClicked) }
    )
}

@Composable
private fun AdvancedSettingsScope.CommutingScheduleSection() {
    SettingsItem(
        title = stringResource(string.commuting_schedule),
        subtitle = state.commutingConfiguration.displayText(),
        onClick = { onIntent(CommutingScheduleClicked) }
    )
}

@Composable
private fun CommutingConfiguration.displayText(): String {
    val schedule = schedules.firstOrNull() ?: return ""
    return remember(schedule) {
        val daysDisplay = run {
            val sortedDays = schedule.days.sortedWith(UserPreferenceDayOfWeekComparator())
            if (sortedDays.isEmpty() || sortedDays.size == 7) return@run null
            var isContiguous = true
            sortedDays.forEachIndexed { index, day ->
                if (index == 0) return@forEachIndexed
                if (day != sortedDays[index - 1].plusDays(1)) {
                    isContiguous = false
                }
            }
            if (isContiguous) {
                sortedDays.first().displayLabel() + " - " + sortedDays.last().displayLabel()
            } else {
                sortedDays.joinToString { it.displayLabel() }
            }

        }

        val timeDisplay =
            schedule.start.formatted(includePeriodMarker = false) +
                    " - " +
                    schedule.end.formatted(includePeriodMarker = false)

        listOfNotNull(daysDisplay, timeDisplay).joinToString()
    }
}