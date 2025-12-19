package com.sixbynine.transit.path.time

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.DayOfWeek.FRIDAY
import kotlinx.datetime.DayOfWeek.MONDAY
import kotlinx.datetime.DayOfWeek.SATURDAY
import kotlinx.datetime.DayOfWeek.SUNDAY
import kotlinx.datetime.DayOfWeek.THURSDAY
import kotlinx.datetime.DayOfWeek.TUESDAY
import kotlinx.datetime.DayOfWeek.WEDNESDAY
import kotlin.test.Test
import kotlin.test.assertEquals

class TimeUtilsTest {
    @Test
    fun plusDays() {
        assertEquals(SUNDAY, SATURDAY.plusDays(1))
        assertEquals(MONDAY, SUNDAY.plusDays(1))
        assertEquals(SUNDAY, MONDAY.minusDays(1))
        assertEquals(SATURDAY, SUNDAY.minusDays(1))
        assertEquals(MONDAY, THURSDAY.minusDays(3))
        assertEquals(SATURDAY, THURSDAY.minusDays(5))

        assertEquals(MONDAY, MONDAY.plusDays(7))
        assertEquals(TUESDAY, MONDAY.plusDays(8))
    }

    @Test
    fun `closed day of week set, sat - sun`() {
        val set = closedDayOfWeekSet(start = SATURDAY, end = SUNDAY)

        assertEquals(setOf(SATURDAY, SUNDAY), set)
    }

    @Test
    fun `closed day of week set, single day`() {
        val set = closedDayOfWeekSet(start = MONDAY, end = MONDAY)

        assertEquals(setOf(MONDAY), set)
    }

    @Test
    fun `closed day of week set, mid week`() {
        val set = closedDayOfWeekSet(start = MONDAY, end = THURSDAY)

        assertEquals(setOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY), set)
    }

    @Test
    fun `closed day of week set, wrap around`() {
        val set = closedDayOfWeekSet(start = THURSDAY, end = MONDAY)

        assertEquals(setOf(THURSDAY, FRIDAY, SATURDAY, SUNDAY, MONDAY), set)
    }

    @Test
    fun `closed day of week set, all days`() {
        val set = closedDayOfWeekSet(start = MONDAY, end = SUNDAY)

        assertEquals(DayOfWeek.entries.toSet(), set)
    }
}
