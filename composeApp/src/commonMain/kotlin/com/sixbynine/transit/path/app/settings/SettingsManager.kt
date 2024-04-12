package com.sixbynine.transit.path.app.settings

import com.sixbynine.transit.path.analytics.Analytics
import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.LocationSetting
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.TrainFilter
import com.sixbynine.transit.path.location.LocationPermissionRequestResult.Denied
import com.sixbynine.transit.path.location.LocationPermissionRequestResult.Granted
import com.sixbynine.transit.path.location.LocationProvider
import com.sixbynine.transit.path.preferences.IntPersistable
import com.sixbynine.transit.path.util.combineStates
import com.sixbynine.transit.path.widget.globalDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object SettingsManager {
    private const val AvoidMissingTrainsKey = "avoid_missing_trains"

    private val trainFilterPersister = SettingPersister("train_filter", TrainFilter.All)
    private val lineFilterPersister = BitFlagSettingPersister("line_filter", Line.entries)
    private val timeDisplayPersister = SettingPersister("time_display", TimeDisplay.Relative)
    private val stationLimitPersister = SettingPersister("station_limit", StationLimit.ThreePerLine)
    private val stationSortPersister = SettingPersister("station_sort", StationSort.Alphabetical)
    private val displayPresumedTrainsPersister = SettingPersister("show_presumed_trains", true)
    private val locationSettingPersister =
        SettingPersister("location_setting", LocationSetting.Disabled)
    private val avoidMissingTrainsPersister =
        GlobalSettingPersister(AvoidMissingTrainsKey, AvoidMissingTrains.Disabled)

    val locationSetting = locationSettingPersister.flow
    val trainFilter = trainFilterPersister.flow
    val lineFilter = lineFilterPersister.flow
    val timeDisplay = timeDisplayPersister.flow
    val stationLimit = stationLimitPersister.flow
    val stationSort = stationSortPersister.flow
    val displayPresumedTrains = displayPresumedTrainsPersister.flow
    val avoidMissingTrains = avoidMissingTrainsPersister.flow

    val settings = combineStates(
        locationSetting,
        trainFilter,
        lineFilter,
        timeDisplay,
        stationLimit,
        stationSort,
        displayPresumedTrains,
        avoidMissingTrains,
        ::AppSettings
    )

    init {
        GlobalScope.launch(Dispatchers.Default) {
            if (!LocationProvider().isLocationSupportedByDevice ||
                !LocationProvider().hasLocationPermission()
            ) {
                locationSettingPersister.update(LocationSetting.Disabled)
            }

            LocationProvider().locationPermissionResults.collectLatest {
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
    }

    fun updateLocationSetting(enabled: Boolean) {
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

    fun updateTrainFilter(trainFilter: TrainFilter) {
        Analytics.filterSet(trainFilter)
        trainFilterPersister.update(trainFilter)
    }

    fun updateLineFilters(lineFilters: Set<Line>) {
        Analytics.lineFiltersSet(lineFilters)
        lineFilterPersister.update(lineFilters)
    }

    fun updateTimeDisplay(timeDisplay: TimeDisplay) {
        Analytics.timeDisplaySet(timeDisplay)
        timeDisplayPersister.update(timeDisplay)
    }

    fun updateStationLimit(stationLimit: StationLimit) {
        Analytics.stationLimitSet(stationLimit)
        stationLimitPersister.update(stationLimit)
    }

    fun updateStationSort(stationSort: StationSort) {
        Analytics.stationSortSet(stationSort)
        stationSortPersister.update(stationSort)
    }

    fun updateDisplayPresumedTrains(displayPresumedTrains: Boolean) {
        displayPresumedTrainsPersister.update(displayPresumedTrains)
    }

    fun updateAvoidMissingTrains(avoidMissingTrains: AvoidMissingTrains) {
        Analytics.avoidMissingTrainsSet(avoidMissingTrains)
        avoidMissingTrainsPersister.update(avoidMissingTrains)
    }

    fun currentAvoidMissingTrains(): AvoidMissingTrains {
        return globalDataStore().getLong(AvoidMissingTrainsKey)
            ?.let { IntPersistable.fromPersistence(it.toInt()) }
            ?: AvoidMissingTrains.Disabled
    }
}
