package com.desaiwang.transit.path

import com.desaiwang.transit.path.api.Stations
import com.desaiwang.transit.path.api.impl.SchedulePathApi
import com.desaiwang.transit.path.test.TestSetupHelper
import com.desaiwang.transit.path.time.NewYorkTimeZone
import com.desaiwang.transit.path.util.Staleness
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

        assertEquals(listOf("20:00", "20:20", "20:40", "21:00", "21:20"), expTrains)
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

        assertEquals(listOf("20:12", "20:32", "20:52", "21:12"), jsqTrains)
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

        assertEquals(listOf("21:52", "22:12", "22:32", "22:52"), jsqTrains)
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
