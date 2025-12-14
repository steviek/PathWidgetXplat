package com.sixbynine.transit.path.schedule.generator

import com.fleeksoft.ksoup.Ksoup
import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.Route
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.Stations.ExchangePlace
import com.sixbynine.transit.path.api.Stations.GroveStreet
import com.sixbynine.transit.path.api.Stations.Harrison
import com.sixbynine.transit.path.api.Stations.Hoboken
import com.sixbynine.transit.path.api.Stations.JournalSquare
import com.sixbynine.transit.path.api.Stations.Newark
import com.sixbynine.transit.path.api.Stations.Newport
import com.sixbynine.transit.path.api.Stations.ThirtyThirdStreet
import com.sixbynine.transit.path.api.Stations.WorldTradeCenter
import com.sixbynine.transit.path.util.nonEmptyListOf
import com.sixbynine.transit.path.util.toNonEmptyList
import kotlinx.datetime.LocalTime

object ScheduleHtmlParser {
    fun parseDepartures(
        html: String,
        verboseLogging: Boolean = false
    ): Pair<Route, List<LocalTime>>? {
        val document = Ksoup.parse(html)

        val headers = arrayListOf<String>()
        val departures = arrayListOf<ArrayList<LocalTime>>()

        document.select("tr").forEach eachRow@{ tableRow ->
            val isHeaderRow = headers.isEmpty()

            val columns = tableRow.select("td")
            if (columns.all { it.text().isBlank() }) return@eachRow

            columns.forEachIndexed eachColumn@{ index, td ->
                val text = td.text().trim()
                if (isHeaderRow) {
                    headers.add(text)
                    return@eachColumn
                }

                if (text.trim().all { it == '-' }) {
                    check(index > 0) {
                        "First station is skipped, need to handle this"
                    }
                    // Skipped station
                    return@eachColumn
                }

                val components = text.split(":")
                check(components.size == 2 && components.none { it.isBlank() }) {
                    "Invalid time format: $text"
                }

                while (departures.size <= index) {
                    departures.add(arrayListOf())
                }

                try {
                    val part1 = components[0].trim()
                    val part2 = components[1].trim()

                    val isAm = "AM" in part2
                    if (!isAm) {
                        check("PM" in part2)
                    }

                    var hour = part1.filter { it.isDigit() }.toInt()
                    if (!isAm && hour != 12) hour += 12
                    if (isAm && hour == 12) hour = 0

                    val minute = part2.filter { it.isDigit() }.toInt()

                    departures[index].add(LocalTime(hour, minute))
                } catch (e: Exception) {
                    throw IllegalStateException("Error parsing time: $text", e)
                }
            }
        }

        if (verboseLogging) {
            repeat(headers.size) { index ->
                val header = headers[index]
                val departureList = departures[index]
                println("$header: $departureList")
            }
        }

        val departureTimes = departures.firstOrNull() ?: return null

        val stations = headers.map {
            val trimmed = it.removeSuffix(" Departure").removePrefix("Arrival at ")
            checkNotNull(Stations.fromHeadSign(trimmed)) {
                "Unknown station: $trimmed"
            }
        }.toNonEmptyList() ?: return null

        val origin = stations.first()
        val destination = stations.last()

        fun unknownRoute(): Nothing = error("Unknown route: $origin -> $destination")

        val lines = when (origin) {
            Newark -> when (destination) {
                WorldTradeCenter, ExchangePlace, GroveStreet, JournalSquare, Harrison -> {
                    nonEmptyListOf(Line.NewarkWtc)
                }

                else -> unknownRoute()

            }

            JournalSquare -> when (destination) {
                WorldTradeCenter -> nonEmptyListOf(Line.NewarkWtc)
                ThirtyThirdStreet -> if (Hoboken in stations) {
                    nonEmptyListOf(Line.JournalSquare33rd, Line.Hoboken33rd)
                } else {
                    nonEmptyListOf(Line.JournalSquare33rd)
                }

                else -> unknownRoute()
            }

            WorldTradeCenter -> when (destination) {
                Newark, JournalSquare, GroveStreet, ExchangePlace -> nonEmptyListOf(Line.NewarkWtc)
                Hoboken, Newport -> nonEmptyListOf(Line.HobokenWtc)
                ThirtyThirdStreet -> nonEmptyListOf(Line.HobokenWtc, Line.JournalSquare33rd)
                else -> unknownRoute()
            }

            Hoboken -> when (destination) {
                ExchangePlace, Newport, WorldTradeCenter -> nonEmptyListOf(Line.HobokenWtc)
                ThirtyThirdStreet -> nonEmptyListOf(Line.Hoboken33rd)
                else -> unknownRoute()
            }

            ThirtyThirdStreet -> when (destination) {
                Hoboken -> nonEmptyListOf(Line.Hoboken33rd)
                JournalSquare, GroveStreet, Newport -> if (Hoboken in stations) {
                    nonEmptyListOf(Line.Hoboken33rd, Line.JournalSquare33rd)
                } else {
                    nonEmptyListOf(Line.JournalSquare33rd)
                }

                else -> unknownRoute()
            }

            else -> unknownRoute()
        }

        return Route(lines, stations) to departureTimes
    }
}
