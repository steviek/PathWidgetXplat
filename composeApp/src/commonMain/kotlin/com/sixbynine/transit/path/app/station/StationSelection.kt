package com.sixbynine.transit.path.app.station

import com.sixbynine.transit.path.api.Station

data class StationSelection(
    val selectedStations: List<Station>,
    val unselectedStations: List<Station>
)
