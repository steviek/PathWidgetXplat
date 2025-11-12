package com.sixbynine.transit.path.api

import com.sixbynine.transit.path.api.impl.PathApiImpl
import com.sixbynine.transit.path.util.FetchWithPrevious
import kotlinx.datetime.Instant

interface PathApi {

    fun getUpcomingDepartures(
        now: Instant,
    ): FetchWithPrevious<UpcomingDepartures>

    companion object {
        val instance: PathApi = PathApiImpl()
    }
}
