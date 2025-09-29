package com.desaiwang.transit.path.widget.ui

import android.os.Build.VERSION
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.Visibility.Gone
import androidx.glance.Visibility.Invisible
import androidx.glance.Visibility.Visible
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.visibility
import com.desaiwang.transit.path.MainActivity.Companion.createAppWidgetLaunchAction
import com.desaiwang.transit.path.R.drawable
import com.desaiwang.transit.path.util.isFailure
import com.desaiwang.transit.path.util.isLoading
import com.desaiwang.transit.path.widget.SmallWidgetSize
import com.desaiwang.transit.path.widget.UpdateWidgetAction
import com.desaiwang.transit.path.widget.WidgetDataFormatter
import com.desaiwang.transit.path.widget.glance.GlanceTheme
import com.desaiwang.transit.path.widget.glance.ImageButton
import com.desaiwang.transit.path.widget.glance.Text
import com.desaiwang.transit.path.widget.glance.stringResource
import com.desaiwang.transit.path.widget.startConfigurationActivityAction
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.alerts
import pathwidgetxplat.composeapp.generated.resources.edit
import pathwidgetxplat.composeapp.generated.resources.error_long
import pathwidgetxplat.composeapp.generated.resources.error_short
import pathwidgetxplat.composeapp.generated.resources.no_internet
import pathwidgetxplat.composeapp.generated.resources.refreshing
import pathwidgetxplat.composeapp.generated.resources.refreshing_short
import pathwidgetxplat.composeapp.generated.resources.update_now
import pathwidgetxplat.composeapp.generated.resources.updated_at_time

@Composable
fun WidgetFooter(
    state: WidgetState,
    modifier: GlanceModifier = GlanceModifier,
) {
    val (result, updateTime) = state
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        if (VERSION.SDK_INT >= 31) {
            val alertCount = result.data?.globalAlerts?.size ?: 0
            if (alertCount > 0) {
                ImageButton(
                    srcResId = drawable.ic_warning_inset,
                    contentDesc = string.alerts,
                    onClick = createAppWidgetLaunchAction(),
                    tintColor = GlanceTheme.colors.error,
                )
            } else {
                Spacer(GlanceModifier.size(40.dp))
            }
        } else {
            // sorry old phones, I don't want to think about your real estate situation yet. You
            // need this edit button.
            ImageButton(
                srcResId = drawable.ic_edit_inset,
                contentDesc = string.edit,
                onClick = startConfigurationActivityAction()
            )
        }


        Spacer(modifier = GlanceModifier.defaultWeight().height(1.dp))

        val isSmallSize = LocalSize.current == SmallWidgetSize
        val updatedAtText = when {
            result.isFailure() && !result.hadInternet -> stringResource(string.no_internet)
            result.isFailure() && isSmallSize -> stringResource(string.error_short)
            result.isFailure() -> stringResource(string.error_long)
            result.isLoading() && isSmallSize -> stringResource(string.refreshing_short)
            result.isLoading() -> stringResource(string.refreshing)
            isSmallSize -> WidgetDataFormatter.formatTime(updateTime)
            else -> {
                stringResource(string.updated_at_time, WidgetDataFormatter.formatTime(updateTime))
            }
        }

        Text(
            text = updatedAtText,
            style = GlanceTheme.typography.secondary,
            fontSize = 12.sp,
        )

        Spacer(modifier = GlanceModifier.defaultWeight().height(1.dp))

        Box(
            modifier = GlanceModifier.size(WidgetFooterHeight),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = GlanceModifier.visibility(
                    if (result.isLoading()) {
                        Visible
                    } else {
                        Invisible
                    }
                )
                    .size(20.dp),
                color = GlanceTheme.colors.primary
            )

            ImageButton(
                srcResId = drawable.ic_refresh_inset,
                contentDesc = string.update_now,
                onClick = actionRunCallback<UpdateWidgetAction>(),
                modifier = GlanceModifier
                    .size(WidgetFooterHeight)
                    .visibility(
                        if (result.isLoading()) {
                            Gone
                        } else {
                            Visible
                        }
                    )
            )
        }
    }
}

internal val WidgetFooterHeight = 48.dp
