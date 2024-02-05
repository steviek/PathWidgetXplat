package com.sixbynine.transit.path.api.impl

import androidx.compose.ui.graphics.Color
import com.sixbynine.transit.path.api.DepartureBoardTrain
import com.sixbynine.transit.path.api.PathApi
import com.sixbynine.transit.path.api.State.NewJersey
import com.sixbynine.transit.path.api.State.NewYork
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.Stations
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.minutes

internal class MockPathApi : PathApi {

    override suspend fun fetchUpcomingDepartures(
    ): Result<Map<Station, List<DepartureBoardTrain>>> {
        val now = Clock.System.now()
        val stationsToDepartures = Stations.All.associateWith { station ->
            listOf(
                DepartureBoardTrain(
                    headsign = "Hoboken",
                    projectedArrival = now + 2.minutes,
                    lineColors = listOf(Color.Green),
                    isDelayed = false,
                    backfillSource = null,
                    directionState = NewJersey
                ),
                DepartureBoardTrain(
                    headsign = "Newark",
                    projectedArrival = now + 4.minutes,
                    lineColors = listOf(Color.Red),
                    isDelayed = false,
                    backfillSource = null,
                    directionState = NewJersey
                ),
                DepartureBoardTrain(
                    headsign = "World Trade Center",
                    projectedArrival = now + 7.minutes,
                    lineColors = listOf(Color.Blue),
                    isDelayed = false,
                    backfillSource = null,
                    directionState = NewYork
                )
            )
        }
        return Result.success(stationsToDepartures)
    }

    override fun getLastSuccessfulUpcomingDepartures(): Map<Station, List<DepartureBoardTrain>>? {
        return null
    }
}