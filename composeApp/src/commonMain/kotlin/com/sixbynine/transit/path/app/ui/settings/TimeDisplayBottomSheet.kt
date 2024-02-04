package com.sixbynine.transit.path.app.ui.settings

import androidx.compose.runtime.Composable
import com.sixbynine.transit.path.MR.strings
import com.sixbynine.transit.path.app.settings.TimeDisplay
import com.sixbynine.transit.path.app.ui.PathBottomSheet
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun TimeDisplayBottomSheet(
    isShown: Boolean,
    display: TimeDisplay,
    onTimeDisplayClicked: (TimeDisplay) -> Unit,
    onDismiss: () -> Unit,
) {
    PathBottomSheet(
        isShown = isShown,
        onDismissRequest = onDismiss,
        title = stringResource(strings.setting_header_time_display)
    ) {
        RadioSection {
            TimeDisplay.entries.forEach {
                item(
                    text = stringResource(it.title),
                    subtext = it.subtitle(),
                    selected = it == display,
                    onClick = { onTimeDisplayClicked(it) }
                )
            }
        }
    }
}
