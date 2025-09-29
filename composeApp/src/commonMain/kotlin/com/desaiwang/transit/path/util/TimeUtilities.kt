package com.desaiwang.transit.path.util

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class TimeUtilities {
    fun getStartOfNextMinute(time: Instant): Instant {
        val timeZone = TimeZone.currentSystemDefault()
        return time
            .plus(1, DateTimeUnit.MINUTE)
            .toLocalDateTime(timeZone)
            .floorToMinute()
            .toInstant(timeZone)
    }

    private fun LocalDateTime.floorToMinute(): LocalDateTime {
        return LocalDateTime(
            year = year,
            monthNumber = monthNumber,
            dayOfMonth = dayOfMonth,
            hour = hour,
            minute = minute
        )
    }
}