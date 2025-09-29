package com.desaiwang.transit.path.api

import com.desaiwang.transit.path.api.impl.PathApiImpl
import com.desaiwang.transit.path.util.FetchWithPrevious
import com.desaiwang.transit.path.util.Staleness
import kotlinx.datetime.Instant

interface PathApi {

    fun getUpcomingDepartures(
        now: Instant,
        staleness: Staleness,
    ): FetchWithPrevious<UpcomingDepartures>

    companion object {
        val instance: PathApi = PathApiImpl()
    }
}
