package com.desaiwang.transit.path.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.desaiwang.transit.path.model.ColorWrapper
import com.desaiwang.transit.path.model.unwrap
import com.desaiwang.transit.path.util.conditional

@Composable
fun ColorCircle(colors: List<ColorWrapper>, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(24.dp)
            .clip(CircleShape)
            .conditional(isSystemInDarkTheme()) {
                border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
            }
    ) {
        colors.firstOrNull()?.let { color ->
            Box(Modifier.size(24.dp).clip(CircleShape).background(color.unwrap()))
        }

        colors.getOrNull(1)?.let { color ->
            Box(
                Modifier.size(24.dp)
                    .clip(CircleShape)
                    .padding(start = 12.dp)
                    .clip(RectangleShape)
                    .background(color.unwrap())
            )
        }

        colors.getOrNull(2)?.let { color ->
            Box(
                Modifier.size(24.dp)
                    .clip(CircleShape)
                    .padding(start = 12.dp, top = 12.dp)
                    .clip(RectangleShape)
                    .background(color.unwrap())
            )
        }
    }
}