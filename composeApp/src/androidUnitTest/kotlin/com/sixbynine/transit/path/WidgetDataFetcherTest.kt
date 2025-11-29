package com.sixbynine.transit.path

import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.TrainFilter.All
import com.sixbynine.transit.path.test.TestSetupHelper
import com.sixbynine.transit.path.time.NewYorkTimeZone
import com.sixbynine.transit.path.util.Staleness
import com.sixbynine.transit.path.util.await
import com.sixbynine.transit.path.widget.WidgetDataFetcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.junit.Before
import org.junit.Test
import java.time.Month.AUGUST
import kotlin.time.Duration.Companion.INFINITE

class WidgetDataFetcherTest {

    @Before
    fun setUp() {
        TestSetupHelper.setUp()
    }

    @Test
    fun foo() = runTest {
        val data = WidgetDataFetcher.fetchWidgetDataWithPrevious(
            stationLimit = 1,
            stations = listOf(Stations.JournalSquare),
            lines = Line.permanentLines.toSet(),
            sort = StationSort.Alphabetical,
            filter = All,
            includeClosestStation = false,
            staleness = Staleness(staleAfter = INFINITE, invalidAfter = INFINITE),
            now = Aug24.atTime(21, 40).toInstant(NewYorkTimeZone),
            isCommuteWidget = false,
        ).await()

        val trains =
            data.data!!
                .stations
                .single()
                .signs
                .single { it.title == "World Trade Center" }
                .projectedArrivals
                .map { it.toLocalDateTime(NewYorkTimeZone).time.toString().take(5) }

        println(trains)
    }

    companion object {
        val Aug24 = LocalDate(2024, AUGUST, 24)
    }
}