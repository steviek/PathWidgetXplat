package com.sixbynine.transit.path.api.impl

import com.sixbynine.transit.path.api.DepartureBoardTrain
import com.sixbynine.transit.path.api.PathApi
import com.sixbynine.transit.path.api.State
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.path.PathRepository
import com.sixbynine.transit.path.api.path.PathRepository.PathServiceResults
import com.sixbynine.transit.path.app.ui.ColorWrapper
import com.sixbynine.transit.path.app.ui.Colors
import com.sixbynine.transit.path.util.suspendRunCatching
import kotlinx.datetime.Clock

internal class PathApiImpl : PathApi {

    override suspend fun fetchUpcomingDepartures(
        force: Boolean
    ): Result<Map<Station, List<DepartureBoardTrain>>> {
        return PathRepository.getResults(force).mapCatching { results -> resultsToMap(results) }
    }

    override suspend fun getLastSuccessfulUpcomingDepartures(): Map<Station, List<DepartureBoardTrain>>? {
        val cachedResults = PathRepository.getCachedResults() ?: return null
        return suspendRunCatching { resultsToMap(cachedResults) }.getOrNull()
    }

    private fun resultsToMap(
        results: PathServiceResults
    ): Map<Station, List<DepartureBoardTrain>> {
        val stationsToCheck = Stations.All.associateBy { it.pathApiName }
        return results.results.mapNotNull { result ->
            val station =
                stationsToCheck[result.consideredStation] ?: return@mapNotNull null

            val trains = result.destinations
                .flatMap { destination ->
                    destination.messages.map {
                        val directionState = when (destination.label) {
                            "ToNJ" -> State.NewJersey
                            "ToNY" -> State.NewYork
                            else -> null
                        }
                        val colors = it.lineColor.split(",").map(Colors::parse).map(::ColorWrapper)
                        DepartureBoardTrain(
                            headsign = it.headSign,
                            projectedArrival = (it.lastUpdated + it.durationToArrival)
                                .coerceAtLeast(Clock.System.now()),
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

            station to trains
        }
            .toMap()
            .let { TrainBackfillHelper.withBackfill(it) }
    }
}
