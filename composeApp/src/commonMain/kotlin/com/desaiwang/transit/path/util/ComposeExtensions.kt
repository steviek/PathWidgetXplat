package com.desaiwang.transit.path.util

import androidx.compose.ui.Modifier

inline fun Modifier.conditional(condition: Boolean, transform: Modifier.() -> Modifier): Modifier {
    return if (condition) transform() else this
}
