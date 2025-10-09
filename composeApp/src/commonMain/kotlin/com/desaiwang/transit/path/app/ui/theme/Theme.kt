package com.desaiwang.transit.path.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2156CA),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDBE1FF),
    onPrimaryContainer = Color(0xFF00174A),
    secondary = Color(0xFF2E59BB),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDBE1FF),
    onSecondaryContainer = Color(0xFF001849),
    tertiary = Color(0xFF626200),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEAE942),
    onTertiaryContainer = Color(0xFF1D1D00),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFEEEEEE),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFEEEEEE),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE2E2EC),
    onSurfaceVariant = Color(0xFF45464F),
    outline = Color(0xFF757680),
    inverseOnSurface = Color(0xFFF2F0F4),
    inverseSurface = Color(0xFF303034),
    inversePrimary = Color(0xFFB3C5FF),
    surfaceTint = Color(0xFF2156CA),
    outlineVariant = Color(0xFFC5C6D0),
    scrim = Color(0xFF000000)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB3C5FF),
    onPrimary = Color(0xFF002A77),
    primaryContainer = Color(0xFF003EA6),
    onPrimaryContainer = Color(0xFFDBE1FF),
    secondary = Color(0xFFB3C5FF),
    onSecondary = Color(0xFF002B75),
    secondaryContainer = Color(0xFF053FA2),
    onSecondaryContainer = Color(0xFFDBE1FF),
    tertiary = Color(0xFFCDCD24),
    onTertiary = Color(0xFF323200),
    tertiaryContainer = Color(0xFF4A4900),
    onTertiaryContainer = Color(0xFFEAE942),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0E0E0E),
    onBackground = Color(0xFFE4E2E6),
    surface = Color(0xFF0E0E0E),
    onSurface = Color(0xFFE4E2E6),
    surfaceVariant = Color(0xFF45464F),
    onSurfaceVariant = Color(0xFFC5C6D0),
    outline = Color(0xFF8F909A),
    inverseOnSurface = Color(0xFF1B1B1F),
    inverseSurface = Color(0xFFE4E2E6),
    inversePrimary = Color(0xFF2156CA),
    surfaceTint = Color(0xFFB3C5FF),
    outlineVariant = Color(0xFF45464F),
    scrim = Color(0xFF000000)
)

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if (!useDarkTheme) {
        LightColors
    } else {
        DarkColors
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
