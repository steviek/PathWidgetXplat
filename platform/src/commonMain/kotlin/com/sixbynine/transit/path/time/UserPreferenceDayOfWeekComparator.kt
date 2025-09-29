package com.desaiwang.transit.path.time

import kotlinx.datetime.DayOfWeek

class UserPreferenceDayOfWeekComparator(
    private val firstDayOfWeek: DayOfWeek = getPlatformTimeUtils().getFirstDayOfWeek()
) : Comparator<DayOfWeek> {

    override fun compare(a: DayOfWeek, b: DayOfWeek): Int {
        val aIndex = (a.ordinal - firstDayOfWeek.ordinal + 7) % 7
        val bIndex = (b.ordinal - firstDayOfWeek.ordinal + 7) % 7
        return aIndex - bIndex
    }
}
