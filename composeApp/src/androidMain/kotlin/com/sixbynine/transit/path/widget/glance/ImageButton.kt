package com.sixbynine.transit.path.widget.glance

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceComposable
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import org.jetbrains.compose.resources.StringResource

@GlanceComposable
@Composable
fun ImageButton(
    modifier: GlanceModifier = GlanceModifier,
    @DrawableRes srcResId: Int,
    contentDesc: StringResource,
    onClick: Action,
) {
    Image(
        modifier = modifier.clickable(onClick).cornerRadius(200.dp),
        provider = ImageProvider(srcResId),
        contentDescription = stringResource(contentDesc),
        colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
    )
}
