package com.desaiwang.transit.path.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun CheckboxWithContent(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    checkboxPosition: RowPosition = RowPosition.Start,
    content: @Composable () -> Unit
) {
    Row(
        Modifier.triStateToggleable(
            state = ToggleableState(checked),
            role = Role.Checkbox,
            onClick = { onCheckedChange(!checked) }
        )
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (checkboxPosition == RowPosition.Start) {
            Checkbox(checked = checked, onCheckedChange = null)
        }

        Box(Modifier.weight(1f)) {
            content()
        }

        if (checkboxPosition == RowPosition.End) {
            Checkbox(checked = checked, onCheckedChange = null)
        }
    }
}

@Composable
fun CheckboxWithText(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    textColor: Color = Color.Unspecified,
    subtext: String? = null,
    subtextStyle: TextStyle = LocalTextStyle.current,
    subtextColor: Color = Color.Unspecified,
) {
    CheckboxWithContent(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.heightIn(if (subtext == null) 48.dp else 72.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = text, style = textStyle, color = textColor)

            if (subtext != null) {
                Text(
                    text = subtext,
                    style = subtextStyle,
                    color = subtextColor
                )
            }
        }
    }
}

enum class RowPosition { Start, End }
