package com.desaiwang.transit.path.app.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desaiwang.transit.path.api.StationSort
import com.desaiwang.transit.path.api.StationSort.Proximity
import com.desaiwang.transit.path.app.ui.PathBottomSheet
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
                if (it == Proximity && !StationSort.isProximityEnabled()) {
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
