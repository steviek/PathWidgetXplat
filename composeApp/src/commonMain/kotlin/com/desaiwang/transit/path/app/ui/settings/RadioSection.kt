package com.desaiwang.transit.path.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.desaiwang.transit.path.app.ui.RadioButtonWithText
import com.desaiwang.transit.path.util.conditional

@Composable
fun RadioSection(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    content: @Composable RadioSectionScope.() -> Unit
) {
    Column(modifier.selectableGroup()) {
        title?.let { SettingsHeader(it, style = titleStyle) }
        val scope = RadioSectionScope(this)
        scope.content()
    }
}

class RadioSectionScope(column: ColumnScope) : ColumnScope by column {
    @Composable
    fun item(
        text: String,
        selected: Boolean,
        subtext: String? = null,
        onClick: () -> Unit,
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
fun SettingsHeader(text: String, style: TextStyle = MaterialTheme.typography.titleMedium) {
    Text(
        text = text,
        style = style,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 16.dp).semantics { heading() }
    )
}

@Composable
fun HorizontalRadioSection(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(16.dp),
    maxWidth: Boolean = true,
    content: @Composable HorizontalRadioSectionScope.() -> Unit
) {
    Column(modifier.selectableGroup()) {
        title?.let { SettingsHeader(it, style = titleStyle) }
        Row(
            modifier = Modifier.conditional(maxWidth) { fillMaxWidth() },
            horizontalArrangement = horizontalArrangement
        ) {
            val scope = HorizontalRadioSectionScope(this)
            scope.content()
        }
    }
}

class HorizontalRadioSectionScope(row: RowScope) : RowScope by row {
    @Composable
    fun item(
        text: String,
        selected: Boolean,
        modifier: Modifier = Modifier,
        subtext: String? = null,
        onClick: () -> Unit,
    ) {
        RadioButtonWithText(
            text = text,
            subtext = subtext,
            selected = selected,
            onClick = onClick,
            modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            textColor = MaterialTheme.colorScheme.onSurface,
            subtextStyle = MaterialTheme.typography.bodyMedium,
            subtextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            fillMaxWidth = false
        )
    }
}