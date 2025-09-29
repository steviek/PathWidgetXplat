package com.desaiwang.transit.path.time

import kotlinx.datetime.DayOfWeek.MONDAY
import kotlinx.datetime.DayOfWeek.SATURDAY
import kotlinx.datetime.DayOfWeek.SUNDAY
import kotlinx.datetime.DayOfWeek.THURSDAY
import kotlinx.datetime.DayOfWeek.TUESDAY
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
}
