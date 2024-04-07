package com.sixbynine.transit.path.api

import com.sixbynine.transit.path.api.impl.PathApiImpl

interface PathApi {
    suspend fun fetchUpcomingDepartures(
        force: Boolean = false,
    ): Result<Map<String, List<DepartureBoardTrain>>>

    suspend fun getLastSuccessfulUpcomingDepartures(): Map<String, List<DepartureBoardTrain>>?

    companion object {
        val instance: PathApi = PathApiImpl()
    }
}
