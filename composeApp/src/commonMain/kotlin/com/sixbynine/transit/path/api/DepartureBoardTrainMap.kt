package com.sixbynine.transit.path.api

class DepartureBoardTrainMap(
    private val stationToTrains: Map<String, List<DepartureBoardTrain>>,
    val scheduleName: String?,
) {
    fun getTrainsAt(station: Station): List<DepartureBoardTrain>? {
        return stationToTrains[station.pathApiName]
    }
}