package com.sixbynine.transit.path.app.ui.advancedsettings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.app.settings.AvoidMissingTrains
import com.sixbynine.transit.path.app.ui.PathBottomSheet
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.avoid_missing_trains_description
import pathwidgetxplat.composeapp.generated.resources.commuting_schedule

@Composable
fun CommutingScheduleBottomSheet(
    isShown: Boolean,
    option: AvoidMissingTrains,
    onOptionClicked: (AvoidMissingTrains) -> Unit,
    onDismiss: () -> Unit,
) {
    PathBottomSheet(
        isShown = isShown,
        onDismissRequest = onDismiss,
        title = stringResource(string.commuting_schedule)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(string.avoid_missing_trains_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

    }
}
