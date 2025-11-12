package com.sixbynine.transit.path.api.path

import com.sixbynine.transit.path.util.FetchWithPrevious
import com.sixbynine.transit.path.util.RemoteFileRepository
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

object PathRepository {

    private val helper = RemoteFileRepository(
        keyPrefix = "path_data",
        url = "https://www.panynj.gov/bin/portauthority/ridepath.json",
        serializer = PathServiceResults.serializer(),
        maxAge = 30.seconds
    )

    fun getResults(now: Instant): FetchWithPrevious<PathServiceResults> {
        return helper.get(now)
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
