package com.sixbynine.transit.path

import com.sixbynine.transit.path.api.path.CitymapperDepartures
import com.sixbynine.transit.path.api.path.CitymapperResponse
import com.sixbynine.transit.path.api.path.PathRepository.PathDestinationMessage
import com.sixbynine.transit.path.api.path.PathRepository.PathServiceResults
import com.sixbynine.transit.path.util.JsonFormat
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CitymapperDeparturesTest {

    private fun convert(json: String): PathServiceResults =
        CitymapperDepartures.toPathServiceResults(
            JsonFormat.decodeFromString<CitymapperResponse>(json)
        )

    private fun PathServiceResults.messages(station: String, label: String): List<PathDestinationMessage> =
        results.single { it.consideredStation == station }
            .destinations.filter { it.label == label }
            .flatMap { it.messages }

    @Test
    fun `hoboken live and scheduled departures`() {
        val results = convert(HobokenResponse)

        val toNy = results.messages("HOB", "ToNY")
        assertEquals(listOf("WTC", "33S", "WTC"), toNy.map { it.target })
        assertEquals(listOf("World Trade Center", "33rd Street", "World Trade Center"), toNy.map { it.headSign })
        assertEquals(listOf("65C100", "4D92FB", "65C100"), toNy.map { it.lineColor })
        // Live countdown is used as-is; scheduled time is made relative to last_updated_time.
        assertEquals(listOf("196", "496", "1137"), toNy.map { it.secondsToArrival })
        assertEquals(listOf("3 min", "8 min", "18 min"), toNy.map { it.arrivalTimeMessage })
        assertTrue(toNy.all { it.lastUpdated == Instant.parse("2026-11-17T22:47:43Z") })

        // The "No current departures" advisory row is dropped.
        assertTrue(results.messages("HOB", "ToNJ").isEmpty())
    }

    @Test
    fun `via hoboken trains match ridepath headsigns and colors`() {
        val results = convert(ChristopherStreetNightResponse)

        val toNj = results.messages("CHR", "ToNJ")
        assertEquals(listOf("Journal Square via Hoboken", "Hoboken"), toNj.map { it.headSign })
        assertEquals(listOf("JSQ", "HOB"), toNj.map { it.target })
        assertEquals(listOf("4D92FB,FF9900", "4D92FB"), toNj.map { it.lineColor })

        val toNy = results.messages("CHR", "ToNY")
        assertEquals(listOf("33rd Street via Hoboken"), toNy.map { it.headSign })
        assertEquals(listOf("4D92FB,FF9900"), toNy.map { it.lineColor })
    }

    @Test
    fun `newark penn keeps only PATH trains`() {
        val results = convert(NewarkPennResponse)

        val toNy = results.messages("NWK", "ToNY")
        assertEquals(listOf("World Trade Center"), toNy.map { it.headSign })
        assertEquals(listOf("D93A30"), toNy.map { it.lineColor })
        // NJ Transit light rail ("Nlr Grove Street") must not be read as a PATH train to Grove St.
        assertTrue(results.messages("NWK", "ToNJ").isEmpty())
    }

    @Test
    fun `unknown stations and error responses produce no results`() {
        assertTrue(convert("""{"error":"Unknown stations."}""").results.isEmpty())
        assertTrue(
            convert(
                """{"stations":[{"station":{"live_code":"PATHSomewhereElse"}}],
                   "last_updated_time":"2026-11-17T17:47:43-05:00"}"""
            ).results.isEmpty()
        )
    }

    @Test
    fun `url requests all thirteen stations`() {
        val ids = CitymapperDepartures.Url.substringAfter("ids=").split(",")
        assertEquals(13, ids.size)
        assertTrue("RailStationNewarkPennStation" in ids)
        assertTrue("PATHHoboken" in ids)
    }

    private companion object {
        val HobokenResponse = """
            {"stations":[{"station":{"name":"Hoboken","coords":[40.735543,-74.028828],
              "live_code":"PATHHoboken","brands":["PATH"]},
             "messages":[],
             "sections":[{"id":"PATHHoboken_x","departure_groupings":[{
               "grouping_id":"Platform_PATHHoboken","direction_name":"33rd Street, Journal Square",
               "departures":[
                 {"destination_name":"33rd Street","route_id":"PATHHob33","pattern_id":"1024_0__Stations","direction_id":"1","time_seconds":496},
                 {"destination_name":"World Trade Center","route_id":"PATHHobwtc","pattern_id":"860_0__Stations","direction_id":"1","time_seconds":196},
                 {"destination_name":"World Trade Center","route_id":"PATHHobwtc","pattern_id":"860_0__Stations","direction_id":"1","scheduled_time":"2026-11-17T18:06:40-05:00"},
                 {"route_id":"PATHJsqhob33","destination_name":"No current departures","is_advisory_message":1}
               ]}],"name":"Departures","departure_types":["now"]}],
             "routes":[
               {"name":"HOB-33","color":"#4d92fb","live_line_code":"PATHHob33","long_name":"Hoboken-33rd Street"},
               {"name":"HOB-WTC","color":"#65c100","live_line_code":"PATHHobwtc","long_name":"Hoboken-World Trade Center"},
               {"name":"JSQ-HOB-33","color":"#ff9900","live_line_code":"PATHJsqhob33","long_name":"Journal Square-33rd Street (Via Hoboken)"}
             ]}],
             "last_updated_time":"2026-11-17T17:47:43-05:00"}
        """.trimIndent()

        val ChristopherStreetNightResponse = """
            {"stations":[{"station":{"name":"Christopher Street","live_code":"PATHChristopherSt"},
             "sections":[{"departure_groupings":[
               {"direction_name":"33rd Street","departures":[
                 {"destination_name":"33rd Street","route_id":"PATHJsqhob33","direction_id":"1","time_seconds":300}
               ]},
               {"direction_name":"Journal Square, Hoboken","departures":[
                 {"destination_name":"Journal Square","route_id":"PATHJsqhob33","direction_id":"0","time_seconds":120},
                 {"destination_name":"Hoboken","route_id":"PATHHob33","direction_id":"0","scheduled_time":"2026-11-18T01:20:00-05:00"}
               ]}
             ]}],
             "routes":[
               {"color":"#4d92fb","live_line_code":"PATHHob33","long_name":"Hoboken-33rd Street"},
               {"color":"#ff9900","live_line_code":"PATHJsqhob33","long_name":"Journal Square-33rd Street (Via Hoboken)"}
             ]}],
             "last_updated_time":"2026-11-18T01:00:00-05:00"}
        """.trimIndent()

        val NewarkPennResponse = """
            {"stations":[{"station":{"name":"Newark Penn Station","live_code":"RailStationNewarkPennStation"},
             "sections":[{"departure_groupings":[
               {"departures":[
                 {"destination_name":"Nlr Grove Street","route_id":"NJTransitLightRailNewarkLightRail","direction_id":"0","time_seconds":60}
               ]},
               {"direction_name":"World Trade Center","departures":[
                 {"destination_name":"World Trade Center","route_id":"PATHNwkwtc","direction_id":"1","time_seconds":249},
                 {"route_id":"PATHNwkwtc","destination_name":"No current departures","is_advisory_message":1}
               ]}
             ]}],
             "routes":[
               {"color":"#00A6E4","live_line_code":"NJTransitLightRailNewarkLightRail","long_name":"Newark Light Rail"},
               {"color":"#d93a30","live_line_code":"PATHNwkwtc","long_name":"Newark-World Trade Center"}
             ]}],
             "last_updated_time":"2026-11-17T18:22:30-05:00"}
        """.trimIndent()
    }
}
