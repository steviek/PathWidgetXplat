package com.desaiwang.transit.path.api.alerts

import com.desaiwang.transit.path.api.Line
import com.desaiwang.transit.path.api.Station
import com.desaiwang.transit.path.time.NewYorkTimeZone
import com.desaiwang.transit.path.time.now
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Alert(
    /** Path 3 letter station code. */
    val stations: List<String>,

    /**
     * When the alert takes effect. While effective, the alert will hide trains matching the
     * [trains] filter for [stations].
     */
    @SerialName("schedule")
    val hideTrainsSchedule: Schedule,

    /**
     * When the alert should display in the app. If the alert does not hide trains, this is the only
     * schedule that should be set.
     */
    val displaySchedule: Schedule? = null,

    /** Trains that are skipping the stations while the alert is active. */
    @SerialName("trains")
    val hiddenTrainsFilter: TrainFilter,

    /** Optional text to display with the alert. */
    val message: AlertText? = null,

    /** Optional web link where the user can read more. */
    val url: AlertText? = null,

    /** The level of the alert's value. */
    val level: String? = null,

    /** Whether this is a global level alert. Stations are ignored if so. */
    val isGlobal: Boolean = false,

    /** Lines affected. */
    val lines: Set<Line>? = null,
)

fun Alert(
    stations: List<Station>,
    hideTrainsSchedule: Schedule,
    hiddenTrainsFilter: TrainFilter,
    message: AlertText? = null,
    url: AlertText? = null,
    displaySchedule: Schedule? = null,
    isGlobal: Boolean = false,
    level: String? = null,
    lines: Set<Line>? = null
): Alert {
    return Alert(
        stations = stations.map { it.pathApiName },
        displaySchedule = displaySchedule,
        hideTrainsSchedule = hideTrainsSchedule,
        hiddenTrainsFilter = hiddenTrainsFilter,
        message = message,
        url = url,
        level = level,
        isGlobal = isGlobal,
        lines = lines,
    )
}

fun Alert.affectsLines(lineSet: Collection<Line>): Boolean {
    return lines.isNullOrEmpty() || lineSet.any { it in lines }
}

/** Schedule for an alert being active. All local times are local to NYC...duh. */
@Serializable
data class Schedule(
    /** Daily repeating schedule for the alert. */
    val repeatingDaily: RepeatingDailySchedule? = null,
    /** Weekly repeating schedule for the alert. */
    val repeatingWeekly: RepeatingWeeklySchedule? = null,
    /** One-time schedule for the alert. */
    val once: OnceSchedule? = null,
) {
    companion object {
        fun repeatingDaily(
            days: List<DayOfWeek>,
            start: LocalTime,
            end: LocalTime,
            from: LocalDate,
            to: LocalDate,
        ): Schedule {
            return Schedule(
                repeatingDaily = RepeatingDailySchedule(
                    days = days.map { it },
                    start = start,
                    end = end,
                    from = from,
                    to = to
                )
            )
        }

        fun repeatingWeekly(
            startDay: DayOfWeek,
            startTime: LocalTime,
            endDay: DayOfWeek,
            endTime: LocalTime,
            from: LocalDate,
            to: LocalDate,
        ): Schedule {
            return Schedule(
                repeatingWeekly = RepeatingWeeklySchedule(
                    startDay = startDay,
                    startTime = startTime,
                    endDay = endDay,
                    endTime = endTime,
                    from = from,
                    to = to
                )
            )
        }

        fun once(from: LocalDateTime, to: LocalDateTime): Schedule {
            return Schedule(once = OnceSchedule(from, to))
        }
    }
}

/**
 * Repeating schedule at the same times each day. If [end] is before [start], then the schedule is
 * overnight. [days] indicates which days the schedule will *start* on.
 */
@Serializable
data class RepeatingDailySchedule(
    /** Days of week when the schedule starts. */
    val days: List<DayOfWeek>,
    /** The (inclusive) time on the day when the schedule starts. */
    val start: LocalTime,
    /** The (exclusive) time when the schedule ends. */
    val end: LocalTime,
    /** The (inclusive) date the schedule starts being valid. */
    val from: LocalDate,
    /** The (inclusive) date when the schedule stops being valid. */
    val to: LocalDate,
)

