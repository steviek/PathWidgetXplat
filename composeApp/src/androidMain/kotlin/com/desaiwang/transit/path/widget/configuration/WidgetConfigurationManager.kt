package com.desaiwang.transit.path.widget.configuration

import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.desaiwang.transit.path.MobilePathApplication
import com.desaiwang.transit.path.api.Line
import com.desaiwang.transit.path.api.StationSort
import com.desaiwang.transit.path.api.TrainFilter
import com.desaiwang.transit.path.preferences.IntPersistable
import com.desaiwang.transit.path.util.JsonFormat
import com.desaiwang.transit.path.widget.DepartureBoardWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlin.time.Duration.Companion.seconds

object WidgetConfigurationManager {

    private val context get() = MobilePathApplication.instance
    private val DEPARTURE_WIDGET_PREFS_KEY = stringPreferencesKey("departure_widget_data")
    private const val SCHEMA_VERSION = 5

    init {
        GlobalScope.launch(Dispatchers.IO) {
            // Don't impact startup for this.
            delay(5.seconds)
            getGlanceIds().forEach { id ->
                val storedConfig = getWidgetConfigurationWithoutMigrating(id) ?: return@forEach
                val migratedConfig = storedConfig.migrateToCurrentVersion()
                if (storedConfig !== migratedConfig) {
                    setWidgetConfiguration(id, migratedConfig)
                }
            }
        }
    }

    suspend fun getWidgetConfiguration(id: GlanceId?): StoredWidgetConfiguration? {
        return getWidgetConfigurationWithoutMigrating(id)?.migrateToCurrentVersion()
    }

    suspend fun getWidgetConfigurations(): Map<GlanceId, StoredWidgetConfiguration> {
        return getGlanceIds()
            .mapNotNull { id -> getWidgetConfiguration(id)?.let { id to it } }
            .toMap()
    }

    private suspend fun getWidgetConfigurationWithoutMigrating(
        id: GlanceId?
    ): StoredWidgetConfiguration? {
        id ?: return null
        val state = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        val json = state[DEPARTURE_WIDGET_PREFS_KEY] ?: return null

        return deserializeConfiguration(json)
    }

    private suspend fun getGlanceIds(): List<GlanceId> {
        return GlanceAppWidgetManager(context).getGlanceIds(DepartureBoardWidget::class.java)
    }

    @Composable
    fun GlanceAppWidget.getWidgetConfiguration(): StoredWidgetConfiguration? {
        val json = currentState(key = DEPARTURE_WIDGET_PREFS_KEY) ?: return null
        return deserializeConfiguration(json)?.migrateToCurrentVersion()
    }

    private fun deserializeConfiguration(json: String): StoredWidgetConfiguration? {
        return runCatching { JsonFormat.decodeFromString<StoredWidgetConfiguration>(json) }
            .getOrNull()
    }

    suspend fun setWidgetConfiguration(
        id: GlanceId?,
        stations: Collection<String>,
        lines: Collection<Line>,
        useClosestStation: Boolean,
        sort: StationSort,
        filter: TrainFilter,
    ) {
        id ?: return

        val configuration = StoredWidgetConfiguration(
            fixedStations = stations.toSet(),
            linesBitmask = IntPersistable.createBitmask(lines),
            useClosestStation = useClosestStation,
            sortOrder = sort,
            filter = filter,
            version = SCHEMA_VERSION
        )

        setWidgetConfiguration(id, configuration)
    }

    private suspend fun setWidgetConfiguration(
        id: GlanceId,
        configuration: StoredWidgetConfiguration
    ) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { newState ->
            newState.toMutablePreferences()
                .apply {
                    this[DEPARTURE_WIDGET_PREFS_KEY] = JsonFormat.encodeToString(configuration)
                }
        }
    }
}
