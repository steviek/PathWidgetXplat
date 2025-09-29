package com.desaiwang.transit.path.widget.glance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.sp
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle

data class Typography(val header: TextStyle, val primary: TextStyle, val secondary: TextStyle)

@Composable
fun createTypography(
    header: TextStyle = TextStyle(
        color = GlanceTheme.colors.onSurface,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
    ),
    primary: TextStyle = TextStyle(
        color = GlanceTheme.colors.onSurface,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
    ),
    secondary: TextStyle = TextStyle(
        color = GlanceTheme.colors.onSurfaceVariant,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
    ),
): Typography {
    return Typography(
        header = header,
        primary = primary,
        secondary = secondary,
    )
}

internal val LocalTypography: ProvidableCompositionLocal<Typography> =
    staticCompositionLocalOf { error("no typography") }
