package com.sixbynine.transit.path.time

import kotlinx.datetime.DayOfWeek
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

object IosPlatformTimeUtils : PlatformTimeUtils {
    override fun is24HourClock(): Boolean {
        val dateFormat = NSDateFormatter.dateFormatFromTemplate(
            tmplate = "j",
            options = 0U,
            locale = NSLocale.currentLocale
        )
        return dateFormat?.indexOf("a")?.let { it < 0 } ?: true
    }

    override fun getFirstDayOfWeek(): DayOfWeek {
        val iosWeekday = NSCalendar.currentCalendar.firstWeekday
        val isoWeekday = 1 + ((iosWeekday.toInt() + 6) % 7)
        return DayOfWeek(isoWeekday)
    }
}

actual fun getPlatformTimeUtils(): PlatformTimeUtils {
    return IosPlatformTimeUtils
}
