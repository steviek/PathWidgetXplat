package com.sixbynine.transit.path.widget.setup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.MR.strings
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.app.ui.CheckboxWithText
import com.sixbynine.transit.path.app.ui.RadioButtonWithText
import com.sixbynine.transit.path.app.ui.settings.SettingsHeader
import com.sixbynine.transit.path.app.ui.settings.title
import com.sixbynine.transit.path.app.ui.theme.AppTheme
import com.sixbynine.transit.path.widget.setup.WidgetSetupScreenContract.Intent.ConfirmClicked
import com.sixbynine.transit.path.widget.setup.WidgetSetupScreenContract.Intent.SortOrderSelected
import com.sixbynine.transit.path.widget.setup.WidgetSetupScreenContract.Intent.StationToggled
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
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                SettingsHeader(text = stringResource(strings.stations))

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

                Row {
                    StationColumn(rows = state.njStations, modifier = Modifier.weight(1f))
                    StationColumn(rows = state.nyStations, modifier = Modifier.weight(1f))
                }

                Column(Modifier.selectableGroup()) {
                    SettingsHeader(text = stringResource(strings.station_order))

                    if (state.useClosestStation) {
                        Text(
                            text = stringResource(strings.closest_always_first),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    StationSort.entries.forEach { sortOrder ->
                        RadioButtonWithText(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = stringResource(sortOrder.title),
                            selected = sortOrder == state.sortOrder,
                            onClick = { onIntent(SortOrderSelected(sortOrder)) }
                        )
                    }
                }

                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.isConfirmButtonEnabled,
                        onClick = { onIntent(ConfirmClicked) }) {
                        Text(stringResource(strings.confirm))
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

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
