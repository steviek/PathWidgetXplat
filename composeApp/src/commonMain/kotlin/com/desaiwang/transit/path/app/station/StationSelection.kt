package com.desaiwang.transit.path.app.station

import com.desaiwang.transit.path.api.Station

data class StationSelection(
    val selectedStations: List<Station>,
    val unselectedStations: List<Station>
)
