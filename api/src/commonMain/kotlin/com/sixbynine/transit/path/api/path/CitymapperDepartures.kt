package com.sixbynine.transit.path.api.path

import com.sixbynine.transit.path.api.State.NewJersey
import com.sixbynine.transit.path.api.State.NewYork
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.path.PathRepository.PathDestination
import com.sixbynine.transit.path.api.path.PathRepository.PathDestinationMessage
import com.sixbynine.transit.path.api.path.PathRepository.PathServiceResult
import com.sixbynine.transit.path.api.path.PathRepository.PathServiceResults
import com.sixbynine.transit.path.api.state
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

/**
 * Realtime PATH departures from Citymapper's `metrodepartures` endpoint, converted into the
 * same [PathServiceResults] shape that `ridepath.json` used to return, so that everything
 * downstream (line computation, backfill, widgets) keeps working unchanged.
 *
 * Citymapper returns, per station, a list of departures. Near-term departures carry a live
 * countdown (`time_seconds`, relative to the response's `last_updated_time`); later ones only
 * carry a `scheduled_time`.
 */
internal object CitymapperDepartures {

    /** Citymapper station id -> PATH station code ([com.sixbynine.transit.path.api.Station.pathApiName]). */
    private val StationCodes = linkedMapOf(
        "RailStationNewarkPennStation" to "NWK",
        "PATHHarrison" to "HAR",
        "PATHJournalSquare" to "JSQ",
        "PATHGroveSt" to "GRV",
        "PATHExchangePlace" to "EXP",
        "PATHWorldTradeCenter" to "WTC",
        "PATHNewport" to "NEW",
        "PATHHoboken" to "HOB",
        "PATHChristopherSt" to "CHR",
        "PATH9St" to "09S",
        "PATH14thStreet" to "14S",
        "PATH23St" to "23S",
        "PATH33St" to "33S",
    )

    val Url: String =
        "https://citymapper.com/api/2/metrodepartures?headways=1&region_id=us-nyc&ids=" +
            StationCodes.keys.joinToString(",")

    /**
     * Citymapper gives the JSQ-33 via Hoboken route a single orange color. ridepath.json used
     * the blue/orange pair, which is what [com.sixbynine.transit.path.api.impl.TrainBackfillHelper]
     * keys on, so keep emitting that.
     */
    private const val ViaHobokenLineColor = "4D92FB,FF9900"

    fun toPathServiceResults(response: CitymapperResponse): PathServiceResults {
        val lastUpdated = response.lastUpdatedTime ?: return PathServiceResults(emptyList())
        val results = response.stations.mapNotNull { entry ->
            val stationCode =
                entry.station.liveCode?.let { StationCodes[it] } ?: return@mapNotNull null
            val routes = entry.routes.associateBy { it.liveLineCode }
            val destinations = entry.sections
                .flatMap { it.departureGroupings }
                .flatMap { it.departures }
                .mapNotNull { it.toLabeledMessage(routes, lastUpdated) }
                .sortedBy { (_, message) -> message.secondsToArrival.toLong() }
                .groupBy(keySelector = { it.first }, valueTransform = { it.second })
                .map { (label, messages) -> PathDestination(label, messages) }
            PathServiceResult(consideredStation = stationCode, destinations = destinations)
        }
        return PathServiceResults(results)
    }

    private fun CitymapperDeparture.toLabeledMessage(
        routes: Map<String?, CitymapperRoute>,
        lastUpdated: Instant,
    ): Pair<String, PathDestinationMessage>? {
        if (isAdvisoryMessage == 1) return null // e.g. "No current departures"
        // Newark Penn is shared with NJ Transit light rail; only keep PATH trains.
        val routeId = routeId?.takeIf { it.startsWith("PATH") } ?: return null
        val destinationName = destinationName?.trim().orEmpty()
        val destination = Stations.fromHeadSign(destinationName) ?: return null

        val departureTime = when {
            timeSeconds != null -> lastUpdated + timeSeconds.seconds
            scheduledTime != null -> scheduledTime
            else -> return null
        }
        val secondsToArrival = (departureTime - lastUpdated).inWholeSeconds

        val route = routes[routeId]
        val isViaHoboken = route?.longName?.contains("via hoboken", ignoreCase = true) == true
        val headSign = if (isViaHoboken && destination != Stations.Hoboken) {
            "$destinationName via Hoboken"
        } else {
            destinationName
        }
        val lineColor = when {
            isViaHoboken -> ViaHobokenLineColor
            else -> route?.color?.removePrefix("#")?.uppercase() ?: return null
        }
        val label = when (destination.state) {
            NewYork -> "ToNY"
            NewJersey -> "ToNJ"
        }

        return label to PathDestinationMessage(
            target = destination.pathApiName,
            secondsToArrival = secondsToArrival.toString(),
            arrivalTimeMessage = "${(secondsToArrival / 60).coerceAtLeast(0)} min",
            lineColor = lineColor,
            headSign = headSign,
            lastUpdated = lastUpdated,
        )
    }
}

@Serializable
internal data class CitymapperResponse(
    val stations: List<CitymapperStationEntry> = emptyList(),
    @SerialName("last_updated_time") val lastUpdatedTime: Instant? = null,
)

@Serializable
internal data class CitymapperStationEntry(
    val station: CitymapperStation,
    val sections: List<CitymapperSection> = emptyList(),
    val routes: List<CitymapperRoute> = emptyList(),
)

@Serializable
internal data class CitymapperStation(
    @SerialName("live_code") val liveCode: String? = null,
)

@Serializable
internal data class CitymapperSection(
    @SerialName("departure_groupings")
    val departureGroupings: List<CitymapperDepartureGrouping> = emptyList(),
)

@Serializable
internal data class CitymapperDepartureGrouping(
    val departures: List<CitymapperDeparture> = emptyList(),
)

@Serializable
internal data class CitymapperDeparture(
    @SerialName("destination_name") val destinationName: String? = null,
    @SerialName("route_id") val routeId: String? = null,
    @SerialName("time_seconds") val timeSeconds: Double? = null,
    @SerialName("scheduled_time") val scheduledTime: Instant? = null,
    @SerialName("is_advisory_message") val isAdvisoryMessage: Int? = null,
)

@Serializable
internal data class CitymapperRoute(
    @SerialName("live_line_code") val liveLineCode: String? = null,
    val color: String? = null,
    @SerialName("long_name") val longName: String? = null,
)
