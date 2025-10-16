package com.desaiwang.transit.path.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.desaiwang.transit.path.model.ColorWrapper
import com.desaiwang.transit.path.model.unwrap
import com.desaiwang.transit.path.util.conditional

@Composable
fun ColorRect(
    colors: List<ColorWrapper>,
    height: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .width(12.dp)
            .height(height)
    ) {
        // First color - fills entire height
        colors.firstOrNull()?.let {
            Box(
                Modifier
                    .width(12.dp)
                    .height(height)
                    .background(it.unwrap())
            )
        }

        // Second color - fills bottom 50%
        colors.getOrNull(1)?.let {
            Box(
                Modifier
                    .width(12.dp)
                    .height(height)
                    .padding(start = 0.dp, top = height / 2)
                    .clip(RectangleShape)
                    .background(it.unwrap())
            )
        }

        // Third color - fills bottom 25%
        colors.getOrNull(2)?.let {
            Box(
                Modifier
                    .width(12.dp)
                    .height(height)
                    .padding(start = 0.dp, top = height * 0.75f)
                    .clip(RectangleShape)
                    .background(it.unwrap())
            )
        }
    }
}