/**
 * Repeating schedule for a time range that repeats each week.
 */
@Serializable
data class RepeatingWeeklySchedule(
    /** Days of week when the schedule starts. */
    val startDay: DayOfWeek,
    /** The (inclusive) time on [startDay] when the schedule starts. */
    val startTime: LocalTime,
    /** Days of week when the schedule ends. */
    val endDay: DayOfWeek,
    /** The (exclusive) time on [endDay] when the schedule ends. */
    val endTime: LocalTime,
    /** The (inclusive) date when the schedule starts being valid. */
    val from: LocalDate,
    /** The (inclusive) date when the schedule stops being valid. */
    val to: LocalDate,
)

/** A schedule simply valid from [from] to [to]. */
@Serializable
data class OnceSchedule(
    /** When the schedule starts being valid. */
    val from: LocalDateTime,
    /** When the schedule stops being valid. */
    val to: LocalDateTime,
)

@Serializable
data class AlertText(val localizations: List<TextAndLocale>) {
    constructor(en: String, es: String) : this(
        listOf(
            TextAndLocale(text = en, locale = "en"),
            TextAndLocale(text = es, locale = "es")
        )
    )

    constructor(en: String) : this(
        listOf(TextAndLocale(text = en, locale = "en"))
    )
}

/**
 * Resolves the best match for the alert text for the language.
 *
 * @param languageCode a two letter language code, e.g. "en" or "es"
 */
fun AlertText.getText(languageCode: String): String? {
    return localizations.find { it.locale == languageCode }?.text
        ?: localizations.firstOrNull()?.text
}

@Serializable
data class TextAndLocale(val text: String?, val locale: String)

@Serializable
data class TrainFilter(
    val all: Boolean? = null,
    val headSigns: List<String>? = null,
) {
    companion object {
        fun all() = TrainFilter(all = true)

        fun headSigns(vararg headSigns: String) = TrainFilter(headSigns = headSigns.toList())
    }
}

fun Alert.canHideTrainsAt(dateTime: LocalDateTime): Boolean {
    return hideTrainsSchedule.isActiveAt(dateTime)
}

fun Alert.isDisplayedNow(): Boolean {
    val dateTime = now().toLocalDateTime(NewYorkTimeZone)
    return isDisplayedAt(dateTime)
}

fun Alert.isDisplayedAt(dateTime: LocalDateTime): Boolean {
    return displaySchedule?.isActiveAt(dateTime) == true || canHideTrainsAt(dateTime)
}

val Alert.isWarning: Boolean get() = level == null || level.startsWith("WARN", ignoreCase = true)

fun Schedule.isActiveAt(dateTime: LocalDateTime): Boolean {
    repeatingWeekly?.run {
        if (dateTime.date !in from..to) return false

        val day = dateTime.dayOfWeek
        val time = dateTime.time

        return when {
            day == startDay && day == endDay -> time >= startTime && time < endTime
            day == startDay -> time >= startTime
            day == endDay -> time < endTime
            startDay < endDay -> day in startDay..endDay
            else -> day >= startDay || day <= endDay

        }
    }

    repeatingDaily?.run {
        if (dateTime.date !in from..to) return false

        val day = dateTime.dayOfWeek
        val time = dateTime.time

        if (start < end) {
            return day in days && time >= start && time < end
        }

        if (time >= start) {
            return day in days
        }

        if (time < end) {
            return day.previousDay in days
        }

        return false
    }

    once?.run {
        return dateTime in from..to
    }

    return false
}

fun Alert.hidesTrain(stationName: String, headSign: String): Boolean {
    if (stationName !in stations) return false

    if (hiddenTrainsFilter.all == true) return true

    hiddenTrainsFilter.headSigns?.forEach {
        if (headSign.contains(it, ignoreCase = true)) return true
    }

    return false
}

fun Alert.hidesTrainAt(stationName: String, headSign: String, time: Instant): Boolean {
    val dateTime = time.toLocalDateTime(NewYorkTimeZone)
    return canHideTrainsAt(dateTime) && hidesTrain(stationName, headSign)
}

private val DayOfWeek.previousDay: DayOfWeek
    get() = DayOfWeek.values().getOrNull(ordinal - 1) ?: DayOfWeek.values().last()
