package com.sixbynine.transit.path.api

import com.sixbynine.transit.path.api.impl.PathApiImpl

interface PathApi {
    suspend fun fetchUpcomingDepartures(
        force: Boolean = false,
    ): Result<Map<Station, List<DepartureBoardTrain>>>

    suspend fun getLastSuccessfulUpcomingDepartures(): Map<Station, List<DepartureBoardTrain>>?

    companion object {
        val instance: PathApi = PathApiImpl()
    }
}
