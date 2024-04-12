package com.sixbynine.transit.path.api

import com.sixbynine.transit.path.api.impl.PathApiImpl
import kotlinx.datetime.Instant

interface PathApi {
    suspend fun fetchUpcomingDepartures(
        now: Instant,
        force: Boolean = false,
    ): Result<Map<String, List<DepartureBoardTrain>>>

    suspend fun getLastSuccessfulUpcomingDepartures(
        now: Instant,
    ): Map<String, List<DepartureBoardTrain>>?

    companion object {
        val instance: PathApi = PathApiImpl()
    }
}
