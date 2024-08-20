package com.sixbynine.transit.path.api.impl

import com.sixbynine.transit.path.api.DepartureBoardTrain
import com.sixbynine.transit.path.api.DepartureBoardTrainMap
import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.PathApi
import com.sixbynine.transit.path.api.State
import com.sixbynine.transit.path.api.State.NewJersey
import com.sixbynine.transit.path.api.State.NewYork
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.schedule.GithubScheduleRepository
import com.sixbynine.transit.path.api.schedule.ScheduleAndOverride
import com.sixbynine.transit.path.api.schedule.ScheduleTiming
import com.sixbynine.transit.path.api.schedule.Schedules
import com.sixbynine.transit.path.app.ui.ColorWrapper
import com.sixbynine.transit.path.app.ui.Colors
import com.sixbynine.transit.path.time.NewYorkTimeZone
import com.sixbynine.transit.path.util.FetchWithPrevious
import com.sixbynine.transit.path.util.Staleness
import com.sixbynine.transit.path.util.map
import kotlinx.datetime.Instant
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes

class SchedulePathApi : PathApi {
    override fun getUpcomingDepartures(
        now: Instant,
        staleness: Staleness
    ): FetchWithPrevious<DepartureBoardTrainMap> {
        return GithubScheduleRepository.getSchedules(now)
            .map { createDepartureBoardMap(now, schedules = it) }
    }

    private fun createDepartureBoardMap(
        now: Instant,
        schedules: ScheduleAndOverride
    ): DepartureBoardTrainMap {
        val (schedule, override) = schedules

        val overrideTrains =
            override
                ?.takeIf {
                    // Use trains from the override if it is valid or was recently valid.
                    if (it.validFrom.toInstant(NewYorkTimeZone) > now) return@takeIf false
                    val validTo = it.validTo ?: return@takeIf true
                    validTo.toInstant(NewYorkTimeZone) >= now - 30.minutes
                }
                ?.let { createDepartureBoardMap(now, it) }

        val earliestRegularDeparture = run {
            if (override == null) return@run Instant.DISTANT_PAST
            if (override.validFrom.toInstant(NewYorkTimeZone) > now) return@run Instant.DISTANT_PAST
            override.validTo?.toInstant(NewYorkTimeZone) ?: Instant.DISTANT_FUTURE
        }

        val latestRegularDeparture = run {
            if (override == null) return@run Instant.DISTANT_FUTURE
            if (override.validFrom.toInstant(NewYorkTimeZone) > now) {
                return@run override.validFrom.toInstant(NewYorkTimeZone)
            }

            override.validTo ?: return@run Instant.DISTANT_PAST

            Instant.DISTANT_FUTURE
        }

        val regularTrains =
            schedule
                .takeIf {
                    earliestRegularDeparture < now && latestRegularDeparture > now - 30.minutes
                }
                ?.let { createDepartureBoardMap(now, it) }

        val allTrains = mutableMapOf<String, List<DepartureBoardTrain>>()

        (overrideTrains?.keys.orEmpty() + regularTrains?.keys.orEmpty())
            .distinct()
            .forEach { station ->
                allTrains[station] =
                    (overrideTrains?.get(station).orEmpty() + regularTrains?.get(station).orEmpty())
                        .filter {
                            it.projectedArrival >= now - 1.minutes &&
                                    it.projectedArrival < now + 90.minutes
                        }
                        .sortedBy { it.projectedArrival }
            }

        return DepartureBoardTrainMap(allTrains)
    }

