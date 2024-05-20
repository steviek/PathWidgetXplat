package com.sixbynine.transit.path.util

import kotlin.time.Duration

data class Staleness(
    val staleAfter: Duration,
    val invalidAfter: Duration,
)
