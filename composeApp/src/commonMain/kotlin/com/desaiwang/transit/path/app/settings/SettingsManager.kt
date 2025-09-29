package com.desaiwang.transit.path.app.settings

import com.desaiwang.transit.path.Logging
import com.desaiwang.transit.path.analytics.Analytics
import com.desaiwang.transit.path.api.Line
import com.desaiwang.transit.path.api.LocationSetting
import com.desaiwang.transit.path.api.StationSort
import com.desaiwang.transit.path.api.TrainFilter
import com.desaiwang.transit.path.location.LocationPermissionRequestResult.Denied
import com.desaiwang.transit.path.location.LocationPermissionRequestResult.Granted
import com.desaiwang.transit.path.location.LocationProvider
import com.desaiwang.transit.path.location.awaitIsLocationSupportedByDevice
import com.desaiwang.transit.path.preferences.IntPersistable
import com.desaiwang.transit.path.preferences.IntPreferencesKey
import com.desaiwang.transit.path.preferences.persisting
import com.desaiwang.transit.path.util.JsonFormat
import com.desaiwang.transit.path.util.collectIn
import com.desaiwang.transit.path.util.globalDataStore
import com.desaiwang.transit.path.util.launchAndReturnUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

object SettingsManager {
    private const val AvoidMissingTrainsKey = "avoid_missing_trains"

    private val trainFilterPersister = SettingPersister("train_filter", TrainFilter.All)
    private val groupTrainsPersister = SettingPersister("group_trains", true)
    private val lineFilterPersister = BitFlagSettingPersister("line_filter", Line.permanentLines)
    private val timeDisplayPersister = SettingPersister("time_display", TimeDisplay.Relative)
    private val stationLimitPersister = SettingPersister("station_limit", StationLimit.ThreePerLine)
    private val stationSortPersister = SettingPersister("station_sort", StationSort.Alphabetical)
    private val displayPresumedTrainsPersister = SettingPersister("show_presumed_trains", true)
    private val locationSettingPersister =
        SettingPersister("location_setting", LocationSetting.Disabled)
    private val avoidMissingTrainsPersister =
        GlobalSettingPersister(AvoidMissingTrainsKey, AvoidMissingTrains.Disabled)
    private val commutingConfigurationPersister =
        GlobalSettingPersister<CommutingConfiguration>(
            "commuting",
            defaultValue = CommutingConfiguration.default(),
            toString = { JsonFormat.encodeToString(it) },
            fromString = { JsonFormat.decodeFromString(it) }
        )
    private val devOptionsPersister = GlobalSettingPersister("dev_options", false)

    val locationSetting = locationSettingPersister.flow
    val groupTrains = groupTrainsPersister.flow
    val trainFilter = trainFilterPersister.flow
    val lineFilter = lineFilterPersister.flow
    val timeDisplay = timeDisplayPersister.flow
    val stationLimit = stationLimitPersister.flow
    val stationSort = stationSortPersister.flow
    val displayPresumedTrains = displayPresumedTrainsPersister.flow
    val avoidMissingTrains = avoidMissingTrainsPersister.flow
    val commutingConfiguration = commutingConfigurationPersister.flow
    val devOptionsEnabled = devOptionsPersister.flow

    private val _settings = MutableStateFlow(
        AppSettings(
            locationSetting.value,
            trainFilter.value,
            lineFilter.value,
            timeDisplay.value,
            stationLimit.value,
            stationSort.value,
            displayPresumedTrains.value,
            avoidMissingTrains.value,
            commutingConfiguration.value,
            groupTrains.value,
        )
    )
    val settings = _settings.asStateFlow()

    private val lightweightScope = CoroutineScope(Dispatchers.Default)

    private var schemaVersion by persisting(IntPreferencesKey("settings_schema"))

