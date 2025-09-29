import com.desaiwang.transit.path.schedule.Schedule
import com.desaiwang.transit.path.schedule.ScheduleTiming
import com.desaiwang.transit.path.schedule.Schedules
import kotlinx.datetime.DayOfWeek.MONDAY
import kotlinx.datetime.DayOfWeek.SATURDAY
import kotlinx.datetime.DayOfWeek.SUNDAY
import kotlinx.datetime.DayOfWeek.TUESDAY
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month.APRIL
import kotlinx.datetime.Month.AUGUST
import kotlinx.datetime.Month.SEPTEMBER
import kotlinx.datetime.atTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test

class GenerateSchedules {
    @Test
    fun foo() {
        printMainSchedule()
        println()
        printOverrideSchedule()
    }
}

val json = Json {
    explicitNulls = false
    encodeDefaults = false
}

private fun printMainSchedule() {
    val weekdaySchedule =
        createSchedule(
            id = 1,
            name = "Regular Weekday Schedule",
            departures = RegularWeekdayDepartures
        )

    val weekdayScheduleTiming = ScheduleTiming(
        scheduleId = 1,
        startDay = MONDAY,
        startTime = MIDNIGHT,
        endDay = SATURDAY,
        endTime = MIDNIGHT
    )

    val saturdaySchedule =
        createSchedule(
            id = 2,
            name = "Regular Saturday Schedule",
            departures = RegularSaturdayDepartures
        )

    val saturdayScheduleTiming = ScheduleTiming(
        scheduleId = 2,
        startDay = SATURDAY,
        startTime = MIDNIGHT,
        endDay = SUNDAY,
        endTime = MIDNIGHT
    )

    val sundaySchedule =
        createSchedule(
            id = 3,
            name = "Regular Sunday Schedule",
            departures = RegularSundayDepartures
        )

    val sundayScheduleTiming = ScheduleTiming(
        scheduleId = 2,
        startDay = SUNDAY,
        startTime = MIDNIGHT,
        endDay = MONDAY,
        endTime = MIDNIGHT
    )

    val schedules = Schedules(
        validFrom = LocalDate(2024, APRIL, 7).atTime(MIDNIGHT),
        validTo = null,
        schedules = listOf(weekdaySchedule, saturdaySchedule, sundaySchedule),
        timings = listOf(weekdayScheduleTiming, saturdayScheduleTiming, sundayScheduleTiming),
        name = "regular",
    )

    println(json.encodeToString(schedules))
}

private fun printOverrideSchedule() {
    val satSchedule =
        createSchedule(id = 10, name = "Aug 31 Schedule", departures = AdjustedSaturdayDepartures)

    val saturdayScheduleTiming = ScheduleTiming(
        scheduleId = satSchedule.id,
        startDay = SATURDAY,
        startTime = MIDNIGHT,
        endDay = SUNDAY,
        endTime = MIDNIGHT
    )

    val sunSchedule =
        createSchedule(id = 11, name = "Sep 1 Schedule", departures = AdjustedSundayDepartures)

    val sundayScheduleTiming = ScheduleTiming(
        scheduleId = sunSchedule.id,
        startDay = SUNDAY,
        startTime = MIDNIGHT,
        endDay = MONDAY,
        endTime = MIDNIGHT
    )

    val monSchedule =
        createSchedule(id = 12, name = "Sep 2 Schedule", departures = AdjustedMondayDepartures)

    val mondayScheduleTiming = ScheduleTiming(
        scheduleId = monSchedule.id,
        startDay = MONDAY,
        startTime = MIDNIGHT,
        endDay = TUESDAY,
        endTime = MIDNIGHT
    )

    val schedules = Schedules(
        validFrom = LocalDate(2024, AUGUST, 31).atTime(MIDNIGHT),
        validTo = LocalDate(2024, SEPTEMBER, 3).atTime(MIDNIGHT),
        schedules = listOf(satSchedule, sunSchedule, monSchedule),
        timings = listOf(saturdayScheduleTiming, sundayScheduleTiming, mondayScheduleTiming),
        name = "Labor Day Weekend"
    )

    println(json.encodeToString(schedules))
}

private fun createSchedule(id: Int, name: String, departures: Departures): Schedule {
    return Schedule(
        id = id,
        name = name,
        departures = mapOf(
            "NWK_WTC" to parseTimes(departures.nwkWtc),
            "WTC_NWK" to parseTimes(departures.wtcNewark),
            "JSQ_33S" to parseTimes(departures.jsq33s),
            "33S_JSQ" to parseTimes(departures.s33Jsq),
            "JSQ_HOB_33S" to parseTimes(departures.jsqHob33s),
            "33S_HOB_JSQ" to parseTimes(departures.s33HobJsq),
            "WTC_HOB" to parseTimes(departures.wtcHob),
            "HOB_WTC" to parseTimes(departures.hobWtc),
            "HOB_33S" to parseTimes(departures.hob33s),
            "33S_HOB" to parseTimes(departures.s33Hob),
            "WTC_JSQ" to parseTimes(departures.wtcJsq),
            "JSQ_WTC" to parseTimes(departures.jsqWtc),
            "NWK_HAR" to parseTimes(departures.nwkHar),
            "HAR_NWK" to parseTimes(departures.harNwk),
        ).filterValues { it.isNotEmpty() },
        firstSlowDepartureTime = departures.firstSlowDepartureTime,
        lastSlowDepartureTime = departures.lastSlowDepartureTime,
    )
}

private fun parseTimes(times: String): List<LocalTime> {
    val results = mutableListOf<LocalTime>()
    times.split("\n")
        .map { it.trim() }
        .forEachIndexed { index, line ->
            val mIndex = line.indexOfFirst { it == 'M' }
            if (mIndex < 0) return@forEachIndexed

            val origin = line.substring(0, mIndex + 1)

            val (rawTime, amPm) = origin.split(" ")
            val (rawHour, rawMinute) =
                rawTime.split(":")

            var hour = rawHour.toInt()
            when {
                amPm == "PM" && hour < 12 -> hour += 12
                amPm == "AM" && hour == 12 -> hour = 0
            }

            val time = LocalTime(hour, rawMinute.toInt())
            results += time
        }
    return results
}

private val MIDNIGHT = LocalTime(0, 0)
