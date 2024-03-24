package com.sixbynine.transit.path.widget.setup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.MR.strings
import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.TrainFilter
import com.sixbynine.transit.path.app.ui.CheckboxWithText
import com.sixbynine.transit.path.app.ui.settings.RadioSection
import com.sixbynine.transit.path.app.ui.settings.SettingsHeader
import com.sixbynine.transit.path.app.ui.settings.TrainLineCheckboxRow
import com.sixbynine.transit.path.app.ui.settings.subtext
import com.sixbynine.transit.path.app.ui.settings.subtitle
import com.sixbynine.transit.path.app.ui.settings.title
import com.sixbynine.transit.path.app.ui.theme.AppTheme
import com.sixbynine.transit.path.widget.setup.WidgetSetupScreenContract.Intent.ConfirmClicked
import com.sixbynine.transit.path.widget.setup.WidgetSetupScreenContract.Intent.LineToggled
import com.sixbynine.transit.path.widget.setup.WidgetSetupScreenContract.Intent.SortOrderSelected
import com.sixbynine.transit.path.widget.setup.WidgetSetupScreenContract.Intent.StationToggled
import com.sixbynine.transit.path.widget.setup.WidgetSetupScreenContract.Intent.TrainFilterSelected
import com.sixbynine.transit.path.widget.setup.WidgetSetupScreenContract.Intent.UseClosestStationToggled
import com.sixbynine.transit.path.widget.setup.WidgetSetupScreenContract.StationRow
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun WidgetSetupScreen(viewModel: WidgetSetupViewModel) {
    val state by viewModel.state.collectAsState()
    val scope = WidgetSetupScreenScope(state, viewModel::onIntent)
    scope.WidgetSetupScreenContent()
}

@Composable
fun WidgetSetupScreenScope.WidgetSetupScreenContent() {
    AppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(strings.widget_configuration)) }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { contentPadding ->
            Column(Modifier.padding(contentPadding)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsHeader(
                        text = stringResource(strings.stations),
                        style = TitleStyle
                    )

                    if (state.isClosestStationAvailable) {
                        CheckboxWithText(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            checked = state.useClosestStation,
                            text = stringResource(strings.closest_station),
                            onCheckedChange = { onIntent(UseClosestStationToggled(it)) }
                        )

                        Text(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .padding(start = 64.dp),
                            text = stringResource(strings.and_or),
                        )
                    }

                    Row {
                        StationColumn(rows = state.njStations, modifier = Modifier.weight(1f))
                        StationColumn(rows = state.nyStations, modifier = Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(24.dp))

                    RadioSection(
                        title = stringResource(strings.station_order),
                        titleStyle = TitleStyle
                    ) {
                        if (state.useClosestStation) {
                            Text(
                                text = stringResource(strings.closest_always_first),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        StationSort.entries.forEach { sortOrder ->
                            item(
                                text = stringResource(sortOrder.title),
                                subtext = sortOrder.subtitle?.let { stringResource(it) },
                                selected = sortOrder == state.sortOrder,
                                onClick = { onIntent(SortOrderSelected(sortOrder)) }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    RadioSection(
                        title = stringResource(strings.filter),
                        titleStyle = TitleStyle
                    ) {
                        TrainFilter.entries.forEach { filter ->
                            item(
                                text = stringResource(filter.title),
                                subtext = filter.subtext?.let { stringResource(it) },
                                selected = filter == state.filter,
                                onClick = { onIntent(TrainFilterSelected(filter)) }
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    LinesSection()
                }

                HorizontalDivider()

                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp),
                        enabled = state.isConfirmButtonEnabled,
                        onClick = { onIntent(ConfirmClicked) }
                    ) {
                        Text(stringResource(strings.confirm))
                    }
                }
            }
        }
    }


}

private val TitleStyle @Composable get() = MaterialTheme.typography.headlineSmall

@Composable
private fun WidgetSetupScreenScope.StationColumn(
    rows: List<StationRow>,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        rows.forEach { station ->
            CheckboxWithText(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = station.displayName,
                checked = station.checked,
                onCheckedChange = { checked ->
                    onIntent(StationToggled(station.id, checked))
                }
            )
        }
    }
}

@Composable
private fun WidgetSetupScreenScope.LinesSection() {
    Column {
        SettingsHeader(
            text = stringResource(strings.lines),
            style = TitleStyle
        )

        Line.entries.forEach { line ->
            TrainLineCheckboxRow(
                line = line,
                checked = line in state.lines,
                onCheckedChange = { isChecked -> onIntent(LineToggled(line, isChecked)) },
            )
        }
    }
}
