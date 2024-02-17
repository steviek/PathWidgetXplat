package com.sixbynine.transit.path.widget.configuration

import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.sixbynine.transit.path.PathApplication
import com.sixbynine.transit.path.util.JsonFormat
import kotlinx.serialization.encodeToString

object WidgetConfigurationManager {

    private val context get() = PathApplication.instance
    private val DEPARTURE_WIDGET_PREFS_KEY = stringPreferencesKey("departure_widget_data")

    suspend fun getWidgetConfiguration(id: GlanceId?): StoredWidgetConfiguration? {
        id ?: return null
        val state = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        val json = state[DEPARTURE_WIDGET_PREFS_KEY] ?: return null

        return deserializeConfiguration(json)
    }

    @Composable
    fun GlanceAppWidget.getWidgetConfiguration(): StoredWidgetConfiguration? {
        val json = currentState(key = DEPARTURE_WIDGET_PREFS_KEY) ?: return null
        return deserializeConfiguration(json)
    }

    private fun deserializeConfiguration(json: String): StoredWidgetConfiguration? {
        return runCatching {
            JsonFormat.decodeFromString<StoredWidgetConfiguration>(json)
        }.getOrNull()
    }

    suspend fun setWidgetConfiguration(id: GlanceId?, configuration: StoredWidgetConfiguration) {
        id ?: return
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { newState ->
            newState.toMutablePreferences()
                .apply {
                    this[DEPARTURE_WIDGET_PREFS_KEY] = JsonFormat.encodeToString(configuration)
                }
        }
    }
}
