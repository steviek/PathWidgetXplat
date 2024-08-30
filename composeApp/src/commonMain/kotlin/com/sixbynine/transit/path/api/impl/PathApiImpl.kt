package com.sixbynine.transit.path.api.impl

import com.sixbynine.transit.path.api.DepartureBoardTrain
import com.sixbynine.transit.path.api.DepartureBoardTrainMap
import com.sixbynine.transit.path.api.PathApi
import com.sixbynine.transit.path.api.State.NewJersey
import com.sixbynine.transit.path.api.State.NewYork
import com.sixbynine.transit.path.api.path.PathRepository
import com.sixbynine.transit.path.api.path.PathRepository.PathServiceResults
import com.sixbynine.transit.path.app.ui.ColorWrapper
import com.sixbynine.transit.path.app.ui.Colors
import com.sixbynine.transit.path.util.FetchWithPrevious
import com.sixbynine.transit.path.util.Staleness
import com.sixbynine.transit.path.util.map
import kotlinx.datetime.Instant

internal class PathApiImpl : PathApi {

    override fun getUpcomingDepartures(
        now: Instant,
        staleness: Staleness
    ): FetchWithPrevious<DepartureBoardTrainMap> {
        return PathRepository.getResults(now, staleness).map { resultsToMap(now, it) }
    }

    private fun resultsToMap(
        now: Instant,
        results: PathServiceResults
    ): DepartureBoardTrainMap {
        return results.results.associate { result ->
            val trains = result.destinations
                .flatMap { destination ->
                    destination.messages.map {
                        val directionState = when (destination.label) {
                            "ToNJ" -> NewJersey
                            "ToNY" -> NewYork
                            else -> null
                        }
                        val colors = it.lineColor.split(",").map(Colors::parse).map(::ColorWrapper)
                        DepartureBoardTrain(
                            headsign = it.headSign,
                            projectedArrival = (it.lastUpdated + it.durationToArrival)
                                .coerceAtLeast(now),
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
            .let { DepartureBoardTrainMap(it, scheduleName = null) }
    }
}
