package com.desaiwang.transit.path.app.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desaiwang.transit.path.app.settings.TimeDisplay
import com.desaiwang.transit.path.app.ui.PathBottomSheet
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.setting_header_time_display

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
        title = stringResource(string.setting_header_time_display)
    ) {
        RadioSection(modifier = Modifier.padding(bottom = 16.dp)) {
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
