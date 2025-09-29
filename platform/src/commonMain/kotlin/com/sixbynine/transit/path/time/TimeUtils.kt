package com.desaiwang.transit.path.time

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
    return DayOfWeek.values()[newOrdinal]
}

fun DayOfWeek.minusDays(days: Int): DayOfWeek = plusDays(-days)
