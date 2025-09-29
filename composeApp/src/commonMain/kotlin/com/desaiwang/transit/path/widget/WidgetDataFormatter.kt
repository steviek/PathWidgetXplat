package com.desaiwang.transit.path.widget

import com.desaiwang.transit.path.time.is24HourClock
import com.desaiwang.transit.path.util.localizedString
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.DayOfWeek.FRIDAY
import kotlinx.datetime.DayOfWeek.MONDAY
import kotlinx.datetime.DayOfWeek.SATURDAY
import kotlinx.datetime.DayOfWeek.SUNDAY
import kotlinx.datetime.DayOfWeek.THURSDAY
import kotlinx.datetime.DayOfWeek.TUESDAY
import kotlinx.datetime.DayOfWeek.WEDNESDAY
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.language_code
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

object WidgetDataFormatter {

    private val locale by lazy { runBlocking { getString(string.language_code) } }

    fun formatArrivalTimes(
        now: Instant,
        arrivalTimes: List<Instant>
    ): String {
        return arrivalTimes.filter { it >= now - 1.minutes }.joinToString {
            formatTime(it)
        }
    }

    fun formatHeadSign(title: String, fits: (String) -> Boolean): String {
        val wide = formatHeadSign(title, HeadSignWidth.Wide)
        if (fits(wide)) return wide

        val short = formatHeadSign(title, HeadSignWidth.Short)
        if (fits(short)) return short

        return formatHeadSign(title, HeadSignWidth.Narrow)
    }

    fun formatHeadSign(title: String, width: HeadSignWidth): String {
        if (title.startsWith("Journal Square")) {
            return when (width) {
                HeadSignWidth.Narrow -> "JSQ"
                HeadSignWidth.Short -> "Journal Square"
                HeadSignWidth.Wide -> title
            }
        }

        if (title.startsWith("33rd Street")) {
            return when (width) {
                HeadSignWidth.Narrow -> "33S"
                HeadSignWidth.Short -> "33rd St"
                HeadSignWidth.Wide -> title
            }
        }

        if (title.startsWith("Newark")) {
            return when (width) {
                HeadSignWidth.Narrow -> "NWK"
                HeadSignWidth.Short -> "Newark"
                HeadSignWidth.Wide -> title
            }
        }

        if (title.startsWith("Exchange")) {
            return when (width) {
                HeadSignWidth.Narrow -> "EXP"
                HeadSignWidth.Short -> title
                HeadSignWidth.Wide -> title
            }
        }

        if (title.startsWith("World")) {
            return when (width) {
                HeadSignWidth.Narrow -> "WTC"
                HeadSignWidth.Short -> title
                HeadSignWidth.Wide -> title
            }
        }

        if (title == "Hoboken") {
            return when (width) {
                HeadSignWidth.Narrow -> "HOB"
                HeadSignWidth.Short -> title
                HeadSignWidth.Wide -> title
            }
        }

        if (title.startsWith("Christopher")) {
            return when (width) {
                HeadSignWidth.Narrow -> "CHR"
                HeadSignWidth.Short -> "Christopher St"
                HeadSignWidth.Wide -> title
            }
        }

        if (title.startsWith("Har")) {
            return when (width) {
                HeadSignWidth.Narrow -> "HAR"
                HeadSignWidth.Short -> title
                HeadSignWidth.Wide -> title
            }
        }

        if ("Grove" in title) {
            return when (width) {
                HeadSignWidth.Narrow -> "GRV"
                HeadSignWidth.Short -> "Grove St"
                HeadSignWidth.Wide -> title
            }
        }

        return title
    }

    fun formatTime(instant: Instant): String {
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val time = dateTime.time
        val hour = when {
            is24HourClock() -> time.hour.toString().padStart(2, '0')
            time.hour in 1..12 -> time.hour.toString()
            time.hour == 0 -> "12"
            else -> (time.hour - 12).toString()
        }
        return hour + ":" + time.minute.toString().padStart(2, '0')
    }

    fun formatTimeWithSeconds(instant: Instant): String {
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val time = dateTime.time
        return time.formatted(includePeriodMarker = false)
    }

    fun formatRelativeTime(now: Instant, time: Instant): String {
        val duration = time - now
        return if (duration < 1.minutes) {
            localizedString(en = "due", es = "llega")
        } else if (duration < 1.hours) {
            duration.inWholeMinutes.toString() + " min"
        } else {
            duration.inWholeHours.toString() + " hr " + (duration.inWholeMinutes % 60) + " min"
        }
    }

    fun LocalTime.formatted(includePeriodMarker: Boolean): String {
        val hourString = when {
            is24HourClock() -> hour.toString().padStart(2, '0')
            hour in 1..12 -> hour.toString()
            hour == 0 -> "12"
            else -> (hour - 12).toString()
        }

        val periodMarker = when {
            !includePeriodMarker || is24HourClock() -> ""
            hour < 12 -> localizedString(en = " a.m.", es = " a. m.")
            else -> localizedString(en = " p.m.", es = " p. m.")
        }

        return hourString + ":" + minute.toString().padStart(2, '0') + periodMarker
    }

    fun DayOfWeek.singleLetterLabel(): String = when (this) {
        MONDAY -> localizedString(en = "M", es = "L")
        TUESDAY -> localizedString(en = "T", es = "M")
        WEDNESDAY -> localizedString(en = "W", es = "X")
        THURSDAY -> localizedString(en = "T", es = "J")
        FRIDAY -> localizedString(en = "F", es = "V")
        SATURDAY -> localizedString(en = "S", es = "S")
        SUNDAY -> localizedString(en = "S", es = "D")
        else -> ""
    }

    fun DayOfWeek.displayLabel(): String = when (this) {
        MONDAY -> localizedString(en = "Monday", es = "Lunes")
        TUESDAY -> localizedString(en = "Tuesday", es = "Martes")
        WEDNESDAY -> localizedString(en = "Wednesday", es = "Miércoles")
        THURSDAY -> localizedString(en = "Thursday", es = "Jueves")
        FRIDAY -> localizedString(en = "Friday", es = "Viernes")
        SATURDAY -> localizedString(en = "Saturday", es = "Sábado")
        SUNDAY -> localizedString(en = "Sunday", es = "Domingo")
        else -> ""
    }

}

enum class HeadSignWidth {
    Narrow, Short, Wide
}
