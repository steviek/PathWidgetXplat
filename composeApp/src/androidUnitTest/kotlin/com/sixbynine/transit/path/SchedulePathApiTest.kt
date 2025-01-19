package com.sixbynine.transit.path

import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.impl.SchedulePathApi
import com.sixbynine.transit.path.test.TestSetupHelper
import com.sixbynine.transit.path.time.NewYorkTimeZone
import com.sixbynine.transit.path.util.Staleness
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.junit.Before
import org.junit.Test
import java.time.Month.AUGUST
import kotlin.test.assertEquals
import kotlin.time.Duration

class SchedulePathApiTest {

    @Before
    fun setUp() {
        TestSetupHelper.setUp()
    }

    @Test
    fun `slower mid-day train`() = runTest {
        val departures = getUpcomingDepartures(Aug24.atTime(20, 0)).fetch.await().data!!
        val expTrains =
            departures
                .getTrainsAt(Stations.ExchangePlace)
                .orEmpty()
                .filter { it.headsign == "World Trade Center" }
                .map {
                    it.projectedArrival.toLocalDateTime(NewYorkTimeZone).time.toString().take(5)
                }

        assertEquals(listOf("20:14", "20:34", "20:54", "21:14"), expTrains)
    }

    @Test
    fun `faster overnight train`() = runTest {
        val departures = getUpcomingDepartures(Aug24.atTime(3, 0)).fetch.await().data!!
        val expTrains =
            departures
                .getTrainsAt(Stations.ExchangePlace)
                .orEmpty()
                .filter { it.headsign == "World Trade Center" }
                .map {
                    it.projectedArrival.toLocalDateTime(NewYorkTimeZone).time.toString().take(5)
                }

        assertEquals(listOf("03:30", "04:10"), expTrains)
    }

    @Test
    fun `jsq trains`() = runTest {
        val departures = getUpcomingDepartures(Aug24.atTime(20, 0)).fetch.await().data!!
        val jsqTrains =
            departures
                .getTrainsAt(Stations.JournalSquare)
                .orEmpty()
                .filter { it.headsign == "World Trade Center" }
                .map {
                    it.projectedArrival.toLocalDateTime(NewYorkTimeZone).time.toString().take(5)
                }

        assertEquals(listOf("20:06", "20:26", "20:46", "21:06", "21:26"), jsqTrains)
    }

    @Test
    fun `jsq trains 2`() = runTest {
        val departures = getUpcomingDepartures(Aug24.atTime(21, 40)).fetch.await().data!!
        val jsqTrains =
            departures
                .getTrainsAt(Stations.JournalSquare)
                .orEmpty()
                .filter { it.headsign == "World Trade Center" }
                .map {
                    it.projectedArrival.toLocalDateTime(NewYorkTimeZone).time.toString().take(5)
                }

        assertEquals(listOf("21:46", "22:06", "22:26", "22:46", "23:06"), jsqTrains)
    }

    private fun getUpcomingDepartures(dateTime: LocalDateTime) =
        SchedulePathApi().getUpcomingDepartures(
            dateTime.toInstant(NewYorkTimeZone),
            Staleness(staleAfter = Duration.INFINITE, invalidAfter = Duration.INFINITE)
        )

    companion object {
        val Aug24 = LocalDate(2024, AUGUST, 24)
    }
}
