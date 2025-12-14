package com.sixbynine.transit.path.schedule.generator

import com.sixbynine.transit.path.api.Route
import com.sixbynine.transit.path.api.Stations.Hoboken
import com.sixbynine.transit.path.api.Stations.JournalSquare
import com.sixbynine.transit.path.api.Stations.ThirtyThirdStreet
import com.sixbynine.transit.path.api.destination
import com.sixbynine.transit.path.api.origin
import com.sixbynine.transit.path.schedule.Schedule
import com.sixbynine.transit.path.schedule.ScheduleTiming
import com.sixbynine.transit.path.schedule.Schedules
import com.sixbynine.transit.path.time.NewYorkTimeZone
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object ScheduleParser {
    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val MIDNIGHT = LocalTime(0, 0)

    fun parse(response: String, name: String?): Schedules {
        val model = json.decodeFromString<JsonElement>(response)

        return JsonParsingScope(model).run {
            child(":children") {
                readChildren(name)
            }
        }
    }

    private fun JsonParsingScope.readChildren(name: String?): Schedules {
        val allSchedules = SchedulePage.entries.map { page ->
            child(page.path) {
                readPage(page)
            }
        }
        check(allSchedules.isNotEmpty())

        val idsFromSchedules = allSchedules.flatMap { it.schedules.map { it.id } }
        val idsFromTimings = allSchedules.flatMap { it.timings.map { it.scheduleId } }

        check(idsFromSchedules.distinct().size == idsFromSchedules.size) {
            "Schedule id was duplicated in schedules: $idsFromSchedules"
        }

        check(idsFromTimings.distinct().size == idsFromTimings.size) {
            "Schedule id was duplicated in timings"
        }

        check(idsFromSchedules.size == idsFromTimings.size) {
            "Mismatch between schedule ids and timing ids"
        }

        return Schedules(
            validFrom = allSchedules.maxOf { it.validFrom },
            validTo = null,
            schedules = allSchedules.flatMap { it.schedules },
            timings = allSchedules.flatMap { it.timings },
            name = name,
        )
    }

    private fun JsonParsingScope.readPage(page: SchedulePage): Schedules {
        val date = Instant.parse(child("date") { string() })

        val schedules = arrayListOf<Schedule>()
        val scheduleTimings = arrayListOf<ScheduleTiming>()

        child(":items", "root", ":items") {
            val pageTitle = readTitleFromHeroCopy()

            val accordionListKeys = children.filter { it.startsWith("accordion") }
            if (accordionListKeys.isEmpty()) {
                error("No accordion lists found for $page")
            }


            accordionListKeys.forEach { key ->
                child(key) {
                    val title = if (accordionListKeys.size == 1) {
                        pageTitle
                    } else {
                        child("title") { string() }
                    }.trim().removeSuffix(":")

                    val scheduleId = when {
                        "Weekday" in title -> ScheduleId.Weekday
                        "Saturday" in title -> ScheduleId.Saturday
                        "Sunday" in title -> ScheduleId.Sunday
                        else -> error("Unknown schedule type for $title")
                    }

                    val departures = mutableMapOf<String, ArrayList<LocalTime>>()

                    val itemOrder = child(":itemsOrder") { stringList() }
                    child(":items") {
                        val keyToIndex = itemOrder.mapIndexed { index, key -> key to index }.toMap()
                        children
                            .sortedBy { keyToIndex[it] }
                            .forEach {
                                child(it) {
                                    val section = readAccordionSection()
                                    section.forEach { (route, times) ->
                                        val routeDepartures =
                                            departures.getOrPut(route.toDepartureMapKey()) {
                                                ArrayList()
                                            }
                                        routeDepartures.addAll(times)
                                    }
                                }
                            }

                    }

                    schedules.add(
                        Schedule(
                            id = scheduleId.id,
                            name = title,
                            departures = departures,
                            firstSlowDepartureTime = null,
                            lastSlowDepartureTime = null,
                        )
                    )

                    scheduleTimings.add(scheduleId.toScheduleTiming())
                }
            }
        }

        return Schedules(
            validFrom = date.toLocalDateTime(NewYorkTimeZone),
            validTo = null,
            schedules = schedules,
            timings = scheduleTimings,
            name = null,
        )
    }

    private enum class ScheduleId(val id: Int) {
        Weekday(1),
        Saturday(2),
        Sunday(3)
    }

    private fun ScheduleId.toScheduleTiming(): ScheduleTiming {
        val (startDay, endDay) = when (this) {
            ScheduleId.Weekday -> DayOfWeek.MONDAY to DayOfWeek.SATURDAY
            ScheduleId.Saturday -> DayOfWeek.SATURDAY to DayOfWeek.SUNDAY
            ScheduleId.Sunday -> DayOfWeek.SUNDAY to DayOfWeek.MONDAY
        }
        return ScheduleTiming(
            startDay = startDay,
            endDay = endDay,
            startTime = MIDNIGHT,
            endTime = MIDNIGHT,
            scheduleId = id,
        )
    }

    private fun Route.toDepartureMapKey(): String {
        if (origin == JournalSquare && destination == ThirtyThirdStreet && Hoboken in stops) {
            return "JSQ_HOB_33S"
        }
        if (origin == ThirtyThirdStreet && destination == JournalSquare && Hoboken in stops) {
            return "33S_HOB_JSQ"
        }
        return origin.pathApiName + "_" + destination.pathApiName
    }

    private fun JsonParsingScope.readAccordionSection(): Map<Route, List<LocalTime>> {
        val itemOrder = child(":itemsOrder") { stringList() }
        val keyToIndex =
            itemOrder.mapIndexed { index, key -> key to index }
                .toMap()
        return child(":items") {
            children.sortedBy { keyToIndex[it] }
                .mapNotNull {
                    child(it) {
                        val type = child(":type") { string() }
                        if (type != "portauthority/components/Text") {
                            return@mapNotNull null
                        }

                        val html = child("text") { string() }
                        ScheduleHtmlParser.parseDepartures(html)
                    }
                }
                .toMap()
        }
    }

    private fun JsonParsingScope.readTitleFromHeroCopy(): String {
        return child("simplehero_copy", "title") { string() }
    }
}

private enum class SchedulePage(val path: String) {
    Weekday("/path/en/schedules-maps/weekday-schedules"),
    Weekend("/path/en/schedules-maps/weekend-schedules"),
}

class JsonParsingScope(
    val element: JsonElement,
    val pathSegments: List<String> = emptyList()
) {

    val prettyPath get() = pathSegments.joinToString(" > ") { "\"$it\"" }

    val children get() = element.jsonObject.keys

    inline fun <T> child(key: String, block: JsonParsingScope.() -> T): T {
        val childElement = element.jsonObject[key]
            ?: error("Path model does not have '$key' at $prettyPath")
        val scope = JsonParsingScope(childElement, pathSegments + key)
        return scope.block()
    }

    fun <T> child(first: String, vararg others: String, block: JsonParsingScope.() -> T): T {
        var scope = this
        val all = listOf(first) + others.toList()
        for (segment in all) {
            scope = scope.child(segment) { this }
        }
        return scope.block()
    }

    fun string(): String {
        check(element.jsonPrimitive.isString) {
            "Expected a string at $prettyPath, but got ${element.jsonPrimitive}"
        }
        return element.jsonPrimitive.content
    }

    fun stringList(): List<String> {
        return element.jsonArray.toList().map { it.jsonPrimitive.content }
    }
}
