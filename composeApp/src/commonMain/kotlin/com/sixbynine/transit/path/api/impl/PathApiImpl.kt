package com.sixbynine.transit.path.api.impl

import com.sixbynine.transit.path.api.DepartureBoardTrain
import com.sixbynine.transit.path.api.NetworkException
import com.sixbynine.transit.path.api.PathApi
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.ui.Colors
import com.sixbynine.transit.path.util.runCatchingSuspend
import com.sixbynine.transit.path.widget.widgetDataStore
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

internal class PathApiImpl : PathApi {
    private val client = PathClient()

    override suspend fun fetchUpcomingDepartures():
            Result<Map<Station, List<DepartureBoardTrain>>> {
        val stationsToCheck = Stations.All.associateBy { it.pathApiName }
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
                .flatMap { it.messages }
                .map {
                    DepartureBoardTrain(
                        headsign = it.headSign,
                        projectedArrival = (it.lastUpdated + it.durationToArrival)
                            .coerceAtLeast(Clock.System.now()),
                        lineColors = it.lineColor.split(",")
                            .map { Colors.parse(it) }
                    )
                }

            station to trains
        }
            .toMap()
    }

    override fun getLastSuccessfulUpcomingDepartures(): Map<Station, List<DepartureBoardTrain>>? {
        val lastResponse = client.getLastSuccessfulResults() ?: return null
        return runCatching { resultsToMap(lastResponse) }.getOrNull()
    }
}

private class PathClient {
    @OptIn(ExperimentalSerializationApi::class)
    private val jsonFormat = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
    private val httpClient = HttpClient()

    suspend fun getResults(): Result<PathServiceResults> {
        return runCatchingSuspend {
            httpClient.get("https://www.panynj.gov/bin/portauthority/ridepath.json")
        }
            .mapCatching { response ->
                if (response.status.isSuccess()) {
                    val responseText = response.bodyAsText()
                    widgetDataStore()["last_success"] = responseText
                    jsonFormat.decodeFromString<PathServiceResults>(responseText)
                } else {
                    return Result.failure(NetworkException(response.status.toString()))
                }
            }
    }

    fun getLastSuccessfulResults(): PathServiceResults? {
        return runCatching {
            val lastResponse = widgetDataStore()["last_success"] ?: return null
            jsonFormat.decodeFromString<PathServiceResults>(lastResponse)
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
        val lineColor: String,
        val headSign: String,
        val lastUpdated: Instant
    ) {
        val durationToArrival get() = secondsToArrival.toInt().seconds
    }
}