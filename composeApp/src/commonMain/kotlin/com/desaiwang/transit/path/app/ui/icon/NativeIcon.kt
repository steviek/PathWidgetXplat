package com.desaiwang.transit.path.app.ui.icon

import PlatformType.ANDROID
import PlatformType.IOS
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import getPlatform

enum class IconType {
    Edit, Station, Filter, Sort, LayoutOneColumn, ArrowUp, ArrowDown, Settings, Delete, Back,
    ExpandDown, Internet
}

@Composable
expect fun IconPainter(icon: IconType): Painter

@Composable
fun NativeIconButton(
    icon: IconType,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    enabled: Boolean = true,
    buttonSize: Dp = 48.dp,
    iconSize: Dp = 24.dp,
) {
    val actualIconSize = when (getPlatform().type) {
        ANDROID -> iconSize
        IOS -> iconSize - 4.dp
    }
    val painter = IconPainter(icon)
    return IconButton(
        onClick = onClick,
        modifier = modifier.size(buttonSize),
        enabled = enabled,
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.size(actualIconSize),
            tint = if (tint.isSpecified) tint else LocalContentColor.current,
        )
    }
}
