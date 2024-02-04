package com.sixbynine.transit.path.app.ui.settings

import androidx.compose.runtime.Composable
import com.sixbynine.transit.path.MR.strings
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.app.ui.PathBottomSheet
import dev.icerock.moko.resources.compose.stringResource

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
        title = stringResource(strings.station_order)
    ) {
        RadioSection {
            StationSort.entries.forEach {
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
