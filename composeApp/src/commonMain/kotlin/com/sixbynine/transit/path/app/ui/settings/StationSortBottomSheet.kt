package com.sixbynine.transit.path.app.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.StationSort.Proximity
import com.sixbynine.transit.path.app.ui.PathBottomSheet
import com.sixbynine.transit.path.location.LocationProvider
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.station_order

@Composable
fun StationSortBottomSheet(
    isShown: Boolean,
    sort: StationSort,
    onSortClicked: (StationSort) -> Unit,
    onDismiss: () -> Unit,
) {
    PathBottomSheet(
        isShown = isShown,
        onDismissRequest = onDismiss,
        title = stringResource(string.station_order)
    ) {
        RadioSection(modifier = Modifier.padding(bottom = 16.dp)) {
            StationSort.entries.forEach {
                if (it == Proximity && !LocationProvider().isLocationSupportedByDevice) {
                    return@forEach
                }

                item(
                    text = stringResource(it.title),
                    subtext = it.subtitle?.let { stringResource(it) },
                    selected = it == sort,
                    onClick = { onSortClicked(it) }
                )
            }
        }
    }
}
