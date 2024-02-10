package com.sixbynine.transit.path.app.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        RadioSection(modifier = Modifier.padding(bottom = 16.dp)) {
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
