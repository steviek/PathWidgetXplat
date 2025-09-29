package com.desaiwang.transit.path.time

import kotlinx.datetime.DayOfWeek.MONDAY
import kotlinx.datetime.DayOfWeek.SATURDAY
import kotlinx.datetime.DayOfWeek.SUNDAY
import kotlinx.datetime.DayOfWeek.TUESDAY
import kotlin.test.Test
import kotlin.test.assertTrue

class UserPreferenceDayOfWeekComparatorTest {
    @Test
    fun `monday as start`() {
        val comparator = UserPreferenceDayOfWeekComparator(MONDAY)
        assertTrue { comparator.compare(MONDAY, TUESDAY) < 0 }
        assertTrue { comparator.compare(SUNDAY, TUESDAY) > 0 }
        assertTrue { comparator.compare(MONDAY, SUNDAY) < 0 }
    }

    @Test
    fun `sunday as start`() {
        val comparator = UserPreferenceDayOfWeekComparator(SUNDAY)
        assertTrue { comparator.compare(MONDAY, TUESDAY) < 0 }
        assertTrue { comparator.compare(SUNDAY, TUESDAY) < 0 }
        assertTrue { comparator.compare(SUNDAY, SATURDAY) < 0 }
        assertTrue { comparator.compare(MONDAY, SUNDAY) > 0 }
        assertTrue { comparator.compare(SATURDAY, SATURDAY) == 0 }
    }
}