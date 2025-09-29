package com.desaiwang.transit.path.app.ui.theme

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Dimensions {
    fun gutter(isTablet: Boolean): Dp {
        return if (isTablet) 64.dp else 16.dp
    }

    fun BoxWithConstraintsScope.isTablet(): Boolean {
        return minOf(maxWidth, maxHeight) >= 480.dp
    }
}
