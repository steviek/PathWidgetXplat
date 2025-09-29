package com.desaiwang.transit.path.widget

import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.MutablePreferences
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.desaiwang.transit.path.MobilePathApplication

suspend inline fun <reified T : GlanceAppWidget> updateAppWidgetStates(
    crossinline updateState: suspend (MutablePreferences, GlanceId) -> Unit,
) {
    val context = MobilePathApplication.instance
    GlanceAppWidgetManager(context)
        .getGlanceIds(T::class.java)
        .forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { preferences ->
                updateState(preferences, glanceId)
            }
        }
}

fun @receiver:ColorInt Int.toColor(): Color {
    return Color(
        red = android.graphics.Color.red(this),
        green = android.graphics.Color.green(this),
        blue = android.graphics.Color.blue(this),
        alpha = android.graphics.Color.alpha(this)
    )
}
