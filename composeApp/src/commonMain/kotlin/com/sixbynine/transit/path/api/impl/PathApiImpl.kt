package com.sixbynine.transit.path.api.impl

import com.sixbynine.transit.path.api.DepartureBoardTrain
import com.sixbynine.transit.path.api.NetworkException
import com.sixbynine.transit.path.api.PathApi
import com.sixbynine.transit.path.api.State
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.app.ui.ColorWrapper
import com.sixbynine.transit.path.app.ui.Colors
import com.sixbynine.transit.path.util.JsonFormat
import com.sixbynine.transit.path.util.suspendRunCatching
import com.sixbynine.transit.path.widget.widgetDataStore
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

internal class PathApiImpl : PathApi {
    private val client = PathClient()

    override suspend fun fetchUpcomingDepartures():
            Result<Map<Station, List<DepartureBoardTrain>>> {
        return client.getResults()
            .mapCatching { results ->
                resultsToMap(results)
            }
    }

    private fun resultsToMap(
        results: PathClient.PathServiceResults
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

    override fun getLastSuccessfulUpcomingDepartures(): Map<Station, List<DepartureBoardTrain>>? {
        val lastResponse = client.getLastSuccessfulResults() ?: return null
        return runCatching { resultsToMap(lastResponse) }.getOrNull()
    }
}

class PathClient {
    private val httpClient = HttpClient()

    suspend fun getResults(): Result<PathServiceResults> {
        return suspendRunCatching {
            withTimeout(5.seconds) {
                val response =
                    httpClient.get("https://www.panynj.gov/bin/portauthority/ridepath.json")
                if (response.status.isSuccess()) {
                    val responseText = response.bodyAsText()
                    widgetDataStore()["last_success"] = responseText
                    JsonFormat.decodeFromString<PathServiceResults>(responseText)
                } else {
                    throw NetworkException(response.status.toString())
                }
            }
        }
    }

    fun getLastSuccessfulResults(): PathServiceResults? {
        return runCatching {
            val lastResponse = widgetDataStore()["last_success"] ?: return null
            JsonFormat.decodeFromString<PathServiceResults>(lastResponse)
        }.getOrNull()
    }

    @Serializable
    data class PathServiceResults(val results: List<PathServiceResult>)

    @Serializable
    data class PathServiceResult(
        val consideredStation: String,
        val destinations: List<PathDestination>
    )

    @Serializable
    data class PathDestination(
        val label: String,
        val messages: List<PathDestinationMessage>
    )

    @Serializable
    data class PathDestinationMessage(
        val target: String,
        val secondsToArrival: String,
        val arrivalTimeMessage: String?,
        val lineColor: String,
        val headSign: String,
        val lastUpdated: Instant
    ) {
        val durationToArrival get() = secondsToArrival.toInt().seconds
    }
}