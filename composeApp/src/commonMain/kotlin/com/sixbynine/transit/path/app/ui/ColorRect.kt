package com.sixbynine.transit.path.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.model.ColorWrapper
import com.sixbynine.transit.path.model.unwrap

@Composable
fun ColorRect(
    colors: List<ColorWrapper>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .width(12.dp)
    ) {
        colors.take(3).forEach { color ->
            Box(
                Modifier
                    .width(12.dp)
                    .weight(1f)
                    .background(color.unwrap())
            )
        }
    }
}
