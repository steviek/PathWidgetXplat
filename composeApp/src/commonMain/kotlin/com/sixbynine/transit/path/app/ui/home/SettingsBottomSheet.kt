package com.sixbynine.transit.path.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.MR.strings
import com.sixbynine.transit.path.api.StationFilter
import com.sixbynine.transit.path.api.StationFilter.All
import com.sixbynine.transit.path.api.StationFilter.Interstate
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.StationSort.Alphabetical
import com.sixbynine.transit.path.api.StationSort.NjAm
import com.sixbynine.transit.path.api.StationSort.NyAm
import com.sixbynine.transit.path.app.ui.PathBottomSheet
import com.sixbynine.transit.path.app.ui.RadioButtonWithText
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.SettingsBottomSheetDismissed
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.SettingsFilterChanged
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.SettingsSortChanged
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.SettingsTimeDisplayChanged
import com.sixbynine.transit.path.widget.WidgetDataFormatter
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.minutes

@Composable
fun HomeScreenScope.SettingsBottomSheet() {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    PathBottomSheet(
        onDismissRequest = { onIntent(SettingsBottomSheetDismissed) },
        sheetState = sheetState
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TimeDisplaySection()

            FilterSection()

            StationOrderSection()
        }
    }
}

@Composable
private fun HomeScreenScope.FilterSection() {
    RadioSection(title = stringResource(strings.filter)) {
        StationFilter.entries.forEach { filter ->
            val text = when (filter) {
                All -> stringResource(strings.show_all_trains)
                Interstate -> stringResource(strings.show_interstate_trains)
            }
            val subtext = when (filter) {
                All -> null
                Interstate -> stringResource(strings.interstate_explanation)
            }
            item(
                text = text,
                subtext = subtext,
                selected = filter == state.stationFilter,
                onClick = {
                    onIntent(SettingsFilterChanged(filter))
                }
            )
        }

    }
}

@Composable
private fun HomeScreenScope.TimeDisplaySection() {
    RadioSection(title = stringResource(strings.setting_header_time_display)) {
        TimeDisplay.entries.forEach {
            val text = when (it) {
                TimeDisplay.Relative -> stringResource(strings.setting_time_display_relative)
                TimeDisplay.Clock -> stringResource(
                    strings.setting_time_display_clock,
                    getClockDisplayTimeLabel()
                )
            }

            item(
                text = text,
                selected = it == state.timeDisplay,
                onClick = {
                    onIntent(SettingsTimeDisplayChanged(it))
                }
            )
        }
    }
}

@Composable
private fun HomeScreenScope.StationOrderSection() {
    RadioSection(title = stringResource(strings.station_order)) {
        StationSort.entries.forEach { sort ->
            val text = when (sort) {
                Alphabetical -> strings.station_order_fixed
                NjAm -> strings.station_order_nj_am
                NyAm -> strings.station_order_ny_am
            }
            item(
                text = stringResource(text),
                selected = sort == state.stationSort,
                onClick = {
                    onIntent(SettingsSortChanged(sort))
                }
            )
        }

    }
}

@Composable
private fun getClockDisplayTimeLabel(): String {
    return remember {
        WidgetDataFormatter.formatTime(Clock.System.now() + 5.minutes)
    }
}

@Composable
private fun RadioSection(title: String, content: @Composable RadioSectionScope.() -> Unit) {
    Column(Modifier.selectableGroup()) {
        SettingsHeader(title)
        val scope = RadioSectionScope(this)
        scope.content()
    }
}

class RadioSectionScope(column: ColumnScope) : ColumnScope by column {
    @Composable
    fun item(
        text: String,
        selected: Boolean,
        onClick: () -> Unit,
        subtext: String? = null,
    ) {
        RadioButtonWithText(
            text = text,
            subtext = subtext,
            selected = selected,
            onClick = onClick,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            textColor = MaterialTheme.colorScheme.onSurface,
            subtextStyle = MaterialTheme.typography.bodyMedium,
            subtextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 16.dp).semantics { heading() }
    )
}
