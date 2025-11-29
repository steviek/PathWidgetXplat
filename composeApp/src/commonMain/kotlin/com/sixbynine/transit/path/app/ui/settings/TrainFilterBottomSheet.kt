package com.sixbynine.transit.path.app.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.api.DepartureBoardTrainFilter
import com.sixbynine.transit.path.app.ui.PathBottomSheet
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.filter

@Composable
fun TrainFilterBottomSheet(
    isShown: Boolean,
    filter: DepartureBoardTrainFilter,
    onFilterClicked: (DepartureBoardTrainFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    PathBottomSheet(
        isShown = isShown,
        onDismissRequest = onDismiss,
        title = stringResource(string.filter)
    ) {
        RadioSection(modifier = Modifier.padding(bottom = 16.dp)) {
            DepartureBoardTrainFilter.entries.forEach {
                item(
                    text = stringResource(it.title),
                    subtext = it.subtext?.let { stringResource(it) },
                    selected = it == filter,
                    onClick = { onFilterClicked(it) }
                )
            }
        }
    }
}
