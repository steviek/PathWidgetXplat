package com.desaiwang.transit.path.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun RadioButtonWithText(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    textColor: Color = Color.Unspecified,
    subtext: String? = null,
    subtextStyle: TextStyle = LocalTextStyle.current,
    subtextColor: Color = Color.Unspecified,
    fillMaxWidth: Boolean = true
) {
    Row(
        Modifier.selectable(selected = selected, onClick = onClick)
            .heightIn(if (subtext == null) 48.dp else 72.dp).then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(8.dp))
        Column(
            modifier = Modifier.weight(1f, fill = fillMaxWidth),
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
