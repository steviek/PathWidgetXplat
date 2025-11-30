package com.sixbynine.transit.path.app.settings

import com.sixbynine.transit.path.schedule.DailySchedule
import com.sixbynine.transit.path.schedule.Schedule
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.DayOfWeek.FRIDAY
import kotlinx.datetime.DayOfWeek.MONDAY
import kotlinx.datetime.DayOfWeek.THURSDAY
import kotlinx.datetime.DayOfWeek.TUESDAY
import kotlinx.datetime.DayOfWeek.WEDNESDAY
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class CommutingConfiguration(
    val schedules: List<CommutingSchedule>,
) : Schedule {
    override fun isActiveAt(dateTime: LocalDateTime): Boolean {
        return activeSchedule.isActiveAt(dateTime)
    }

    companion object {
        val DefaultSchedule = CommutingSchedule(
            days = setOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY),
            start = LocalTime(12, 0),
            end = LocalTime(3, 0),
        )

        fun default(): CommutingConfiguration {
            return CommutingConfiguration(schedules = listOf(DefaultSchedule))
        }
    }
}

val CommutingConfiguration.activeSchedule: CommutingSchedule
    get() = schedules.firstOrNull() ?: CommutingConfiguration.DefaultSchedule

@Serializable
data class CommutingSchedule(
    override val days: Set<DayOfWeek>,
    override val start: LocalTime,
    override val end: LocalTime,
) : DailySchedule
