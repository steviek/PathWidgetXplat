package com.sixbynine.transit.path.api

import com.sixbynine.transit.path.api.impl.PathApiImpl

interface PathApi {
    suspend fun fetchUpcomingDepartures(): Result<Map<Station, List<DepartureBoardTrain>>>

    fun getLastSuccessfulUpcomingDepartures(): Map<Station, List<DepartureBoardTrain>>?

    companion object {
        val instance: PathApi = PathApiImpl()
    }
}
