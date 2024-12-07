package com.sixbynine.transit.path.api

import com.sixbynine.transit.path.app.ui.home.HomeScreenContract
import com.sixbynine.transit.path.preferences.IntPersistable
import com.sixbynine.transit.path.widget.WidgetData

enum class TrainFilter(override val number: Int) : IntPersistable {
    All(1), Interstate(2);

    companion object {
        fun matchesFilter(
            origin: Station,
            train: WidgetData.TrainData,
            filter: TrainFilter
        ): Boolean {
            if (filter == All) return true
            val destination = Stations.fromHeadSign(train.title) ?: return true
            return matchesFilter(origin, destination, filter)
        }

        fun matchesFilter(
            origin: Station,
            train: HomeScreenContract.TrainData,
            filter: TrainFilter
        ): Boolean {
            if (filter == All) return true
            val destination = Stations.fromHeadSign(train.title) ?: return true
            return matchesFilter(origin, destination, filter)
        }

        fun matchesFilter(
            station: Station,
            destination: Station,
            filter: TrainFilter
        ): Boolean {
            if (filter == All) return true

            return when {
                // Newport -> Hoboken is the only time an NJ-terminating train travels east.
                destination == Stations.Hoboken -> station.isInNewYork
                station.isInNewYork -> destination.isInNewJersey || destination isWestOf station
                station.isInNewJersey -> destination.isInNewYork || destination isEastOf station
                else -> true // never happens, here for readability
            }
        }
    }
}


