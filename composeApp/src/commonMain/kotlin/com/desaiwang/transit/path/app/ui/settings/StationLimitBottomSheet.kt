package com.desaiwang.transit.path.app.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desaiwang.transit.path.app.settings.StationLimit
import com.desaiwang.transit.path.app.ui.PathBottomSheet
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.settings_header_station_filter

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
        title = stringResource(string.settings_header_station_filter)
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
