import com.sixbynine.transit.path.schedule.DailySchedule
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.DayOfWeek.FRIDAY
import kotlinx.datetime.DayOfWeek.MONDAY
import kotlinx.datetime.DayOfWeek.SATURDAY
import kotlinx.datetime.DayOfWeek.SUNDAY
import kotlinx.datetime.DayOfWeek.THURSDAY
import kotlinx.datetime.DayOfWeek.TUESDAY
import kotlinx.datetime.DayOfWeek.WEDNESDAY
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month.FEBRUARY
import kotlinx.datetime.atTime
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DailyScheduleTest {

    @Test
    fun `non overnight schedule`() {
        val schedule = dailyScheduleOf(9.am, 5.pm, WEEKDAYS)

        assertTrue(schedule.isActiveAt(MONDAY, 10.am))
        assertTrue(schedule.isActiveAt(WEDNESDAY, 2.pm))

        assertFalse(schedule.isActiveAt(SATURDAY, 2.pm))
        assertFalse(schedule.isActiveAt(WEDNESDAY, 10.pm))
        assertFalse(schedule.isActiveAt(SATURDAY, 10.pm))
    }

    @Test
    fun `overnight schedule`() {
        val schedule = dailyScheduleOf(8.pm, 2.am, FRIDAY, SATURDAY)

        assertTrue(schedule.isActiveAt(FRIDAY, 10.pm))
        assertTrue(schedule.isActiveAt(SATURDAY, 1.am))
        assertTrue(schedule.isActiveAt(SUNDAY, 1.am))

        assertFalse(schedule.isActiveAt(MONDAY, 10.am))
        assertFalse(schedule.isActiveAt(MONDAY, 1.am))
        assertFalse(schedule.isActiveAt(FRIDAY, 1.am))
        assertFalse(schedule.isActiveAt(SATURDAY, 2.pm))
        assertFalse(schedule.isActiveAt(SUNDAY, 10.pm))
    }

    @Test
    fun `same start and end`() {
        val schedule = dailyScheduleOf(5.pm, 5.pm, SATURDAY)

        assertTrue(schedule.isActiveAt(SATURDAY, 5.pm))
        assertTrue(schedule.isActiveAt(SATURDAY, 10.pm))
        assertTrue(schedule.isActiveAt(SUNDAY, 2.pm))

        assertFalse(schedule.isActiveAt(SATURDAY, 10.am))
        assertFalse(schedule.isActiveAt(SUNDAY, 5.pm))
    }

    @Test
    fun `same start and end with from and to`() {
        val schedule = dailyScheduleOf(
            start = 5.pm,
            end = 5.pm,
            from = LocalDate(2025, FEBRUARY, 10),
            to = LocalDate(2025, FEBRUARY, 25),
            days = listOf(MONDAY)
        )

        assertFalse(schedule.isActiveAt(LocalDateTime(2025, FEBRUARY, 10, 10, 0)))
        assertTrue(schedule.isActiveAt(LocalDateTime(2025, FEBRUARY, 10, 18, 0)))
    }
}

private val WEEKDAYS = listOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)

private val Int.am: LocalTime
    get() = if (this == 12) LocalTime(0, 0) else LocalTime(this, 0)

private val Int.pm: LocalTime
    get() = if (this == 12) LocalTime(12, 0) else LocalTime(this + 12, 0)

private fun DailySchedule.isActiveAt(dayOfWeek: DayOfWeek, time: LocalTime): Boolean {
    return isActiveAt(LocalDate(2025, FEBRUARY, 10 + dayOfWeek.ordinal).atTime(time))
}

fun dailyScheduleOf(
    start: LocalTime,
    end: LocalTime,
    days: Collection<DayOfWeek>,
    from: LocalDate? = null,
    to: LocalDate? = null
): DailySchedule {
    val set = days as? Set<DayOfWeek> ?: days.toSet()
    return SimpleDailySchedule(start, end, set, from, to)
}

fun dailyScheduleOf(
    start: LocalTime,
    end: LocalTime,
    vararg days: DayOfWeek
): DailySchedule = dailyScheduleOf(start, end, days = days.toSet())

private data class SimpleDailySchedule(
    override val start: LocalTime,
    override val end: LocalTime,
    override val days: Set<DayOfWeek>,
    override val from: LocalDate? = null,
    override val to: LocalDate? = null
) : DailySchedule
