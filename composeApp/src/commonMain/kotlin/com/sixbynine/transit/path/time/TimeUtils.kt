package com.sixbynine.transit.path.time

import kotlinx.datetime.TimeZone

expect fun is24HourClock(): Boolean

val NewYorkTimeZone = TimeZone.of("America/New_York")