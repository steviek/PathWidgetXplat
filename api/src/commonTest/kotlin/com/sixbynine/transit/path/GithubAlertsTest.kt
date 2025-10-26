package com.sixbynine.transit.path

import com.sixbynine.transit.path.api.Stations.ExchangePlace
import com.sixbynine.transit.path.api.Stations.FourteenthStreet
import com.sixbynine.transit.path.api.Stations.GroveStreet
import com.sixbynine.transit.path.api.Stations.Harrison
import com.sixbynine.transit.path.api.Stations.JournalSquare
import com.sixbynine.transit.path.api.Stations.Newark
import com.sixbynine.transit.path.api.Stations.NinthStreet
import com.sixbynine.transit.path.api.Stations.TwentyThirdStreet
import com.sixbynine.transit.path.api.Stations.WorldTradeCenter
import com.sixbynine.transit.path.api.alerts.Alert
import com.sixbynine.transit.path.api.alerts.AlertText
import com.sixbynine.transit.path.api.alerts.Schedule
import com.sixbynine.transit.path.api.alerts.TrainFilter
import com.sixbynine.transit.path.api.alerts.canHideTrainsAt
import com.sixbynine.transit.path.api.alerts.github.GithubAlerts
import com.sixbynine.transit.path.api.alerts.hidesTrain
import com.sixbynine.transit.path.util.JsonFormat
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.DayOfWeek.FRIDAY
import kotlinx.datetime.DayOfWeek.MONDAY
import kotlinx.datetime.DayOfWeek.SATURDAY
import kotlinx.datetime.DayOfWeek.SUNDAY
import kotlinx.datetime.DayOfWeek.WEDNESDAY
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month.APRIL
import kotlinx.datetime.Month.DECEMBER
import kotlinx.datetime.Month.FEBRUARY
import kotlinx.datetime.Month.JULY
import kotlinx.datetime.Month.JUNE
import kotlinx.datetime.Month.NOVEMBER
import kotlinx.datetime.Month.OCTOBER
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GithubAlertsTest {
    @Test
    fun `current alerts text`() {
        val alerts = GithubAlerts(
            GeneralOvernightCleaning,
            FourteenthStreetOvernight,
            ConrailDemolition1,
            ConrailDemolition2,
            ConrailDemolitionGlobal,
            WeekendOvernightCleaning,
        )

        val json = JsonFormat.encodeToString(alerts)

        assertEquals(alerts, JsonFormat.decodeFromString(json))

        println(json)
    }

    @Test
    fun `active at repeating weekly`() {
        val alert = Alert(
            stations = listOf(GroveStreet),
            hideTrainsSchedule = Schedule.repeatingWeekly(
                startDay = SATURDAY,
                startTime = LocalTime(6, 0),
                endDay = MONDAY,
                endTime = LocalTime(0, 0),
                from = LocalDate(2024, APRIL, 6),
                to = LocalDate(2024, JUNE, 30),
            ),
            hiddenTrainsFilter = TrainFilter.headSigns("33rd", "World Trade"),
        )

        assertTrue(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 6, 6, 0)))
        assertTrue(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 7, 10, 0)))
        assertTrue(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 7, 14, 0)))
        assertTrue(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 20, 14, 0)))
        assertFalse(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 11, 14, 0)))
        assertFalse(alert.canHideTrainsAt(LocalDateTime(2024, JULY, 6, 14, 0)))
    }

    @Test
    fun `active at repeating daily`() {
        val alert = Alert(
            stations = listOf(GroveStreet),
            hideTrainsSchedule = Schedule.repeatingDaily(
                days = listOf(WEDNESDAY),
                start = LocalTime(6, 0),
                end = LocalTime(10, 0),
                from = LocalDate(2024, APRIL, 6),
                to = LocalDate(2024, JUNE, 30),
            ),
            hiddenTrainsFilter = TrainFilter.headSigns("33rd", "World Trade"),
        )

        assertTrue(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 10, 6, 0)))
        assertFalse(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 10, 10, 0)))
    }

    @Test
    fun `active at same start and end time`() {
        val alert = Alert(
            stations = listOf(GroveStreet),
            hideTrainsSchedule = Schedule.repeatingDaily(
                days = listOf(SATURDAY),
                start = LocalTime(0, 0),
                end = LocalTime(0, 0),
                from = LocalDate(2024, APRIL, 6),
                to = LocalDate(2024, JUNE, 30),
            ),
            hiddenTrainsFilter = TrainFilter.headSigns("33rd", "World Trade"),
        )

        assertTrue(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 6, 0, 0)))
        assertTrue(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 6, 6, 0)))
        assertTrue(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 6, 23, 0)))
        assertFalse(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 5, 23, 0)))
        assertFalse(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 7, 0, 0)))
    }

    @Test
    fun `active at same start and end time not midnight`() {
        val alert = Alert(
            stations = listOf(GroveStreet),
            hideTrainsSchedule = Schedule.repeatingDaily(
                days = listOf(SATURDAY),
                start = LocalTime(4, 0),
                end = LocalTime(4, 0),
                from = LocalDate(2024, APRIL, 6),
                to = LocalDate(2024, JUNE, 30),
            ),
            hiddenTrainsFilter = TrainFilter.headSigns("33rd", "World Trade"),
        )

        assertFalse(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 5, 23, 0)))
        assertFalse(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 6, 0, 0)))
        assertTrue(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 6, 6, 0)))
        assertTrue(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 6, 23, 0)))
        assertTrue(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 7, 0, 0)))
        assertTrue(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 7, 3, 0)))
        assertFalse(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 7, 4, 0)))
    }

    @Test
    fun `active at repeating daily overnight`() {
        val alert = Alert(
            stations = listOf(GroveStreet),
            hideTrainsSchedule = Schedule.repeatingDaily(
                days = listOf(WEDNESDAY, FRIDAY),
                start = LocalTime(22, 0),
                end = LocalTime(10, 0),
                from = LocalDate(2024, APRIL, 6),
                to = LocalDate(2024, JUNE, 30),
            ),
            hiddenTrainsFilter = TrainFilter.headSigns("33rd", "World Trade"),
        )

        // Wednesday at 23:00
        assertTrue(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 10, 23, 0)))
        // Thursday at 09:00
        assertTrue(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 11, 9, 0)))
        // Thursday at 10:00
        assertFalse(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 11, 10, 0)))
        // Thursday at 23:00
        assertFalse(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 11, 23, 0)))
        // Friday at 23:00
        assertTrue(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 12, 23, 0)))
        // Saturday at midnight
        assertTrue(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 13, 0, 0)))
        // Sunday at midnight
        assertFalse(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 14, 0, 0)))
    }

    @Test
    fun `active at once`() {
        val alert = Alert(
            stations = listOf(GroveStreet),
            hideTrainsSchedule = Schedule.once(
                from = LocalDateTime(2024, APRIL, 6, 10, 0),
                to = LocalDateTime(2024, JUNE, 30, 10, 0),
            ),
            hiddenTrainsFilter = TrainFilter.headSigns("33rd", "World Trade"),
        )

        assertTrue(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 10, 6, 0)))
        assertTrue(alert.canHideTrainsAt(LocalDateTime(2024, APRIL, 15, 10, 0)))
        assertTrue(alert.canHideTrainsAt(LocalDateTime(2024, JUNE, 30, 8, 0)))
        assertFalse(alert.canHideTrainsAt(LocalDateTime(2024, JUNE, 30, 11, 0)))
    }

    @Test
    fun `hidesTrain with headsign`() {
        val alert = Alert(
            stations = listOf(GroveStreet),
            hideTrainsSchedule = Schedule(),
            hiddenTrainsFilter = TrainFilter.headSigns("33rd", "World Trade"),
        )

        assertFalse(alert.hidesTrain("JSQ", "33rd"))
        assertFalse(alert.hidesTrain("GRV", "Newark"))
        assertTrue(alert.hidesTrain("GRV", "33rd"))
        assertTrue(alert.hidesTrain("GRV", "33RD"))
        assertTrue(alert.hidesTrain("GRV", "33rd St via Hoboken"))
        assertTrue(alert.hidesTrain("GRV", "World Trade Center"))
    }

    @Test
    fun `hidesTrain with all`() {
        val alert = Alert(
            stations = listOf(NinthStreet, TwentyThirdStreet),
            hideTrainsSchedule = Schedule(),
            hiddenTrainsFilter = TrainFilter.all(),
        )

        assertFalse(alert.hidesTrain("14S", "33rd St"))
        assertTrue(alert.hidesTrain("09S", "33rd St"))
        assertTrue(alert.hidesTrain("23S", "33rd St"))
        assertTrue(alert.hidesTrain("23S", "Hoboken"))
    }

    private companion object {
        val GeneralOvernightCleaning = Alert(
            stations = listOf(NinthStreet, TwentyThirdStreet),
            hideTrainsSchedule = Schedule.repeatingDaily(
                days = DayOfWeek.entries,
                start = LocalTime(0, 0),
                end = LocalTime(5, 0),
                from = LocalDate(2025, OCTOBER, 14),
                to = LocalDate(2026, DECEMBER, 31),
            ),
            displaySchedule = Schedule.repeatingDaily(
                days = DayOfWeek.entries,
                start = LocalTime(22, 0),
                end = LocalTime(5, 0),
                from = LocalDate(2025, OCTOBER, 13),
                to = LocalDate(2026, DECEMBER, 31),
            ),
            hiddenTrainsFilter = TrainFilter.all(),
            message = AlertText(
                en = "9 St. & 23 St. stations are closed daily from 11:59 PM – 5 AM for maintenance-related activity. Please use Christopher St., 14 St., or 33 St. station.",
                es = "Las estaciones de 9 St. y 23 St. están cerradas diariamente de 11:59 p. m. a 5 a. m. para actividades relacionadas con el mantenimiento. Utilice las estaciones de Christopher St., 14 St. o 33 St."
            ),
            url = AlertText(
                en = "https://www.panynj.gov/path/en/schedules-maps.html"
            ),
            level = "WARN"
        )

        val WeekendOvernightCleaning = Alert(
            stations = listOf(NinthStreet, TwentyThirdStreet),
            hideTrainsSchedule = Schedule(),
            displaySchedule = Schedule.repeatingDaily(
                from = LocalDate(2025, OCTOBER, 10),
                to = LocalDate(2025, OCTOBER, 12),
                days = listOf(FRIDAY, SATURDAY, SUNDAY),
                start = LocalTime(22, 0),
                end = LocalTime(5, 0),
            ),
            hiddenTrainsFilter = TrainFilter(),
            message = AlertText(
                en = "9 St. & 23 St. stations will remain open overnight this weekend",
                es = "Las estaciones de 9 St. y 23 St. permanecerán abiertas durante la noche este fin de semana"
            ),
            url = AlertText(
                en = "https://www.panynj.gov/path/en/schedules-maps/weekend-schedules.html"
            ),
            level = "INFO"
        )

        val FourteenthStreetOvernight = Alert(
            stations = listOf(FourteenthStreet),
            hideTrainsSchedule = Schedule(),
            displaySchedule = Schedule.repeatingDaily(
                days = DayOfWeek.entries,
                start = LocalTime(22, 0),
                end = LocalTime(7, 0),
                from = LocalDate(2024, FEBRUARY, 25),
                to = LocalDate(2026, DECEMBER, 31),
            ),
            hiddenTrainsFilter = TrainFilter(),
            message = AlertText(
                en = "During overnight hours all PATH service at the 14 Street Station may operate from the station’s uptown or downtown track.  Signage is posted at the entrance of the station when this single-track operation is in effect.",
            ),
            url = AlertText(
                en = "https://www.panynj.gov/path/en/planned-service-changes.html",
            ),
            level = "INFO",
        )

        val ConrailDemolition1 = Alert(
            stations = listOf(
                Newark,
                Harrison,
            ),
            hideTrainsSchedule = Schedule.repeatingWeekly(
                startDay = SATURDAY,
                startTime = LocalTime(0, 0),
                endDay = MONDAY,
                endTime = LocalTime(5, 0),
                from = LocalDate(2025, OCTOBER, 24),
                to = LocalDate(2025, NOVEMBER, 6),
            ),
            hiddenTrainsFilter = TrainFilter.headSigns("World Trade Center"),
            displaySchedule = Schedule.repeatingDaily(
                days = listOf(SATURDAY, SUNDAY),
                start = LocalTime(0, 0),
                end = LocalTime(5, 0),
                from = LocalDate(2025, OCTOBER, 24),
                to = LocalDate(2025, NOVEMBER, 6),
            ),
            message = AlertText(
                en = "No service between Harrison and Journal Square. Shuttle buses are running between HAR-JSQ and NWK-JSQ.",
                es = "No hay servicio entre Harrison y Journal Square. Los autobuses están circulando entre HAR-JSQ y NWK-JSQ."
            ),
            url = AlertText(
                en = "https://www.panynj.gov/path/en/schedules-maps.html"
            ),
            level = "INFO"
        )

        val ConrailDemolition2 = ConrailDemolition1.copy(
            stations = listOf(
                JournalSquare,
                GroveStreet,
                ExchangePlace,
                WorldTradeCenter,
            ).map { it.pathApiName },
            hiddenTrainsFilter = TrainFilter.headSigns("Newark"),
        )

        val ConrailDemolitionGlobal = Alert(
            stations = ConrailDemolition1.stations + ConrailDemolition2.stations,
            hideTrainsSchedule = Schedule(),
            hiddenTrainsFilter = TrainFilter(),
            displaySchedule = Schedule.repeatingDaily(
                days = listOf(FRIDAY, SATURDAY),
                start = LocalTime(15, 0),
                end = LocalTime(5, 0),
                from = LocalDate(2025, OCTOBER, 25),
                to = LocalDate(2025, NOVEMBER, 6),
            ),
            message = AlertText(
                en = "No service between Harrison and Journal Square this weekend, starting at midnight Friday night",
                es = "No hay servicio entre Harrison y Journal Square este fin de semana, a partir de la medianoche de la noche del viernes."
            ),
            url = AlertText(
                en = "https://www.panynj.gov/path/en/schedules-maps.html"
            ),
            level = "INFO"
        )
    }
}