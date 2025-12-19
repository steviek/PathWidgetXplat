package com.sixbynine.transit.path.time

import kotlinx.datetime.DayOfWeek

actual fun getPlatformTimeUtils(): PlatformTimeUtils = object : PlatformTimeUtils{
    override fun is24HourClock(): Boolean {
        return true
    }

    override fun getFirstDayOfWeek(): DayOfWeek {
        return DayOfWeek.MONDAY
    }
}
