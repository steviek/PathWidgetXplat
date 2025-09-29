package com.desaiwang.transit.path.api.impl

import com.desaiwang.transit.path.api.DepartingTrain
import com.desaiwang.transit.path.api.Line
import com.desaiwang.transit.path.api.PathApi
import com.desaiwang.transit.path.api.State
import com.desaiwang.transit.path.api.State.NewJersey
import com.desaiwang.transit.path.api.State.NewYork
import com.desaiwang.transit.path.api.Station
import com.desaiwang.transit.path.api.Stations
import com.desaiwang.transit.path.api.UpcomingDepartures
import com.desaiwang.transit.path.api.schedule.GithubScheduleRepository
import com.desaiwang.transit.path.api.schedule.ScheduleAndOverride
import com.desaiwang.transit.path.model.ColorWrapper
import com.desaiwang.transit.path.model.Colors
import com.desaiwang.transit.path.schedule.ScheduleTiming
import com.desaiwang.transit.path.schedule.Schedules
import com.desaiwang.transit.path.time.NewYorkTimeZone
import com.desaiwang.transit.path.util.FetchWithPrevious
import com.desaiwang.transit.path.util.Staleness
import com.desaiwang.transit.path.util.map
import com.desaiwang.transit.path.util.orElse
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes

/**
 * Implementation of PATH API that provides train schedules based on timetables.
 * 
 * This class:
 * 1. Loads schedule data from GitHub repository
 * 2. Handles schedule overrides for service changes
 * 3. Computes arrival times based on schedules
 * 4. Supports different service patterns for weekdays/weekends
 * 5. Handles schedule validity periods and transitions
 */
class SchedulePathApi : PathApi {
    override fun getUpcomingDepartures(
        now: Instant,
        staleness: Staleness
    ): FetchWithPrevious<UpcomingDepartures> {
        return GithubScheduleRepository.getSchedules(now)
            .map { createDepartureBoardMap(now, schedules = it) }
    }

    private fun createDepartureBoardMap(
        now: Instant,
        schedules: ScheduleAndOverride
    ): UpcomingDepartures {
        val (schedule, override) = schedules

        val localNow = now.toLocalDateTime(NewYorkTimeZone)

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

        val allTrains = mutableMapOf<String, List<DepartingTrain>>()

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

        val scheduleName =
            override
                ?.takeIf { localNow.inRange(it.validFrom, it.validTo) }
                .orElse { schedule }
                .name

        return UpcomingDepartures(allTrains, scheduleName = scheduleName)
    }

    private fun createDepartureBoardMap(
        now: Instant,
        schedules: Schedules
    ): Map<String, List<DepartingTrain>> {
        val results = mutableMapOf<String, MutableList<DepartingTrain>>()

        val localNow = now.toLocalDateTime(NewYorkTimeZone)
        schedules.timings.forEach { timing ->
            // This doesn't account for midnight overlap, should sort this out.
            if (!timing.isActiveAt(now)) return@forEach
            val schedule = schedules.schedules.find { it.id == timing.scheduleId } ?: return@forEach

            val slowStartTime = schedule.firstSlowDepartureTime
            val slowEndTime = schedule.lastSlowDepartureTime

            fun isSlowAt(time: LocalTime): Boolean {
                slowStartTime ?: return false
                slowEndTime ?: return false
                return if (slowStartTime <= slowEndTime) {
                    time >= slowStartTime && time <= slowEndTime
                } else {
                    time >= slowStartTime || time <= slowEndTime
                }
            }

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
                    val dates = mutableListOf(localNow.date)
                    if (timing.isActiveAt(localNow.minusDays(1).toInstant(NewYorkTimeZone))) {
                        dates += localNow.minusDays(1).date
                    }
                    if (timing.isActiveAt(localNow.plusDays(1).toInstant(NewYorkTimeZone))) {
                        dates += localNow.plusDays(1).date
                    }

                    dates.forEach { date ->
                        val originTrain = DepartingTrain(
                            headsign = headsign,
                            projectedArrival = date.atTime(time).toInstant(NewYorkTimeZone),
                            lineColors = lineColors,
                            isDelayed = false,
                            backfillSource = null,
                            directionState = directionState,
                            lines = lines,
                        )
                        results.getOrPut(origin.pathApiName) { mutableListOf() } += originTrain

                        val checkpoints =
                            TrainBackfillHelper.getCheckpoints(route, isSlowTime = isSlowAt(time))
                                ?: return@forEachDeparture
                        checkpoints.filterKeys { it != origin }.forEach { (checkpointStation, checkpointTime) ->
                            results.getOrPut(checkpointStation.pathApiName) { mutableListOf() } +=
                                originTrain.copy(
                                    projectedArrival = originTrain.projectedArrival + checkpointTime
                                )
                        }
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

private fun LocalDateTime.inRange(from: LocalDateTime, to: LocalDateTime?): Boolean {
    if (this < from) return false
    if (to == null) return true
    return this < to
}

fun LocalDateTime.minusDays(days: Int): LocalDateTime {
    return date.minus(DatePeriod(days = 1)).atTime(time)
}

fun LocalDateTime.plusDays(days: Int): LocalDateTime {
    return date.plus(DatePeriod(days = 1)).atTime(time)
}