package com.sixbynine.transit.path.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.app.ui.RadioButtonWithText

@Composable
fun RadioSection(title: String? = null, content: @Composable RadioSectionScope.() -> Unit) {
    Column(Modifier.selectableGroup()) {
        title?.let { SettingsHeader(it) }
        val scope = RadioSectionScope(this)
        scope.content()
    }
}

class RadioSectionScope(column: ColumnScope) : ColumnScope by column {
    @Composable
    fun item(
        text: String,
        selected: Boolean,
        onClick: () -> Unit,
        subtext: String? = null,
    ) {
        RadioButtonWithText(
            text = text,
            subtext = subtext,
            selected = selected,
            onClick = onClick,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            textColor = MaterialTheme.colorScheme.onSurface,
            subtextStyle = MaterialTheme.typography.bodyMedium,
            subtextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun SettingsHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 16.dp).semantics { heading() }
    )
}