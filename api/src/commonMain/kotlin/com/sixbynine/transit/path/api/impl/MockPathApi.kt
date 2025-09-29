package com.desaiwang.transit.path.api.impl

import com.desaiwang.transit.path.api.DepartingTrain
import com.desaiwang.transit.path.api.Line
import com.desaiwang.transit.path.api.Line.Hoboken33rd
import com.desaiwang.transit.path.api.Line.HobokenWtc
import com.desaiwang.transit.path.api.Line.NewarkWtc
import com.desaiwang.transit.path.api.PathApi
import com.desaiwang.transit.path.api.State.NewJersey
import com.desaiwang.transit.path.api.State.NewYork
import com.desaiwang.transit.path.api.Stations
import com.desaiwang.transit.path.api.Stations.ChristopherStreet
import com.desaiwang.transit.path.api.Stations.ExchangePlace
import com.desaiwang.transit.path.api.Stations.FourteenthStreet
import com.desaiwang.transit.path.api.Stations.GroveStreet
import com.desaiwang.transit.path.api.Stations.Harrison
import com.desaiwang.transit.path.api.Stations.Hoboken
import com.desaiwang.transit.path.api.Stations.JournalSquare
import com.desaiwang.transit.path.api.Stations.Newport
import com.desaiwang.transit.path.api.Stations.NinthStreet
import com.desaiwang.transit.path.api.Stations.ThirtyThirdStreet
import com.desaiwang.transit.path.api.Stations.TwentyThirdStreet
import com.desaiwang.transit.path.api.Stations.WorldTradeCenter
import com.desaiwang.transit.path.api.UpcomingDepartures
import com.desaiwang.transit.path.model.Colors
import com.desaiwang.transit.path.util.AgedValue
import com.desaiwang.transit.path.util.FetchWithPrevious
import com.desaiwang.transit.path.util.Staleness
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Mock implementation of PATH API for testing and development.
 * 
 * This class:
 * 1. Provides realistic but static train departure data
 * 2. Simulates trains on all major PATH lines
 * 3. Uses fixed time offsets from the current time
 * 4. Helps test the app without hitting real PATH API
 */
internal class MockPathApi : PathApi {

    override fun getUpcomingDepartures(
        now: Instant,
        staleness: Staleness
    ): FetchWithPrevious<UpcomingDepartures> {
        val stationsToDepartures = Stations.All.associateWith { station ->
            listOfNotNull(
                DepartingTrain(
                    headsign = "World Trade Center",
                    projectedArrival = now + 2.minutes,
                    lineColors = Colors.HobWtc,
                    isDelayed = false,
                    backfillSource = null,
                    directionState = NewYork,
                    lines = setOf(HobokenWtc)
                ).takeIf {
                    station in listOf(
                        Hoboken,
                        ExchangePlace,
                        Newport,
                    )
                },
                DepartingTrain(
                    headsign = "Newark",
                    projectedArrival = now + 4.minutes,
                    lineColors = Colors.NwkWtc,
                    isDelayed = false,
                    backfillSource = null,
                    directionState = NewJersey,
                    lines = setOf(NewarkWtc)
                ).takeIf {
                    station in listOf(
                        WorldTradeCenter,
                        ExchangePlace,
                        GroveStreet,
                        JournalSquare,
                        Harrison
                    )
                },
                DepartingTrain(
                    headsign = "Hoboken",
                    projectedArrival = now + 7.minutes,
                    lineColors = Colors.Hob33s,
                    isDelayed = false,
                    backfillSource = null,
                    directionState = NewJersey,
                    lines = setOf(Hoboken33rd)
                ).takeIf {
                    station in listOf(
                        ChristopherStreet,
                        NinthStreet,
                        FourteenthStreet,
                        TwentyThirdStreet,
                        ThirtyThirdStreet
                    )
                },
                DepartingTrain(
                    headsign = "Hoboken",
                    projectedArrival = now + 10.minutes,
                    lineColors = Colors.HobWtc,
                    isDelayed = false,
                    backfillSource = null,
                    directionState = NewJersey,
                    lines = setOf(HobokenWtc)
                ).takeIf { station in listOf(Newport, WorldTradeCenter, ExchangePlace) },
                DepartingTrain(
                    headsign = "World Trade Center",
                    projectedArrival = now + 12.minutes,
                    lineColors = Colors.HobWtc,
                    isDelayed = false,
                    backfillSource = null,
                    directionState = NewYork,
                    lines = setOf(HobokenWtc)
                ).takeIf { station in listOf(Newport, Hoboken, ExchangePlace) },
                DepartingTrain(
                    headsign = "33rd St",
                    projectedArrival = now + 4.minutes,
                    lineColors = Colors.HobWtc,
                    isDelayed = false,
                    backfillSource = null,
                    directionState = NewYork,
                    lines = Line.permanentLinesForWtc33rd.toSet()
                ).takeIf {
                    station in listOf(
                        Newport,
                        WorldTradeCenter,
                        ExchangePlace,
                        NinthStreet,
                        ChristopherStreet,
                        FourteenthStreet,
                        TwentyThirdStreet,
                    )
                },
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
