package com.desaiwang.transit.path.app.ui

import LocalIsTablet
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.desaiwang.transit.path.app.ui.theme.Dimensions

interface AppUiScope {
    val isTablet: Boolean
}

@Composable
fun gutter(): Dp {
    return Dimensions.gutter(isTablet = LocalIsTablet.current)
}