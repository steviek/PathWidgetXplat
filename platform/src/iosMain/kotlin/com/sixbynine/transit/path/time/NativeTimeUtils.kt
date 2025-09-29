package com.desaiwang.transit.path.time

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.DayOfWeek
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

object IosPlatformTimeUtils : PlatformTimeUtils {

    private val firstDayOfWeek = MutableStateFlow<String?>(null)

    fun setFirstDayOfWeek(firstDayOfWeek: String?) {
        this.firstDayOfWeek.value = firstDayOfWeek
    }

    override fun is24HourClock(): Boolean {
        val dateFormat = NSDateFormatter.dateFormatFromTemplate(
            tmplate = "j",
            options = 0U,
            locale = NSLocale.currentLocale
        )
        return dateFormat?.indexOf("a")?.let { it < 0 } ?: true
    }

    override fun getFirstDayOfWeek(): DayOfWeek {
        val value = firstDayOfWeek.value?.lowercase()
        return when {
            value == null || value.startsWith("su") -> DayOfWeek.SUNDAY
            value.startsWith("sa") -> DayOfWeek.SATURDAY
            value.startsWith("m") -> DayOfWeek.MONDAY
            value.startsWith("tu") -> DayOfWeek.TUESDAY
            value.startsWith("w") -> DayOfWeek.WEDNESDAY
            value.startsWith("th") -> DayOfWeek.THURSDAY
            value.startsWith("f") -> DayOfWeek.FRIDAY
            else -> DayOfWeek.SUNDAY
        }
    }
}

actual fun getPlatformTimeUtils(): PlatformTimeUtils {
    return IosPlatformTimeUtils
}
