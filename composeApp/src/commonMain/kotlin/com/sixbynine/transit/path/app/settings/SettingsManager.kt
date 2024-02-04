package com.sixbynine.transit.path.app.settings

import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.TrainFilter

object SettingsManager {
    private val trainFilterPersister = SettingPersister("train_filter", TrainFilter.All)
    private val timeDisplayPersister = SettingPersister("time_display", TimeDisplay.Relative)
    private val stationLimitPersister = SettingPersister("station_limit", StationLimit.ThreePerLine)
    private val stationSortPersister = SettingPersister("station_sort", StationSort.Alphabetical)
    private val displayPresumedTrainsPersister = SettingPersister("show_presumed_trains", true)

    val trainFilter = trainFilterPersister.flow
    val timeDisplay = timeDisplayPersister.flow
    val stationLimit = stationLimitPersister.flow
    val stationSort = stationSortPersister.flow
    val displayPresumedTrains = displayPresumedTrainsPersister.flow

    fun updateTrainFilter(trainFilter: TrainFilter) {
        trainFilterPersister.update(trainFilter)
    }

    fun updateTimeDisplay(timeDisplay: TimeDisplay) {
        timeDisplayPersister.update(timeDisplay)
    }

    fun updateStationLimit(stationLimit: StationLimit) {
        stationLimitPersister.update(stationLimit)
    }

    fun updateStationSort(stationSort: StationSort) {
        stationSortPersister.update(stationSort)
    }

    fun updateDisplayPresumedTrains(displayPresumedTrains: Boolean) {
        displayPresumedTrainsPersister.update(displayPresumedTrains)
    }
}