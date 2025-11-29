package com.sixbynine.transit.path.schedule

import kotlinx.datetime.LocalDateTime

/** Schedule that can be either active or inactive given a specific calendar time. */
interface Schedule {
    fun isActiveAt(dateTime: LocalDateTime): Boolean
}
