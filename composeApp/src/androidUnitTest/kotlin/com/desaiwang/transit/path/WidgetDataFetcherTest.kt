package com.desaiwang.transit.path

import com.desaiwang.transit.path.api.Line
import com.desaiwang.transit.path.api.StationSort
import com.desaiwang.transit.path.api.Stations
import com.desaiwang.transit.path.api.TrainFilter.All
import com.desaiwang.transit.path.test.TestSetupHelper
import com.desaiwang.transit.path.time.NewYorkTimeZone
import com.desaiwang.transit.path.util.Staleness
import com.desaiwang.transit.path.util.await
import com.desaiwang.transit.path.widget.WidgetDataFetcher
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
            stations = listOf(Stations.FourteenthStreet, Stations.GroveStreet),
            lines = Line.permanentLines.toSet(),
            sort = StationSort.Alphabetical,
            filter = All,
            includeClosestStation = false,
            staleness = Staleness(staleAfter = INFINITE, invalidAfter = INFINITE),
            now = Aug24.atTime(21, 40).toInstant(NewYorkTimeZone),
        ).await()

        // Test is running on Saturday 9:40 PM, which triggers late night/weekend routing
        // We expect JSQ-HOB-33S route which shows "Journal Square via Hoboken"
        val allSigns = data.data!!.stations.flatMap { it.signs }
        val signTitles = allSigns.map { it.title }
        
        println("All available destinations: $signTitles")
        
        // Verify we have the JSQ-HOB-33S route destination
        assert(signTitles.contains("Journal Square via Hoboken")) { 
            "Expected 'Journal Square via Hoboken' destination but found: $signTitles" 
        }
        
        // Verify we don't have regular route destinations (which would indicate regular hours routing)
        assert(!signTitles.contains("World Trade Center")) { 
            "Should not have WTC destination during late night/weekend hours, but found: $signTitles" 
        }
        
        // Test the JSQ-HOB-33S route trains
        val jsqHobTrains = allSigns
            .single { it.title == "Journal Square via Hoboken" }
            .projectedArrivals
            .map { it.toLocalDateTime(NewYorkTimeZone).time.toString().take(5) }

        println("Journal Square via Hoboken trains: $jsqHobTrains")
    }

    @Test
    fun `weekday regular hours routing`() = runTest {
        // Test weekday at 12pm (regular hours) - should use regular routes, not JSQ-HOB-33S
        val weekdayData = WidgetDataFetcher.fetchWidgetDataWithPrevious(
            stationLimit = 1,
            stations = listOf(Stations.FourteenthStreet, Stations.GroveStreet),
            lines = Line.permanentLines.toSet(),
            sort = StationSort.Alphabetical,
            filter = All,
            includeClosestStation = false,
            staleness = Staleness(staleAfter = INFINITE, invalidAfter = INFINITE),
            now = Aug21.atTime(12, 0).toInstant(NewYorkTimeZone), // Wednesday 12pm
        ).await()

        // Test is running on Wednesday 12pm, which should use regular hours routing
        val allSigns = weekdayData.data!!.stations.flatMap { it.signs }
        val signTitles = allSigns.map { it.title }
        
        println("Weekday regular hours destinations: $signTitles")
        
        // Verify we have regular route destinations (Journal Square route)
        assert(signTitles.contains("Journal Square")) { 
            "Expected 'Journal Square' destination during regular hours but found: $signTitles" 
        }
        
        // Verify we don't have the late night/weekend JSQ-HOB-33S route
        assert(!signTitles.contains("Journal Square via Hoboken")) { 
            "Should not have JSQ-HOB-33S route during regular hours, but found: $signTitles" 
        }
        
        // Test the Journal Square route trains
        val jsqTrains = allSigns
            .single { it.title == "Journal Square" }
            .projectedArrivals
            .map { it.toLocalDateTime(NewYorkTimeZone).time.toString().take(5) }

        println("Journal Square trains: $jsqTrains")
    }

    companion object {
        val Aug24 = LocalDate(2024, AUGUST, 24)  // Saturday
        val Aug21 = LocalDate(2024, AUGUST, 21)  // Wednesday
    }
}