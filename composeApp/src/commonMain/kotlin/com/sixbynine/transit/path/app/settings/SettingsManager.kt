package com.sixbynine.transit.path.app.settings

import com.sixbynine.transit.path.analytics.Analytics
import com.sixbynine.transit.path.api.LocationSetting
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.TrainFilter
import com.sixbynine.transit.path.location.LocationPermissionRequestResult
import com.sixbynine.transit.path.location.LocationProvider
import com.sixbynine.transit.path.util.combineStates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object SettingsManager {
    private val trainFilterPersister = SettingPersister("train_filter", TrainFilter.All)
    private val timeDisplayPersister = SettingPersister("time_display", TimeDisplay.Relative)
    private val stationLimitPersister = SettingPersister("station_limit", StationLimit.ThreePerLine)
    private val stationSortPersister = SettingPersister("station_sort", StationSort.Alphabetical)
    private val displayPresumedTrainsPersister = SettingPersister("show_presumed_trains", true)
    private val locationSettingPersister =
        SettingPersister("location_setting", LocationSetting.Disabled)

    val locationSetting = locationSettingPersister.flow
    val trainFilter = trainFilterPersister.flow
    val timeDisplay = timeDisplayPersister.flow
    val stationLimit = stationLimitPersister.flow
    val stationSort = stationSortPersister.flow
    val displayPresumedTrains = displayPresumedTrainsPersister.flow

    val settings = combineStates(
        locationSetting,
        trainFilter,
        timeDisplay,
        stationLimit,
        stationSort,
        displayPresumedTrains,
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
                if (it is LocationPermissionRequestResult.Denied) {
                    locationSettingPersister.update(LocationSetting.Disabled)
                }
            }
        }
    }

    fun updateLocationSetting(enabled: Boolean) {
        val setting = if (enabled) {
            LocationSetting.Enabled
        } else {
            LocationSetting.Disabled
        }
        Analytics.locationSettingSet(setting)
        locationSettingPersister.update(setting)

        if (enabled && !LocationProvider().hasLocationPermission()) {
            LocationProvider().requestLocationPermission()
        }
    }

    fun updateTrainFilter(trainFilter: TrainFilter) {
        Analytics.filterSet(trainFilter)
        trainFilterPersister.update(trainFilter)
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
}
