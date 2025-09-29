package com.desaiwang.transit.path.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.desaiwang.transit.path.app.external.ExternalRoutingManager
import com.desaiwang.transit.path.app.settings.AvoidMissingTrains
import com.desaiwang.transit.path.app.ui.PathBottomSheet
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.avoid_missing_trains
import pathwidgetxplat.composeapp.generated.resources.avoid_missing_trains_description
import pathwidgetxplat.composeapp.generated.resources.avoid_missing_trains_description_2
import pathwidgetxplat.composeapp.generated.resources.avoid_missing_trains_description_3
import pathwidgetxplat.composeapp.generated.resources.learn_more

@Composable
fun AvoidMissingTrainsBottomSheet(
    isShown: Boolean,
    option: AvoidMissingTrains,
    onOptionClicked: (AvoidMissingTrains) -> Unit,
    onDismiss: () -> Unit,
) {
    PathBottomSheet(
        isShown = isShown,
        onDismissRequest = onDismiss,
        title = stringResource(string.avoid_missing_trains)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(string.avoid_missing_trains_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Column(Modifier.padding(horizontal = 16.dp).padding(top = 8.dp).fillMaxWidth()) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(string.avoid_missing_trains_description_2),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontStyle = FontStyle.Italic
            )

            val scope = rememberCoroutineScope()
            TextButton(
                onClick = {
                    scope.launch {
                        ExternalRoutingManager()
                            .openUrl("https://www.panynj.gov/path/en/schedules-maps.html")
                    }
                },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(text = stringResource(string.learn_more))
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(string.avoid_missing_trains_description_3),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        RadioSection(modifier = Modifier.padding(bottom = 16.dp)) {
            AvoidMissingTrains.entries.forEach {
                item(
                    text = it.displayName,
                    subtext = it.subtitle?.let { stringResource(it) },
                    selected = it == option,
                    onClick = { onOptionClicked(it) }
                )
            }
        }
    }
}
