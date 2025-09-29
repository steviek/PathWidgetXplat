package com.desaiwang.transit.path.api.impl

import com.desaiwang.transit.path.api.DepartingTrain
import com.desaiwang.transit.path.api.PathApi
import com.desaiwang.transit.path.api.State.NewJersey
import com.desaiwang.transit.path.api.State.NewYork
import com.desaiwang.transit.path.api.UpcomingDepartures
import com.desaiwang.transit.path.api.path.PathRepository
import com.desaiwang.transit.path.api.path.PathRepository.PathServiceResults
import com.desaiwang.transit.path.model.ColorWrapper
import com.desaiwang.transit.path.model.Colors
import com.desaiwang.transit.path.util.FetchWithPrevious
import com.desaiwang.transit.path.util.Staleness
import com.desaiwang.transit.path.util.map
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.seconds

/**
 * Main implementation of the PATH train API that fetches real-time train departures.
 * 
 * This class:
 * 1. Fetches raw train data from the PATH API
 * 2. Processes arrival times and train statuses
 * 3. Computes train lines based on colors and destinations
 * 4. Handles train delays and direction information
 */
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
