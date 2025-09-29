package com.desaiwang.transit.path.app.ui.filter

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desaiwang.transit.path.api.TrainFilter
import com.desaiwang.transit.path.api.TrainFilter.All
import com.desaiwang.transit.path.api.TrainFilter.Interstate
import com.desaiwang.transit.path.app.settings.SettingsManager
import com.desaiwang.transit.path.app.ui.AppUiScope
import com.desaiwang.transit.path.app.ui.PathBottomSheet
import com.desaiwang.transit.path.app.ui.gutter
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.show_all_trains
import pathwidgetxplat.composeapp.generated.resources.show_interstate_trains

@Composable
fun AppUiScope.FilterBottomSheet(onDismiss: () -> Unit) {
    var filter by remember {
        mutableStateOf(SettingsManager.trainFilter.value)
    }
    FilterBottomSheet(
        filter = filter,
        onFilterClick = { filter = it },
        onDismiss = {
            SettingsManager.updateTrainFilter(filter)
            onDismiss()
        }
    )
}

@Composable
fun AppUiScope.FilterBottomSheet(
    filter: TrainFilter,
    onFilterClick: (TrainFilter) -> Unit,
    onDismiss: () -> Unit
) {
    PathBottomSheet(
        isShown = true,
        onDismissRequest = onDismiss,
    ) {
        TrainFilter.entries.forEach {
            FilterRow(it, selected = it == filter, onClick = { onFilterClick(it) })
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun AppUiScope.FilterRow(
    filter: TrainFilter,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.heightIn(min = 48.dp)
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick)
            .padding(vertical = 8.dp, horizontal = gutter()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Spacer(Modifier.width(8.dp))

        val text = when (filter) {
            All -> stringResource(string.show_all_trains)
            Interstate -> stringResource(string.show_interstate_trains)
        }

        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}