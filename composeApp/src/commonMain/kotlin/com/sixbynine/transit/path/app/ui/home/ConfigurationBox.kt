package com.sixbynine.transit.path.app.ui.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.MR.strings
import com.sixbynine.transit.path.app.ui.icon.IconPainter
import com.sixbynine.transit.path.app.ui.icon.IconType
import dev.icerock.moko.resources.compose.stringResource

@Composable
internal fun ConfigurationBox(
    onClick: (ConfigurationItem) -> Unit
) {
    FlowRow(
        Modifier
            .border(1.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            16.dp,
            alignment = Alignment.CenterHorizontally
        )
    ) {

        ConfigurationItem.entries.forEach {
            ConfigurationChip(it, onClick = onClick)
        }
    }
}

enum class ConfigurationItem {
    Station, Settings
}

@Composable
private fun ConfigurationChip(
    item: ConfigurationItem,
    onClick: (ConfigurationItem) -> Unit
) {
    val icon = when (item) {
        ConfigurationItem.Station -> IconType.Station
        ConfigurationItem.Settings -> IconType.Settings
    }
    val text = when (item) {
        ConfigurationItem.Station -> strings.stations
        ConfigurationItem.Settings -> strings.settings
    }
    TextButton(modifier = Modifier.widthIn(100.dp).heightIn(48.dp), onClick = { onClick(item) }) {
        Icon(painter = IconPainter(icon), contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text(text = stringResource(text))
    }
}