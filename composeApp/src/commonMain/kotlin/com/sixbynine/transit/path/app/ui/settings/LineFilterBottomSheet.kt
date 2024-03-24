package com.sixbynine.transit.path.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.MR.strings
import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.app.ui.PathBottomSheet
import dev.icerock.moko.resources.compose.stringResource

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
        title = stringResource(strings.lines)
    ) {
        Column(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Line.entries.forEach { line ->
                TrainLineCheckboxRow(
                    line = line,
                    checked = line in lines,
                    onCheckedChange = { isChecked -> onLineCheckedChange(line, isChecked) },
                )
            }
        }
    }
}
