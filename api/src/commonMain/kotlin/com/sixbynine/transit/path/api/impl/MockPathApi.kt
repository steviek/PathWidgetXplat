package com.sixbynine.transit.path.api.impl

import com.sixbynine.transit.path.api.DepartingTrain
import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.Line.Hoboken33rd
import com.sixbynine.transit.path.api.Line.HobokenWtc
import com.sixbynine.transit.path.api.Line.NewarkWtc
import com.sixbynine.transit.path.api.PathApi
import com.sixbynine.transit.path.api.State.NewJersey
import com.sixbynine.transit.path.api.State.NewYork
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.UpcomingDepartures
import com.sixbynine.transit.path.model.Colors
import com.sixbynine.transit.path.util.AgedValue
import com.sixbynine.transit.path.util.FetchWithPrevious
import com.sixbynine.transit.path.util.Staleness
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal class MockPathApi : PathApi {

    override fun getUpcomingDepartures(
        now: Instant,
        staleness: Staleness
    ): FetchWithPrevious<UpcomingDepartures> {
        val stationsToDepartures = Stations.All.associateWith { station ->
            listOf(
                DepartingTrain(
                    headsign = "World Trade Center",
                    projectedArrival = now + 2.minutes,
                    lineColors = Colors.HobWtc,
                    isDelayed = false,
                    backfillSource = null,
                    directionState = NewYork,
                    lines = setOf(HobokenWtc)
                ),
                DepartingTrain(
                    headsign = "Newark",
                    projectedArrival = now + 4.minutes,
                    lineColors = Colors.NwkWtc,
                    isDelayed = false,
                    backfillSource = null,
                    directionState = NewJersey,
                    lines = setOf(NewarkWtc)
                ),
                DepartingTrain(
                    headsign = "Hoboken",
                    projectedArrival = now + 7.minutes,
                    lineColors = Colors.Hob33s,
                    isDelayed = false,
                    backfillSource = null,
                    directionState = NewJersey,
                    lines = setOf(Hoboken33rd)
                ),
                DepartingTrain(
                    headsign = "33rd St",
                    projectedArrival = now + 4.minutes,
                    lineColors = Colors.HobWtc,
                    isDelayed = false,
                    backfillSource = null,
                    directionState = NewYork,
                    lines = Line.permanentLinesForWtc33rd.toSet()
                ),
            )
        }.mapKeys { it.key.pathApiName }
        return FetchWithPrevious(
            AgedValue(
                0.seconds,
                UpcomingDepartures(stationsToDepartures, scheduleName = null)
            )
        )
    }
}
