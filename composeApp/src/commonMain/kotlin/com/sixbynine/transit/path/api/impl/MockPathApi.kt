package com.sixbynine.transit.path.api.impl

import com.sixbynine.transit.path.api.DepartureBoardTrain
import com.sixbynine.transit.path.api.Line.Hoboken33rd
import com.sixbynine.transit.path.api.Line.HobokenWtc
import com.sixbynine.transit.path.api.Line.NewarkWtc
import com.sixbynine.transit.path.api.PathApi
import com.sixbynine.transit.path.api.State.NewJersey
import com.sixbynine.transit.path.api.State.NewYork
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.app.ui.Colors
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.minutes

internal class MockPathApi : PathApi {

    override suspend fun fetchUpcomingDepartures(
    ): Result<Map<Station, List<DepartureBoardTrain>>> {
        val now = Clock.System.now()
        val stationsToDepartures = Stations.All.associateWith { station ->
            listOf(
                DepartureBoardTrain(
                    headsign = "World Trade Center",
                    projectedArrival = now + 2.minutes,
                    lineColors = Colors.HobWtc,
                    isDelayed = false,
                    backfillSource = null,
                    directionState = NewYork,
                    lines = setOf(HobokenWtc)
                ),
                DepartureBoardTrain(
                    headsign = "Newark",
                    projectedArrival = now + 4.minutes,
                    lineColors = Colors.NwkWtc,
                    isDelayed = false,
                    backfillSource = null,
                    directionState = NewJersey,
                    lines = setOf(NewarkWtc)
                ),
                DepartureBoardTrain(
                    headsign = "Hoboken",
                    projectedArrival = now + 7.minutes,
                    lineColors = Colors.Hob33s,
                    isDelayed = false,
                    backfillSource = null,
                    directionState = NewJersey,
                    lines = setOf(Hoboken33rd)
                )
            )
        }
        return Result.success(stationsToDepartures)
    }

    override fun getLastSuccessfulUpcomingDepartures(): Map<Station, List<DepartureBoardTrain>>? {
        return null
    }
}