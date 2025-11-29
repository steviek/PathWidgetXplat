package com.sixbynine.transit.path.api

import com.sixbynine.transit.path.app.ui.common.AppUiTrainData
import com.sixbynine.transit.path.model.DepartureBoardData
import com.sixbynine.transit.path.preferences.IntPersistable

sealed interface TrainFilter {
    fun matchesFilter(
        origin: Station,
        train: DepartureBoardData.TrainData,
    ): Boolean
}

data class CommuteTrainFilter(val origin: Station, val destination: Station) : TrainFilter {
    override fun matchesFilter(
        origin: Station,
        train: DepartureBoardData.TrainData
    ): Boolean {
        TODO("Not yet implemented")
    }
}


enum class DepartureBoardTrainFilter(override val number: Int) : IntPersistable, TrainFilter {
    All(1), Interstate(2);

    override fun matchesFilter(origin: Station, train: DepartureBoardData.TrainData): Boolean {
        if (this == All) return true
        val destination = Stations.fromHeadSign(train.title) ?: return true
        return matchesFilter(origin, destination, this)
    }

    companion object Companion {
        fun matchesFilter(
            origin: Station,
            train: DepartureBoardData.TrainData,
            filter: DepartureBoardTrainFilter
        ): Boolean {
           return filter.matchesFilter(origin, train)
        }

        // Change these too
        fun matchesFilter(
            origin: Station,
            train: AppUiTrainData,
            filter: DepartureBoardTrainFilter
        ): Boolean {
            if (filter == All) return true
            val destination = Stations.fromHeadSign(train.title) ?: return true
            return matchesFilter(origin, destination, filter)
        }

        fun matchesFilter(
            station: Station,
            destination: Station,
            filter: DepartureBoardTrainFilter
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


