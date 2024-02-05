package com.sixbynine.transit.path.app.settings

import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.TrainFilter

data class AppSettings(
    val trainFilter: TrainFilter,
    val timeDisplay: TimeDisplay,
    val stationLimit: StationLimit,
    val stationSort: StationSort,
    val displayPresumedTrains: Boolean
)
