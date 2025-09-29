package com.desaiwang.transit.path.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desaiwang.transit.path.api.Line
import com.desaiwang.transit.path.app.ui.PathBottomSheet
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.lines

@Composable
fun LineFilterBottomSheet(
    isShown: Boolean,
    lines: Set<Line>,
    onLineCheckedChange: (Line, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    PathBottomSheet(
        isShown = isShown,
        onDismissRequest = onDismiss,
        title = stringResource(string.lines)
    ) {
        Column(Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Line.permanentLines.forEach { line ->
                TrainLineCheckboxRow(
                    line = line,
                    checked = line in lines,
                    onCheckedChange = { isChecked -> onLineCheckedChange(line, isChecked) },
                )
            }
        }
    }
}
