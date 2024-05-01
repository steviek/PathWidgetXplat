package com.sixbynine.transit.path.api

class DepartureBoardTrainMap(
    private val stationToTrains: Map<String, List<DepartureBoardTrain>>,
) {
    fun getTrainsAt(station: Station): List<DepartureBoardTrain>? {
        return stationToTrains[station.pathApiName]
    }
}