package com.desaiwang.transit.path.api

import com.desaiwang.transit.path.app.ui.common.AppUiTrainData
import com.desaiwang.transit.path.preferences.IntPersistable
import com.desaiwang.transit.path.model.DepartureBoardData

enum class TrainFilter(override val number: Int) : IntPersistable {
    All(1), Interstate(2);

    companion object {
        fun matchesFilter(
            origin: Station,
            train: DepartureBoardData.TrainData,
            filter: TrainFilter
        ): Boolean {
            if (filter == All) return true
            val destination = Stations.fromHeadSign(train.title) ?: return true
            return matchesFilter(origin, destination, filter)
        }

        fun matchesFilter(
            origin: Station,
            train: AppUiTrainData,
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
                // Trains to/from WTC are always interstate.
                station == Stations.WorldTradeCenter ||
                        destination == Stations.WorldTradeCenter -> true
                // Newport -> Hoboken is the only time an NJ-terminating train travels east.
                destination == Stations.Hoboken -> station.isInNewYork
                station.isInNewYork -> destination.isInNewJersey || destination isWestOf station
                station.isInNewJersey -> destination.isInNewYork || destination isEastOf station
                else -> true // never happens, here for readability
            }
        }
    }
}


