package com.sixbynine.transit.path.app.ui

import androidx.compose.ui.graphics.Color

data class ColorWrapper(val color: Color) {
    val red: Float
        get() = color.red

    val green: Float
        get() = color.green

    val blue: Float
        get() = color.blue
}
