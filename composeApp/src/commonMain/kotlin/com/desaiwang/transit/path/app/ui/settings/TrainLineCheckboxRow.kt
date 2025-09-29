package com.desaiwang.transit.path.app.ui.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desaiwang.transit.path.api.Line
import com.desaiwang.transit.path.app.ui.CheckboxWithContent
import com.desaiwang.transit.path.app.ui.ColorCircle
import com.desaiwang.transit.path.app.ui.RowPosition.Start

@Composable
fun TrainLineCheckboxRow(
    line: Line,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    CheckboxWithContent(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        checked = checked,
        onCheckedChange = onCheckedChange,
        checkboxPosition = Start,
        content = {
            Row(
                Modifier.heightIn(48.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = line.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.width(16.dp))

                ColorCircle(line.colors)
            }
        }
    )
}
