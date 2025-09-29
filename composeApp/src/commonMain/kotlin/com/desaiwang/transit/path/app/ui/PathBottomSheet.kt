package com.desaiwang.transit.path.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desaiwang.transit.path.app.ui.TransitionState.Hidden
import com.desaiwang.transit.path.app.ui.TransitionState.Hiding
import com.desaiwang.transit.path.app.ui.TransitionState.Shown

@Composable
fun PathBottomSheet(
    isShown: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    PathBottomSheet(
        isShown = isShown,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        content = content
    )
}

@Composable
fun PathBottomSheet(
    isShown: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var transitionState by remember { mutableStateOf(if (isShown) Shown else Hidden) }

    // Handle keeping the bottom sheet composed while the hide animation is going on. If we show
    // again while hiding, only then do we explicitly call show. Otherwise, the sheet shows
    // on its own when it gets composed.
    LaunchedEffect(isShown) {
        if (isShown) {
            if (transitionState == Hiding) {
                sheetState.show()
            }
            transitionState = Shown
        } else {
            transitionState = Hiding
            sheetState.hide()
            transitionState = Hidden
        }
    }

    if (transitionState != Hidden) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            windowInsets = BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Top)
        ) {
            Column(
                modifier,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                title()

                Column {
                    content()
                }
            }
        }
    }
}

private enum class TransitionState { Hidden, Shown, Hiding }
