package com.desaiwang.transit.path.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.glance.LocalContext
import androidx.glance.LocalGlanceId
import androidx.glance.action.Action
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity
import com.desaiwang.transit.path.widget.setup.WidgetSetupActivity

@Composable
fun startConfigurationActivityAction(): Action {
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