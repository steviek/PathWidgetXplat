package com.sixbynine.transit.path.schedule

import com.sixbynine.transit.path.time.previous
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime

/** Schedule that repeats the same start and end time for a set of days in a week. */
interface DailySchedule : Schedule {
    /** The (inclusive) time on the day when the schedule starts. */
    val start: LocalTime

    /** The (exclusive) time when the schedule ends. */
    val end: LocalTime

    /** Days of week when the schedule starts. */
    val days: Set<DayOfWeek>

    /**
     * The (inclusive) date when the schedule starts being valid. If null, no date is too early for
     * the schedule.
     */
    val from: LocalDate? get() = null

    /**
     * The (inclusive) date when the schedule stop being valid. If null, no date is too late for
     * the schedule.
     */
    val to: LocalDate? get() = null

    override fun isActiveAt(dateTime: LocalDateTime): Boolean {
        val date = dateTime.date
        from?.let { if (dateTime < it.atTime(start) ) return false }
        to?.let { if (date > it) return false }

        val time = dateTime.time
        when {
            start == end -> {
                // If start == end, then it's valid from start on the start day until the end on
                // the next day.
            }
            start > end -> {
                // Overnight schedule, disabled between end and start time.
                if (time >= end && time < start) return false
            }
            else -> {
                // Non-overnight
                if (time < start || time >= end) return false
            }
        }

        val day = date.dayOfWeek
        val startDay = if (start >= end && time < end) day.previous() else day
        return startDay in days
    }
}
