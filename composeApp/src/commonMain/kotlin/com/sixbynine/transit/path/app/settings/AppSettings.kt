package com.sixbynine.transit.path.app.settings

import com.sixbynine.transit.path.api.LineFilter
import com.sixbynine.transit.path.api.LocationSetting
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.TrainFilter

data class AppSettings(
    val locationSetting: LocationSetting,
    val trainFilter: TrainFilter,
    val lineFilters: Set<LineFilter>,
    val timeDisplay: TimeDisplay,
    val stationLimit: StationLimit,
    val stationSort: StationSort,
    val displayPresumedTrains: Boolean
)
