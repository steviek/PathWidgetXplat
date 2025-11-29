package com.sixbynine.transit.path.widget

import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.StationChoice
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.TrainFilter

sealed interface PathWidgetConfiguration {
    val stationLimit: Int

    data class DepartureBoard(
        override val stationLimit: Int,
        val stationChoices: List<StationChoice>,
        val lines: Collection<Line>,
        val sort: StationSort,
        val filter: TrainFilter,
    ) : PathWidgetConfiguration

    data class Commute(
        val origin: StationChoice,
        val destination: StationChoice,
    ) : PathWidgetConfiguration {
        override val stationLimit: Int = 1
    }

    companion object {
        fun allData(
            includeClosestStation: Boolean,
            sort: StationSort = StationSort.Alphabetical
        ): DepartureBoard {
            return DepartureBoard(
                stationLimit = Int.MAX_VALUE,
                stationChoices = buildList {
                    if (includeClosestStation) add(StationChoice.Closest)
                    Stations.All.forEach { add(StationChoice.Fixed(it)) }
                },
                lines = Line.entries,
                sort = StationSort.Alphabetical,
                filter = TrainFilter.All,
            )
        }
    }
}
