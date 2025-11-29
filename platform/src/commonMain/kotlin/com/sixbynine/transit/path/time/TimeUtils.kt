package com.sixbynine.transit.path.time

import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

interface PlatformTimeUtils {
    fun is24HourClock(): Boolean

    fun getFirstDayOfWeek(): DayOfWeek
}

expect fun getPlatformTimeUtils(): PlatformTimeUtils

fun is24HourClock() = getPlatformTimeUtils().is24HourClock()

fun now(): Instant = Clock.System.now()

fun today(): LocalDate = now().toLocalDateTime(TimeZone.currentSystemDefault()).date

val NewYorkTimeZone = TimeZone.of("America/New_York")

fun DayOfWeek.plusDays(days: Int): DayOfWeek {
    var newOrdinal = ordinal + days
    while (newOrdinal < 0) {
        newOrdinal += 7
    }
    while (newOrdinal >= 7) {
        newOrdinal -= 7
    }
    return DayOfWeek.entries[newOrdinal]
}

fun DayOfWeek.minusDays(days: Int): DayOfWeek = plusDays(-days)

fun DayOfWeek.previous() = minusDays(1)
fun DayOfWeek.next() = plusDays(1)

fun closedDayOfWeekSet(start: DayOfWeek, end: DayOfWeek): Set<DayOfWeek> {
    return buildSet {
        var current = start
        while (current <= end) {
            add(current)
            current = current.next()
        }
    }
}
