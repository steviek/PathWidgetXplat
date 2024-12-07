package com.sixbynine.transit.path.app.ui.home

import androidx.compose.ui.util.fastForEach
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.Stations.ChristopherStreet
import com.sixbynine.transit.path.api.Stations.FourteenthStreet
import com.sixbynine.transit.path.api.Stations.GroveStreet
import com.sixbynine.transit.path.api.Stations.Hoboken
import com.sixbynine.transit.path.api.Stations.Newport
import com.sixbynine.transit.path.api.Stations.NinthStreet
import com.sixbynine.transit.path.api.Stations.TwentyThirdStreet
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.TrainData

object TrainGrouper {
    fun groupTrains(station: Station, trains: List<TrainData>): List<List<TrainData>> {
        val groupedTrains = arrayListOf<ArrayList<TrainData>>()

        trains.fastForEach { train ->
            val updatedTrain = simplifyTitle(station, train)
            val group = groupedTrains.find { it.firstOrNull()?.title == updatedTrain.title }
            if (group == null) {
                groupedTrains += arrayListOf(updatedTrain)
            } else {
                group.add(updatedTrain)
            }
        }

        return groupedTrains
    }

    private fun simplifyTitle(station: Station, trainData: TrainData): TrainData {
        val newTitle = when (trainData.title) {
            "Journal Square via Hoboken" -> when (station) {
                Newport, GroveStreet, Hoboken -> "Journal Square"
                else -> null
            }

            "33rd Street via Hoboken" -> when (station) {
                ChristopherStreet, NinthStreet, FourteenthStreet, TwentyThirdStreet, Hoboken -> {
                    "33rd Street"
                }
                else -> null
            }

            else -> null
        }

        newTitle ?: return trainData

        return trainData.copy(title = newTitle)
    }
}
