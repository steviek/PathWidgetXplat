package com.sixbynine.transit.path.widget

import com.sixbynine.transit.path.time.is24HourClock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes

object WidgetDataFormatter {
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
        if (width == HeadSignWidth.Wide) return title

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
                HeadSignWidth.Narrow -> "Christopher"
                HeadSignWidth.Short -> "Christopher St"
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
}

enum class HeadSignWidth {
    Narrow, Short, Wide
}
