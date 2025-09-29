package com.desaiwang.transit.path.widget.glance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.glance.GlanceComposable
import androidx.glance.color.ColorProviders

object GlanceTheme {
    val colors: ColorProviders
        @GlanceComposable
        @Composable
        @ReadOnlyComposable
        get() = androidx.glance.GlanceTheme.colors

    val typography: Typography
        @GlanceComposable
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current
}

@Composable
fun GlanceTheme(
    colors: ColorProviders = androidx.glance.GlanceTheme.colors,
    content: @GlanceComposable @Composable () -> Unit
) {
    androidx.glance.GlanceTheme(colors) {
        CompositionLocalProvider(
            LocalTypography provides createTypography(),
            content = content
        )
    }
}
