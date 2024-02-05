package com.sixbynine.transit.path.time

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

expect fun is24HourClock(): Boolean

fun now(): Instant = Clock.System.now()

val NewYorkTimeZone = TimeZone.of("America/New_York")