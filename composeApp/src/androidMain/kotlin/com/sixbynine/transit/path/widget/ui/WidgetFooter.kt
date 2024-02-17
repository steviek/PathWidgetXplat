package com.sixbynine.transit.path.widget.ui

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build.VERSION
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalGlanceId
import androidx.glance.LocalSize
import androidx.glance.Visibility.Gone
import androidx.glance.Visibility.Invisible
import androidx.glance.Visibility.Visible
import androidx.glance.action.Action
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.visibility
import com.sixbynine.transit.path.MR.strings
import com.sixbynine.transit.path.R.drawable
import com.sixbynine.transit.path.resources.getString
import com.sixbynine.transit.path.util.isLoading
import com.sixbynine.transit.path.widget.SmallWidgetSize
import com.sixbynine.transit.path.widget.UpdateWidgetAction
import com.sixbynine.transit.path.widget.WidgetDataFormatter
import com.sixbynine.transit.path.widget.glance.GlanceTheme
import com.sixbynine.transit.path.widget.glance.ImageButton
import com.sixbynine.transit.path.widget.glance.Text
import com.sixbynine.transit.path.widget.setup.WidgetSetupActivity

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
        ImageButton(
            modifier =
            GlanceModifier
                .visibility(if (VERSION.SDK_INT >= 31) Invisible else Visible),
            srcResId = drawable.ic_edit_inset,
            contentDesc = strings.edit,
            onClick = startConfigurationActivityAction()
        )

        Spacer(modifier = GlanceModifier.defaultWeight().height(1.dp))

        val isSmallSize = LocalSize.current == SmallWidgetSize
        val updatedAtText = when {
            result.isLoading() && isSmallSize -> getString(strings.refreshing_short)
            result.isLoading() -> getString(strings.refreshing)
            isSmallSize -> WidgetDataFormatter.formatTime(updateTime)
            else -> {
                getString(strings.updated_at_time, WidgetDataFormatter.formatTime(updateTime))
            }
        }

        Text(
            text = updatedAtText,
            style = GlanceTheme.typography.secondary,
            fontSize = 12.sp,
        )

        Spacer(modifier = GlanceModifier.defaultWeight().height(1.dp))

        Spacer(GlanceModifier.width(8.dp))


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
                contentDesc = strings.update_now,
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

@Composable
private fun startConfigurationActivityAction(): Action {
    val context = LocalContext.current
    val appWidgetManager = GlanceAppWidgetManager(context)
    val appWidgetId = appWidgetManager.getAppWidgetId(LocalGlanceId.current)
    val configurationIntent =
        Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
            .setClass(LocalContext.current, WidgetSetupActivity::class.java)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    return actionStartActivity(configurationIntent)
}

internal val WidgetFooterHeight = 48.dp