    init {
        val prevSchemaVersion = schemaVersion
        val currentSchemaVersion = 3
        if (prevSchemaVersion == null || prevSchemaVersion < currentSchemaVersion) {
            performMigration(prevSchemaVersion)
            schemaVersion = currentSchemaVersion
        }

        lightweightScope.launch {
            if (!LocationProvider().awaitIsLocationSupportedByDevice() ||
                !LocationProvider().hasLocationPermission()
            ) {
                locationSettingPersister.update(LocationSetting.Disabled)
            }

            LocationProvider().locationPermissionResults.collect {
                when (it) {
                    Denied -> {
                        locationSettingPersister.update(LocationSetting.Disabled)
                    }

                    Granted -> {
                        if (locationSettingPersister.flow.value ==
                            LocationSetting.EnabledPendingPermission
                        ) {
                            locationSettingPersister.update(LocationSetting.Enabled)
                        }
                    }
                }
            }
        }

        locationSetting.collectIn(lightweightScope) { newSetting ->
            _settings.update { it.copy(locationSetting = newSetting) }
        }

        trainFilter.collectIn(lightweightScope) { newSetting ->
            _settings.update { it.copy(trainFilter = newSetting) }
        }

        lineFilter.collectIn(lightweightScope) { newSetting ->
            _settings.update { it.copy(lineFilters = newSetting) }
        }

        timeDisplay.collectIn(lightweightScope) { newSetting ->
            _settings.update { it.copy(timeDisplay = newSetting) }
        }

        stationLimit.collectIn(lightweightScope) { newSetting ->
            _settings.update { it.copy(stationLimit = newSetting) }
        }

        stationSort.collectIn(lightweightScope) { newSetting ->
            _settings.update { it.copy(stationSort = newSetting) }
        }

        displayPresumedTrains.collectIn(lightweightScope) { newSetting ->
            _settings.update { it.copy(displayPresumedTrains = newSetting) }
        }

        avoidMissingTrains.collectIn(lightweightScope) { newSetting ->
            _settings.update { it.copy(avoidMissingTrains = newSetting) }
        }

        commutingConfiguration.collectIn(lightweightScope) { newSetting ->
            _settings.update { it.copy(commutingConfiguration = newSetting) }
        }

        groupTrains.collectIn(lightweightScope) { newSetting ->
            _settings.update { it.copy(groupTrains = newSetting) }
        }

        devOptionsEnabled.collectIn(lightweightScope) { isEnabled ->
            Logging.isDeveloperMode = isEnabled
        }
    }

    fun updateLocationSetting(enabled: Boolean) = lightweightScope.launchAndReturnUnit {
        val setting = when {
            !enabled -> LocationSetting.Disabled
            LocationProvider().hasLocationPermission() -> LocationSetting.Enabled
            else -> LocationSetting.EnabledPendingPermission
        }
        Analytics.locationSettingSet(setting)
        locationSettingPersister.update(setting)

        if (setting == LocationSetting.EnabledPendingPermission) {
            LocationProvider().requestLocationPermission()
        }
    }

    fun updateTrainFilter(trainFilter: TrainFilter) = lightweightScope.launchAndReturnUnit {
        Analytics.filterSet(trainFilter)
        trainFilterPersister.update(trainFilter)
    }

    fun updateLineFilters(lineFilters: Set<Line>) = lightweightScope.launchAndReturnUnit {
        Analytics.lineFiltersSet(lineFilters)
        lineFilterPersister.update(lineFilters)
    }

    fun updateTimeDisplay(timeDisplay: TimeDisplay) = lightweightScope.launchAndReturnUnit {
        Analytics.timeDisplaySet(timeDisplay)
        timeDisplayPersister.update(timeDisplay)
    }

    fun updateStationLimit(stationLimit: StationLimit) = lightweightScope.launchAndReturnUnit {
        Analytics.stationLimitSet(stationLimit)
        stationLimitPersister.update(stationLimit)
    }

    fun updateStationSort(stationSort: StationSort) = lightweightScope.launchAndReturnUnit {
        Analytics.stationSortSet(stationSort)
        stationSortPersister.update(stationSort)
    }

    fun updateDisplayPresumedTrains(
        displayPresumedTrains: Boolean,
    ) = lightweightScope.launchAndReturnUnit {
        displayPresumedTrainsPersister.update(displayPresumedTrains)
    }

    fun updateAvoidMissingTrains(
        avoidMissingTrains: AvoidMissingTrains,
    ) = lightweightScope.launchAndReturnUnit {
        Analytics.avoidMissingTrainsSet(avoidMissingTrains)
        avoidMissingTrainsPersister.update(avoidMissingTrains)
    }

    fun currentAvoidMissingTrains(): AvoidMissingTrains {
        return globalDataStore().getLong(AvoidMissingTrainsKey)
            ?.let { IntPersistable.fromPersistence(it.toInt()) }
            ?: AvoidMissingTrains.Disabled
    }

    fun updateCommutingConfiguration(
        configuration: CommutingConfiguration,
    ) = lightweightScope.launchAndReturnUnit {
        Analytics.commutingConfigurationSet()
        commutingConfigurationPersister.update(configuration)
    }

    fun updateGroupTrains(
        groupTrains: Boolean,
    ) = lightweightScope.launchAndReturnUnit {
        groupTrainsPersister.update(groupTrains)
    }

    fun updateDevOptionsEnabled(enabled: Boolean) = lightweightScope.launchAndReturnUnit {
        devOptionsPersister.update(enabled)
    }

    private fun performMigration(prevVersion: Int?) {

    }
}
