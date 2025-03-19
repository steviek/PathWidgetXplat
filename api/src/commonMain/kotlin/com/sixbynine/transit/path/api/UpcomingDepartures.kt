package com.sixbynine.transit.path.api

class UpcomingDepartures(
    private val stationToTrains: Map<String, List<DepartingTrain>>,
    val scheduleName: String?,
) {
    fun getTrainsAt(station: Station): List<DepartingTrain>? {
        return stationToTrains[station.pathApiName]
    }
}