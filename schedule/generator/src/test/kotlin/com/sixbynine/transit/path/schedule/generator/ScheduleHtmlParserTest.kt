package com.sixbynine.transit.path.schedule.generator

import kotlin.test.Test
import kotlin.test.assertNotNull

class ScheduleHtmlParserTest {
    @Test
    fun `parse nwk wtc`() {
        parseAndPrint("nwkWtcAccordion.html")
    }

    @Test
    fun `parse jsq hob 33s`() {
        parseAndPrint("jsq33sAccordion.html")
    }

    private fun parseAndPrint(filename: String) {
        val html = ScheduleHtmlParserTest::class.java.getResource(filename)!!.readText()
        val result = ScheduleHtmlParser.parseDepartures(html, verboseLogging = false)
        assertNotNull(result)

        println("Route:")
        println("\tLines:${result.first.lines}")
        println("\tStations:${result.first.stops.map { it.pathApiName }}")
        println()
        println("Departures:")
        println(result.second)
    }
}