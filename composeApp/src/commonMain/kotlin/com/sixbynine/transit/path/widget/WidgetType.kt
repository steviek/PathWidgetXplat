package com.sixbynine.transit.path.widget

import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.TrainFilter

sealed interface WidgetType {
    val stationLimit: Int

    data class DepartureBoard(
        override val stationLimit: Int,
        val stations: List<Station>,
        val lines: Collection<Line>,
        val sort: StationSort,
        val filter: TrainFilter,
    ) : WidgetType

    data class Commute(
        val origin: Station,
        val destination: Station
    ) : WidgetType {
        override val stationLimit: Int = 1
    }
}
