package com.sixbynine.transit.path.api.impl

import com.sixbynine.transit.path.api.DepartingTrain
import com.sixbynine.transit.path.api.PathApi
import com.sixbynine.transit.path.api.State.NewJersey
import com.sixbynine.transit.path.api.State.NewYork
import com.sixbynine.transit.path.api.UpcomingDepartures
import com.sixbynine.transit.path.api.path.PathRepository
import com.sixbynine.transit.path.api.path.PathRepository.PathServiceResults
import com.sixbynine.transit.path.model.ColorWrapper
import com.sixbynine.transit.path.model.Colors
import com.sixbynine.transit.path.util.FetchWithPrevious
import com.sixbynine.transit.path.util.Staleness
import com.sixbynine.transit.path.util.map
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.seconds

internal class PathApiImpl : PathApi {

    override fun getUpcomingDepartures(
        now: Instant,
        staleness: Staleness
    ): FetchWithPrevious<UpcomingDepartures> {
        return PathRepository.getResults(now, staleness).map { resultsToMap(now, it) }
    }

    private fun resultsToMap(
        now: Instant,
        results: PathServiceResults
    ): UpcomingDepartures {
        return results.results.associate { result ->
            val trains = result.destinations
                .flatMap { destination ->
                    destination.messages.mapNotNull {
                        val rawArrivalTime = it.lastUpdated + it.durationToArrival
                        if (rawArrivalTime < now - 30.seconds) return@mapNotNull null
                        val directionState = when (destination.label) {
                            "ToNJ" -> NewJersey
                            "ToNY" -> NewYork
                            else -> null
                        }
                        val colors = it.lineColor.split(",").map(Colors::parse).map(::ColorWrapper)
                        DepartingTrain(
                            headsign = it.headSign,
                            projectedArrival = rawArrivalTime.coerceAtLeast(now),
                            lineColors = colors,
                            isDelayed = it.arrivalTimeMessage == "Delayed",
                            backfillSource = null,
                            directionState = directionState,
                            lines = LineComputer.computeLines(
                                station = result.consideredStation,
                                target = it.target,
                                colors = colors,
                            )
                        )
                    }
                }

            result.consideredStation to trains
        }
            .let { TrainBackfillHelper.withBackfill(it) }
            .let { UpcomingDepartures(it, scheduleName = null) }
    }
}
