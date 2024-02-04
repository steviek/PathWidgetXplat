package com.sixbynine.transit.path.app.ui.settings

import androidx.compose.runtime.Composable
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
        RadioSection {
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
