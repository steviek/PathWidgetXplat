package com.desaiwang.transit.path.api

import com.desaiwang.transit.path.location.Location
import kotlinx.serialization.Serializable

@Serializable
data class Station(
    val pathApiName: String,
    val displayName: String,
    val coordinates: Coordinates
)

object Stations {
    fun fromHeadSign(headSign: String): Station? = when {
        "World Trade" in headSign -> WorldTradeCenter
        headSign == "Newark" -> Newark
        "33" in headSign -> ThirtyThirdStreet
        headSign == "Hoboken" -> Hoboken
        "Journal" in headSign -> JournalSquare
        "Exchange" in headSign -> ExchangePlace
        "Grove" in headSign -> GroveStreet
        "Harrison" in headSign -> Harrison
        else -> null
    }

    val Newark = Station(
        pathApiName = "NWK",
        displayName = "Newark",
        coordinates = Coordinates(40.73454, -74.16375)
    )

    val Harrison = Station(
        pathApiName = "HAR",
        displayName = "Harrison",
        coordinates = Coordinates(40.73942, -74.15587)
    )

    val JournalSquare = Station(
        pathApiName = "JSQ",
        displayName = "Journal Square",
        coordinates = Coordinates(40.73301, -74.06289)
    )


    val GroveStreet = Station(
        pathApiName = "GRV",
        displayName = "Grove Street",
        coordinates = Coordinates(40.71966, -74.04245)
    )


    val ExchangePlace = Station(
        pathApiName = "EXP",
        displayName = "Exchange Place",
        coordinates = Coordinates(40.71676, -74.03238)
    )

    val WorldTradeCenter = Station(
        pathApiName = "WTC",
        displayName = "World Trade Center",
        coordinates = Coordinates(40.71271, -74.01193)
    )

    val Newport = Station(
        pathApiName = "NEW",
        displayName = "Newport",
        coordinates = Coordinates(40.72699, -74.03383)
    )

    val Hoboken = Station(
        pathApiName = "HOB",
        displayName = "Hoboken",
        coordinates = Coordinates(40.73586, -74.02922)
    )

    val ChristopherStreet = Station(
        pathApiName = "CHR",
        displayName = "Christopher Street",
        coordinates = Coordinates(40.73295, -74.00707)
    )

    val NinthStreet = Station(
        pathApiName = "09S",
        displayName = "9th Street",
        coordinates = Coordinates(40.73424, -73.9991)
    )

    val FourteenthStreet = Station(
        pathApiName = "14S",
        displayName = "14th Street",
        coordinates = Coordinates(40.73735, -73.99684)
    )

    val TwentyThirdStreet = Station(
        pathApiName = "23S",
        displayName = "23rd Street",
        coordinates = Coordinates(40.7429, -73.99278)
    )

    val ThirtyThirdStreet = Station(
        pathApiName = "33S",
        displayName = "33rd Street",
        coordinates = Coordinates(40.74912, -73.98827)
    )

    val All = listOf(
        Newark,
        Harrison,
        JournalSquare,
        GroveStreet,
        ExchangePlace,
        Newport,
        Hoboken,
        WorldTradeCenter,
        ChristopherStreet,
        NinthStreet,
        FourteenthStreet,
        TwentyThirdStreet,
        ThirtyThirdStreet
    )

    private val stationById = All.associateBy { it.pathApiName }

    fun byId(id: String): Station? = stationById[id]

    fun byProximityTo(location: Location): List<Station> {
        val (sameState, otherState) = All.partition { it.state == location.state }
        return sameState.sortedBy { it.distanceTo(location) } +
                otherState.sortedBy { it.distanceTo(location) }
    }

    private fun Station.distanceTo(location: Location): Double {
        val dLatitude = coordinates.latitude - location.latitude
        val dLongitude = coordinates.longitude - location.longitude
        return (dLatitude * dLatitude) + (dLongitude * dLongitude)
    }
}
