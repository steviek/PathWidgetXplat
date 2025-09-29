package com.desaiwang.transit.path.api.impl

import com.desaiwang.transit.path.app.settings.Schedule
import com.desaiwang.transit.path.app.settings.isActiveAt
import kotlinx.datetime.DayOfWeek.FRIDAY
import kotlinx.datetime.DayOfWeek.MONDAY
import kotlinx.datetime.DayOfWeek.SATURDAY
import kotlinx.datetime.DayOfWeek.SUNDAY
import kotlinx.datetime.DayOfWeek.THURSDAY
import kotlinx.datetime.DayOfWeek.TUESDAY
import kotlinx.datetime.DayOfWeek.WEDNESDAY
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommutingConfigurationTest {
    @Test
    fun `not crossing midnight`() {
        val schedule = Schedule(
            days = setOf(TUESDAY, WEDNESDAY),
            start = 10.h,
            end = 16.h
        )

        assertTrue { schedule.isActiveAt(TUESDAY, 10.h) }
        assertTrue { schedule.isActiveAt(TUESDAY, 11.h) }
        assertTrue { schedule.isActiveAt(WEDNESDAY, 11.h) }

        assertFalse { schedule.isActiveAt(TUESDAY, 16.h) }
        assertFalse { schedule.isActiveAt(MONDAY, 11.h) }
    }

    @Test
    fun `crossing midnight`() {
        val schedule = Schedule(
            days = setOf(THURSDAY, SATURDAY),
            start = 22.h,
            end = 5.h
        )

        assertTrue { schedule.isActiveAt(THURSDAY, 23.h) }
        assertTrue { schedule.isActiveAt(FRIDAY, 4.h) }
        assertTrue { schedule.isActiveAt(SUNDAY, 2.h) }
        assertTrue { schedule.isActiveAt(SATURDAY, 23.h) }

        assertFalse { schedule.isActiveAt(THURSDAY, 4.h) }
        assertFalse { schedule.isActiveAt(SATURDAY, 3.h) }
        assertFalse { schedule.isActiveAt(FRIDAY, 23.h) }
    }

    @Test
    fun `start same as end`() {
        val schedule = Schedule(
            days = setOf(THURSDAY, SATURDAY),
            start = 22.h,
            end = 22.h
        )

        assertTrue { schedule.isActiveAt(THURSDAY, 23.h) }
        assertTrue { schedule.isActiveAt(SATURDAY, 4.h) }

        assertFalse { schedule.isActiveAt(FRIDAY, 23.h) }
    }

    private val Int.h get() = LocalTime(this, 0)
}