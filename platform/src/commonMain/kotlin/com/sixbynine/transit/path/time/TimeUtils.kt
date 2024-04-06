package com.sixbynine.transit.path.time

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

expect fun is24HourClock(): Boolean

fun now(): Instant = Clock.System.now()

fun today(): LocalDate = now().toLocalDateTime(TimeZone.currentSystemDefault()).date

val NewYorkTimeZone = TimeZone.of("America/New_York")