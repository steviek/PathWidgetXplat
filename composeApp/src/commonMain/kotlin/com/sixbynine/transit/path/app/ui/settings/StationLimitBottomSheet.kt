package com.sixbynine.transit.path.app.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.MR.strings
import com.sixbynine.transit.path.app.settings.StationLimit
import com.sixbynine.transit.path.app.ui.PathBottomSheet
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun StationLimitBottomSheet(
    isShown: Boolean,
    limit: StationLimit,
    onLimitClicked: (StationLimit) -> Unit,
    onDismiss: () -> Unit,
) {
    PathBottomSheet(
        isShown = isShown,
        onDismissRequest = onDismiss,
        title = stringResource(strings.settings_header_station_filter)
    ) {
        RadioSection(modifier = Modifier.padding(bottom = 16.dp)) {
            StationLimit.entries.forEach {
                item(
                    text = stringResource(it.displayName),
                    selected = it == limit,
                    onClick = { onLimitClicked(it) }
                )
            }
        }
    }
}
