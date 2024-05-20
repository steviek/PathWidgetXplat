package com.sixbynine.transit.path.api

import com.sixbynine.transit.path.api.impl.PathApiImpl
import com.sixbynine.transit.path.util.FetchWithPrevious
import com.sixbynine.transit.path.util.Staleness
import kotlinx.datetime.Instant

interface PathApi {

    fun getUpcomingDepartures(
        now: Instant,
        staleness: Staleness,
    ): FetchWithPrevious<DepartureBoardTrainMap>

    companion object {
        val instance: PathApi = PathApiImpl()
    }
}