    private fun createDepartureBoardMap(
        now: Instant,
        schedules: Schedules
    ): Map<String, List<DepartureBoardTrain>> {
        val results = mutableMapOf<String, MutableList<DepartureBoardTrain>>()

        val localNow = now.toLocalDateTime(NewYorkTimeZone)
        schedules.timings.forEach { timing ->
            // This doesn't account for midnight overlap, should sort this out.
            if (!timing.isActiveAt(now)) return@forEach
            val schedule = schedules.schedules.find { it.id == timing.scheduleId } ?: return@forEach
            schedule.departures.forEach forEachRoute@{ (route, departures) ->
                val headsign: String
                val lineColors: List<ColorWrapper>
                val directionState: State
                val lines: Set<Line>
                val origin: Station
                when (route) {
                    "NWK_WTC" -> {
                        headsign = "World Trade Center"
                        lineColors = Colors.NwkWtc
                        directionState = NewYork
                        lines = setOf(Line.NewarkWtc)
                        origin = Stations.Newark
                    }
                    "WTC_NWK" -> {
                        headsign = "Newark"
                        lineColors = Colors.NwkWtc
                        directionState = NewJersey
                        lines = setOf(Line.NewarkWtc)
                        origin = Stations.WorldTradeCenter
                    }
                    "JSQ_33S" -> {
                        headsign = "33rd Street"
                        lineColors = Colors.Jsq33s
                        directionState = NewYork
                        lines = setOf(Line.JournalSquare33rd)
                        origin = Stations.JournalSquare
                    }
                    "33S_JSQ" -> {
                        headsign = "Journal Square"
                        lineColors = Colors.Jsq33s
                        directionState = NewJersey
                        lines = setOf(Line.JournalSquare33rd)
                        origin = Stations.ThirtyThirdStreet
                    }
                    "JSQ_HOB_33S" -> {
                        headsign = "33rd Street via Hoboken"
                        lineColors = Colors.Jsq33s + Colors.Hob33s
                        directionState = NewYork
                        lines = setOf(Line.JournalSquare33rd, Line.Hoboken33rd)
                        origin = Stations.JournalSquare
                    }
                    "33S_HOB_JSQ" -> {
                        headsign = "Journal Square via Hoboken"
                        lineColors = Colors.Jsq33s + Colors.Hob33s
                        directionState = NewJersey
                        lines = setOf(Line.JournalSquare33rd, Line.Hoboken33rd)
                        origin = Stations.ThirtyThirdStreet
                    }
                    "WTC_HOB" -> {
                        headsign = "Hoboken"
                        lineColors = Colors.HobWtc
                        directionState = NewJersey
                        lines = setOf(Line.HobokenWtc)
                        origin = Stations.WorldTradeCenter
                    }
                    "HOB_WTC" -> {
                        headsign = "World Trade Center"
                        lineColors = Colors.HobWtc
                        directionState = NewYork
                        lines = setOf(Line.HobokenWtc)
                        origin = Stations.Hoboken
                    }
                    "HOB_33S" -> {
                        headsign = "33rd Street"
                        lineColors = Colors.Hob33s
                        directionState = NewYork
                        lines = setOf(Line.Hoboken33rd)
                        origin = Stations.Hoboken
                    }
                    "33S_HOB" -> {
                        headsign = "Hoboken"
                        lineColors = Colors.Hob33s
                        directionState = NewJersey
                        lines = setOf(Line.Hoboken33rd)
                        origin = Stations.ThirtyThirdStreet
                    }
                    "WTC_JSQ" -> {
                        headsign = "Journal Square"
                        lineColors = Colors.NwkWtc
                        directionState = NewJersey
                        lines = setOf(Line.NewarkWtc)
                        origin = Stations.WorldTradeCenter
                    }
                    "JSQ_WTC" -> {
                        headsign = "World Trade Center"
                        lineColors = Colors.NwkWtc
                        directionState = NewYork
                        lines = setOf(Line.NewarkWtc)
                        origin = Stations.JournalSquare
                    }
                    "NWK_HAR" -> {
                        headsign = "Harrison"
                        lineColors = Colors.NwkWtc
                        directionState = NewYork
                        lines = setOf(Line.NewarkWtc)
                        origin = Stations.Newark
                    }
                    "HAR_NWK" -> {
                        headsign = "Newark"
                        lineColors = Colors.NwkWtc
                        directionState = NewJersey
                        lines = setOf(Line.NewarkWtc)
                        origin = Stations.Harrison
                    }
                    else -> return@forEachRoute
                }

                departures.forEach forEachDeparture@{ time ->
                    val originTrain = DepartureBoardTrain(
                        headsign = headsign,
                        projectedArrival = localNow.date.atTime(time).toInstant(NewYorkTimeZone),
                        lineColors = lineColors,
                        isDelayed = false,
                        backfillSource = null,
                        directionState = directionState,
                        lines = lines,
                    )
                    results.getOrPut(origin.pathApiName) { mutableListOf() } += originTrain

                    val checkpoints =
                        TrainBackfillHelper.getCheckpoints(route) ?: return@forEachDeparture
                    checkpoints.forEach { (checkpointStation, checkpointTime) ->
                        results.getOrPut(checkpointStation.pathApiName) { mutableListOf() } +=
                            originTrain.copy(
                                projectedArrival = originTrain.projectedArrival + checkpointTime
                            )
                    }

                }
            }
        }


        return results
    }

    private fun ScheduleTiming.isActiveAt(now: Instant): Boolean {
        val localNow = now.toLocalDateTime(NewYorkTimeZone)
        val day = localNow.dayOfWeek
        val time = localNow.time
        // Yeah, this could be nicer...
        return if (startDay < endDay) {
            if (startTime < endTime) {
                when {
                    day == startDay && (startDay == endDay) -> time >= startTime && time < endTime
                    day == startDay -> time >= startTime
                    day == endDay -> time < endTime
                    else -> day > startDay && day < endDay
                }
            } else {
                when {
                    day == startDay && (startDay == endDay) -> time >= startTime || time < endTime
                    day == startDay -> time >= startTime
                    day == endDay -> time < endTime
                    else -> day > startDay && day < endDay
                }
            }
        } else {
            if (startTime < endTime) {
                when {
                    day == startDay && (startDay == endDay) -> time >= startTime && time < endTime
                    day == startDay -> time >= startTime
                    day == endDay -> time < endTime
                    else -> day > startDay || day < endDay
                }
            } else {
                when {
                    day == startDay && (startDay == endDay) -> time >= startTime || time < endTime
                    day == startDay -> time >= startTime
                    day == endDay -> time < endTime
                    else -> day > startDay || day < endDay
                }
            }
        }
    }
}