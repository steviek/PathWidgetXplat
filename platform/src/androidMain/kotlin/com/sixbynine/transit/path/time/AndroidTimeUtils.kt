package com.desaiwang.transit.path.time

import android.text.format.DateFormat
import androidx.core.text.util.LocalePreferences
import androidx.core.text.util.LocalePreferences.FirstDayOfWeek
import com.desaiwang.transit.path.PathApplication
import com.desaiwang.transit.path.PreviewContext
import kotlinx.datetime.DayOfWeek

object AndroidPlatformTimeUtils : PlatformTimeUtils {
    override fun is24HourClock(): Boolean {
        return DateFormat.is24HourFormat(PreviewContext ?: PathApplication.instance)
    }

    override fun getFirstDayOfWeek(): DayOfWeek = when (LocalePreferences.getFirstDayOfWeek()) {
        FirstDayOfWeek.MONDAY -> DayOfWeek.MONDAY
        FirstDayOfWeek.TUESDAY -> DayOfWeek.TUESDAY
        FirstDayOfWeek.WEDNESDAY -> DayOfWeek.WEDNESDAY
        FirstDayOfWeek.THURSDAY -> DayOfWeek.THURSDAY
        FirstDayOfWeek.FRIDAY -> DayOfWeek.FRIDAY
        FirstDayOfWeek.SATURDAY -> DayOfWeek.SATURDAY
        FirstDayOfWeek.SUNDAY -> DayOfWeek.SUNDAY
        else -> DayOfWeek.SUNDAY
    }
}

actual fun getPlatformTimeUtils(): PlatformTimeUtils {
    return AndroidPlatformTimeUtils
}
