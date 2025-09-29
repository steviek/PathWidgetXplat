package com.desaiwang.transit.path.app.settings

import com.desaiwang.transit.path.api.Line
import com.desaiwang.transit.path.api.LocationSetting
import com.desaiwang.transit.path.api.StationSort
import com.desaiwang.transit.path.api.TrainFilter

data class AppSettings(
    val locationSetting: LocationSetting,
    val trainFilter: TrainFilter,
    val lineFilters: Set<Line>,
    val timeDisplay: TimeDisplay,
    val stationLimit: StationLimit,
    val stationSort: StationSort,
    val displayPresumedTrains: Boolean,
    val avoidMissingTrains: AvoidMissingTrains,
    val commutingConfiguration: CommutingConfiguration,
    val groupTrains: Boolean,
)
