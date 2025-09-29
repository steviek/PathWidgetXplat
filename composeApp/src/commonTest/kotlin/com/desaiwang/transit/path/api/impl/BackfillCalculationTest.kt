package com.desaiwang.transit.path.api.impl

import com.desaiwang.transit.path.api.Station
import com.desaiwang.transit.path.api.Stations
import com.desaiwang.transit.path.api.Stations.ChristopherStreet
import com.desaiwang.transit.path.api.Stations.ExchangePlace
import com.desaiwang.transit.path.api.Stations.FourteenthStreet
import com.desaiwang.transit.path.api.Stations.GroveStreet
import com.desaiwang.transit.path.api.Stations.Harrison
import com.desaiwang.transit.path.api.Stations.Hoboken
import com.desaiwang.transit.path.api.Stations.JournalSquare
import com.desaiwang.transit.path.api.Stations.Newark
import com.desaiwang.transit.path.api.Stations.Newport
import com.desaiwang.transit.path.api.Stations.NinthStreet
import com.desaiwang.transit.path.api.Stations.ThirtyThirdStreet
import com.desaiwang.transit.path.api.Stations.TwentyThirdStreet
import com.desaiwang.transit.path.api.Stations.WorldTradeCenter
import com.desaiwang.transit.path.api.path.PathRepository.PathServiceResults
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class BackfillCalculationTest {
    @Test
    fun `response 1`() {
        processResponse(response1)
    }

    @Test
    fun `response 2`() {
        processResponse(response2)
    }

    @Test
    fun `response 3`() {
        processResponse(response3)
    }

    @Test
    fun `response 4`() {
        processResponse(response4)
    }

    @Test
    fun `response 5`() {
        processResponse(response5)
    }

    private fun processResponse(response: String) {
        val format = Json {
            explicitNulls = false
            ignoreUnknownKeys = true
        }

        val stationToHeadSignsAndTimes =
            mutableMapOf<Station, MutableMap<LineId, MutableList<LocalTime>>>()

        val results = format.decodeFromString<PathServiceResults>(response)
        results.results.forEach { result ->
            val station = Stations.All.first { it.pathApiName == result.consideredStation }
            result.destinations.flatMap { it.messages }.forEach eachMessage@ { message ->
               // if (message.durationToArrival <= Duration.ZERO) return@eachMessage

                val projectedArrival =
                    (message.lastUpdated + message.durationToArrival)
                        .toLocalDateTime(TimeZone.of("America/New_York"))
                        .time
                        .let { LocalTime.fromSecondOfDay(it.toSecondOfDay()) }
                stationToHeadSignsAndTimes
                    .getOrPut(station) { mutableMapOf() }
                    .getOrPut(LineId(message.headSign, message.lineColor)) { arrayListOf() }
                    .add(projectedArrival)
            }
        }

        TrainLine.entries.forEach { line ->
            val stations = line.stations
            var priorStation: Station? = null
            val stationToDeltas = mutableMapOf<Station, List<Duration>>()
            val linesToPrint = arrayListOf<String>()
            stations.forEach eachStation@{ station ->
                val times = stationToHeadSignsAndTimes[station]?.get(line.id) ?: return@eachStation
                val deltas = arrayListOf<Duration>()
                if (priorStation != null) {
                    val timesAtPrior =
                        stationToHeadSignsAndTimes[priorStation]?.get(line.id)
                            ?: return@eachStation

                    times.forEach eachTime@{ time ->
                        val potentialPrior =
                            timesAtPrior
                                .filter { it <= time.minusMinutes(1) }
                                .maxOrNull()
                        if (potentialPrior != null) {
                            deltas += time - potentialPrior
                        }
                    }
                }
                stationToDeltas[station] = deltas

                priorStation = station
                linesToPrint += "\t${station.displayName}: $times, ${deltas.distinct()}"
            }
            if (linesToPrint.isNotEmpty()) {
                println(line.headSign)
                linesToPrint.forEach { println(it) }
                println()
            }
        }
    }

    private companion object {
        operator fun LocalTime.minus(other: LocalTime): Duration {
            val date = LocalDate(2024, JANUARY, 22)
            return if (other < this) {
                date.atTime(this).toInstant(TimeZone.UTC) -
                        date.atTime(other).toInstant(TimeZone.UTC)
            } else {
                LocalDate(2024, JANUARY, 23)
                    .atTime(this)
                    .toInstant(TimeZone.UTC) -
                        date.atTime(other).toInstant(TimeZone.UTC)
            }
        }

        fun LocalTime.minusMinutes(minutes: Int): LocalTime {
            val date = LocalDate(2024, JANUARY, 22)
            return (date.atTime(this).toInstant(TimeZone.UTC) - minutes.minutes)
                .toLocalDateTime(TimeZone.UTC)
                .time
        }

        val newarkToWtc = listOf(
            Newark,
            Harrison,
            JournalSquare,
            GroveStreet,
            ExchangePlace,
            WorldTradeCenter
        )

        private val midtownPathJekyll = listOf(
            ThirtyThirdStreet,
            TwentyThirdStreet,
            FourteenthStreet,
            NinthStreet,
            ChristopherStreet,
            Newport,
            GroveStreet,
            JournalSquare
        )

        private val midtownPathHyde = listOf(
            ThirtyThirdStreet,
            TwentyThirdStreet,
            FourteenthStreet,
            NinthStreet,
            ChristopherStreet,
            Hoboken,
            Newport,
            GroveStreet,
            JournalSquare
        )

        private val hobokenToWtc = listOf(
            Hoboken,
            Newport,
            ExchangePlace,
            WorldTradeCenter
        )

        private val hobokenTo33S = listOf(
            Hoboken,
            ChristopherStreet,
            NinthStreet,
            FourteenthStreet,
            TwentyThirdStreet,
            ThirtyThirdStreet
        )

        private data class LineId(val headSign: String, val lineColor: String)

        private enum class TrainLine(
            val headSign: String,
            val lineColor: String,
            val stations: List<Station>
        ) {
            NWK_WTC("World Trade Center", "D93A30", newarkToWtc),
            WTC_NWK("Newark", "D93A30", newarkToWtc.reversed()),
            OK_33S_JSQ("Journal Square", "FF9900", midtownPathJekyll),
            OK_JSQ_33S("33rd Street", "FF9900", midtownPathJekyll.reversed()),
            PAIN_33S_JSQ("Journal Square via Hoboken", "4D92FB,FF9900", midtownPathHyde),
            PAIN_JSQ_33S("33rd Street via Hoboken", "4D92FB,FF9900", midtownPathHyde.reversed()),
            HOB_WTC("World Trade Center", "65C100", hobokenToWtc),
            WTC_HOB("Hoboken", "65C100", hobokenToWtc.reversed()),
            OK_HOB_33S("33rd Street", "4D92FB", hobokenTo33S),
            OK_33S_HOB("Hoboken", "4D92FB", hobokenTo33S.reversed());

            val id: LineId = LineId(headSign, lineColor)
        }

        private val response1 =
            """
                {
                  "results": [
                    {
                      "consideredStation": "NWK",
                      "destinations": [
                        {
                          "label": "ToNY",
                          "messages": [
                            {
                              "target": "WTC",
                              "secondsToArrival": "454",
                              "arrivalTimeMessage": "8 min",
                              "lineColor": "D93A30",
                              "headSign": "World Trade Center",
                              "lastUpdated": "2024-01-21T20:52:25.740338-05:00"
                            },
                            {
                              "target": "WTC",
                              "secondsToArrival": "1654",
                              "arrivalTimeMessage": "28 min",
                              "lineColor": "D93A30",
                              "headSign": "World Trade Center",
                              "lastUpdated": "2024-01-21T20:52:25.740338-05:00"
                            }
                          ]
                        }
                      ]
                    },
                    {
                      "consideredStation": "HAR",
                      "destinations": [
                        {
                          "label": "ToNJ",
                          "messages": [
                            {
                              "target": "NWK",
                              "secondsToArrival": "302",
                              "arrivalTimeMessage": "5 min",
                              "lineColor": "D93A30",
                              "headSign": "Newark",
                              "lastUpdated": "2024-01-21T20:52:20.459978-05:00"
                            },
                            {
                              "target": "NWK",
                              "secondsToArrival": "1191",
                              "arrivalTimeMessage": "20 min",
                              "lineColor": "D93A30",
                              "headSign": "Newark",
                              "lastUpdated": "2024-01-21T20:52:20.459978-05:00"
                            }
                          ]
                        },
                        {
                          "label": "ToNY",
                          "messages": [
                            {
                              "target": "WTC",
                              "secondsToArrival": "591",
                              "arrivalTimeMessage": "10 min",
                              "lineColor": "D93A30",
                              "headSign": "World Trade Center",
                              "lastUpdated": "2024-01-21T20:51:50.446158-05:00"
                            },
                            {
                              "target": "WTC",
                              "secondsToArrival": "1791",
                              "arrivalTimeMessage": "30 min",
                              "lineColor": "D93A30",
                              "headSign": "World Trade Center",
                              "lastUpdated": "2024-01-21T20:51:50.446158-05:00"
                            }
                          ]
                        }
                      ]
                    },
                    {
                      "consideredStation": "JSQ",
                      "destinations": [
                        {
                          "label": "ToNJ",
                          "messages": [
                            {
                              "target": "NWK",
                              "secondsToArrival": "767",
                              "arrivalTimeMessage": "13 min",
                              "lineColor": "D93A30",
                              "headSign": "Newark",
                              "lastUpdated": "2024-01-21T20:51:50.446158-05:00"
                            },
                            {
                              "target": "NWK",
                              "secondsToArrival": "1967",
                              "arrivalTimeMessage": "33 min",
                              "lineColor": "D93A30",
                              "headSign": "Newark",
                              "lastUpdated": "2024-01-21T20:51:50.446158-05:00"
                            }
                          ]
                        },
                        {
                          "label": "ToNY",
                          "messages": [
                            {
                              "target": "WTC",
                              "secondsToArrival": "44",
                              "arrivalTimeMessage": "1 min",
                              "lineColor": "D93A30",
                              "headSign": "World Trade Center",
                              "lastUpdated": "2024-01-21T20:51:40.435601-05:00"
                            },
                            {
                              "target": "33S",
                              "secondsToArrival": "200",
                              "arrivalTimeMessage": "3 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "33rd Street via Hoboken",
                              "lastUpdated": "2024-01-21T20:51:40.435601-05:00"
                            },
                            {
                              "target": "WTC",
                              "secondsToArrival": "1262",
                              "arrivalTimeMessage": "21 min",
                              "lineColor": "D93A30",
                              "headSign": "World Trade Center",
                              "lastUpdated": "2024-01-21T20:51:40.435601-05:00"
                            },
                            {
                              "target": "33S",
                              "secondsToArrival": "1400",
                              "arrivalTimeMessage": "23 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "33rd Street via Hoboken",
                              "lastUpdated": "2024-01-21T20:51:40.435601-05:00"
                            }
                          ]
                        }
                      ]
                    },
                    {
                      "consideredStation": "GRV",
                      "destinations": [
                        {
                          "label": "ToNY",
                          "messages": [
                            {
                              "target": "WTC",
                              "secondsToArrival": "293",
                              "arrivalTimeMessage": "5 min",
                              "lineColor": "D93A30",
                              "headSign": "World Trade Center",
                              "lastUpdated": "2024-01-21T20:52:25.740338-05:00"
                            },
                            {
                              "target": "33S",
                              "secondsToArrival": "436",
                              "arrivalTimeMessage": "7 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "33rd Street via Hoboken",
                              "lastUpdated": "2024-01-21T20:52:25.740338-05:00"
                            }
                          ]
                        }
                      ]
                    },
                    {
                      "consideredStation": "NEW",
                      "destinations": [
                        {
                          "label": "ToNJ",
                          "messages": [
                            {
                              "target": "JSQ",
                              "secondsToArrival": "721",
                              "arrivalTimeMessage": "12 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "Journal Square via Hoboken",
                              "lastUpdated": "2024-01-21T20:52:06.706513-05:00"
                            },
                            {
                              "target": "JSQ",
                              "secondsToArrival": "1475",
                              "arrivalTimeMessage": "25 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "Journal Square via Hoboken",
                              "lastUpdated": "2024-01-21T20:52:06.706513-05:00"
                            }
                          ]
                        },
                        {
                          "label": "ToNY",
                          "messages": [
                            {
                              "target": "33S",
                              "secondsToArrival": "681",
                              "arrivalTimeMessage": "12 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "33rd Street via Hoboken",
                              "lastUpdated": "2024-01-21T20:52:20.459978-05:00"
                            },
                            {
                              "target": "33S",
                              "secondsToArrival": "1881",
                              "arrivalTimeMessage": "32 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "33rd Street via Hoboken",
                              "lastUpdated": "2024-01-21T20:52:20.459978-05:00"
                            }
                          ]
                        }
                      ]
                    },
                    {
                      "consideredStation": "EXP",
                      "destinations": [
                        {
                          "label": "ToNJ",
                          "messages": [
                            {
                              "target": "NWK",
                              "secondsToArrival": "383",
                              "arrivalTimeMessage": "7 min",
                              "lineColor": "D93A30",
                              "headSign": "Newark",
                              "lastUpdated": "2024-01-21T20:52:00.729465-05:00"
                            }
                          ]
                        },
                        {
                          "label": "ToNY",
                          "messages": [
                            {
                              "target": "WTC",
                              "secondsToArrival": "474",
                              "arrivalTimeMessage": "8 min",
                              "lineColor": "D93A30",
                              "headSign": "World Trade Center",
                              "lastUpdated": "2024-01-21T20:52:15.762083-05:00"
                            }
                          ]
                        }
                      ]
                    },
                    {
                      "consideredStation": "HOB",
                      "destinations": [
                        {
                          "label": "ToNJ",
                          "messages": [
                            {
                              "target": "JSQ",
                              "secondsToArrival": "515",
                              "arrivalTimeMessage": "9 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "Journal Square via Hoboken",
                              "lastUpdated": "2024-01-21T20:51:50.446158-05:00"
                            },
                            {
                              "target": "JSQ",
                              "secondsToArrival": "1269",
                              "arrivalTimeMessage": "21 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "Journal Square via Hoboken",
                              "lastUpdated": "2024-01-21T20:51:50.446158-05:00"
                            }
                          ]
                        },
                        {
                          "label": "ToNY",
                          "messages": [
                            {
                              "target": "33S",
                              "secondsToArrival": "33",
                              "arrivalTimeMessage": "1 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "33rd Street via Hoboken",
                              "lastUpdated": "2024-01-21T20:52:20.459978-05:00"
                            },
                            {
                              "target": "33S",
                              "secondsToArrival": "1179",
                              "arrivalTimeMessage": "20 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "33rd Street via Hoboken",
                              "lastUpdated": "2024-01-21T20:52:20.459978-05:00"
                            }
                          ]
                        }
                      ]
                    },
                    {
                      "consideredStation": "WTC",
                      "destinations": [
                        {
                          "label": "ToNJ",
                          "messages": [
                            {
                              "target": "NWK",
                              "secondsToArrival": "179",
                              "arrivalTimeMessage": "3 min",
                              "lineColor": "D93A30",
                              "headSign": "Newark",
                              "lastUpdated": "2024-01-21T20:52:00.729465-05:00"
                            },
                            {
                              "target": "NWK",
                              "secondsToArrival": "1379",
                              "arrivalTimeMessage": "23 min",
                              "lineColor": "D93A30",
                              "headSign": "Newark",
                              "lastUpdated": "2024-01-21T20:52:00.729465-05:00"
                            }
                          ]
                        }
                      ]
                    },
                    {
                      "consideredStation": "CHR",
                      "destinations": [
                        {
                          "label": "ToNJ",
                          "messages": [
                            {
                              "target": "JSQ",
                              "secondsToArrival": "482",
                              "arrivalTimeMessage": "8 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "Journal Square via Hoboken",
                              "lastUpdated": "2024-01-21T20:51:40.435601-05:00"
                            },
                            {
                              "target": "JSQ",
                              "secondsToArrival": "1682",
                              "arrivalTimeMessage": "28 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "Journal Square via Hoboken",
                              "lastUpdated": "2024-01-21T20:51:40.435601-05:00"
                            }
                          ]
                        },
                        {
                          "label": "ToNY",
                          "messages": [
                            {
                              "target": "33S",
                              "secondsToArrival": "629",
                              "arrivalTimeMessage": "11 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "33rd Street via Hoboken",
                              "lastUpdated": "2024-01-21T20:51:50.446158-05:00"
                            },
                            {
                              "target": "33S",
                              "secondsToArrival": "1731",
                              "arrivalTimeMessage": "29 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "33rd Street via Hoboken",
                              "lastUpdated": "2024-01-21T20:51:50.446158-05:00"
                            }
                          ]
                        }
                      ]
                    },
                    {
                      "consideredStation": "09S",
                      "destinations": [
                        {
                          "label": "ToNJ",
                          "messages": [
                            {
                              "target": "JSQ",
                              "secondsToArrival": "362",
                              "arrivalTimeMessage": "6 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "Journal Square via Hoboken",
                              "lastUpdated": "2024-01-21T20:51:40.435601-05:00"
                            },
                            {
                              "target": "JSQ",
                              "secondsToArrival": "1562",
                              "arrivalTimeMessage": "26 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "Journal Square via Hoboken",
                              "lastUpdated": "2024-01-21T20:51:40.435601-05:00"
                            }
                          ]
                        },
                        {
                          "label": "ToNY",
                          "messages": [
                            {
                              "target": "33S",
                              "secondsToArrival": "729",
                              "arrivalTimeMessage": "12 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "33rd Street via Hoboken",
                              "lastUpdated": "2024-01-21T20:52:10.454449-05:00"
                            },
                            {
                              "target": "33S",
                              "secondsToArrival": "1831",
                              "arrivalTimeMessage": "31 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "33rd Street via Hoboken",
                              "lastUpdated": "2024-01-21T20:52:10.454449-05:00"
                            }
                          ]
                        }
                      ]
                    },
                    {
                      "consideredStation": "14S",
                      "destinations": [
                        {
                          "label": "ToNJ",
                          "messages": [
                            {
                              "target": "JSQ",
                              "secondsToArrival": "286",
                              "arrivalTimeMessage": "5 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "Journal Square via Hoboken",
                              "lastUpdated": "2024-01-21T20:51:55.465399-05:00"
                            },
                            {
                              "target": "JSQ",
                              "secondsToArrival": "1486",
                              "arrivalTimeMessage": "25 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "Journal Square via Hoboken",
                              "lastUpdated": "2024-01-21T20:51:55.465399-05:00"
                            }
                          ]
                        },
                        {
                          "label": "ToNY",
                          "messages": [
                            {
                              "target": "33S",
                              "secondsToArrival": "799",
                              "arrivalTimeMessage": "13 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "33rd Street via Hoboken",
                              "lastUpdated": "2024-01-21T20:52:00.729465-05:00"
                            },
                            {
                              "target": "33S",
                              "secondsToArrival": "1901",
                              "arrivalTimeMessage": "32 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "33rd Street via Hoboken",
                              "lastUpdated": "2024-01-21T20:52:00.729465-05:00"
                            }
                          ]
                        }
                      ]
                    },
                    {
                      "consideredStation": "23S",
                      "destinations": [
                        {
                          "label": "ToNJ",
                          "messages": [
                            {
                              "target": "JSQ",
                              "secondsToArrival": "151",
                              "arrivalTimeMessage": "3 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "Journal Square via Hoboken",
                              "lastUpdated": "2024-01-21T20:52:10.454449-05:00"
                            },
                            {
                              "target": "JSQ",
                              "secondsToArrival": "1351",
                              "arrivalTimeMessage": "23 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "Journal Square via Hoboken",
                              "lastUpdated": "2024-01-21T20:52:10.454449-05:00"
                            }
                          ]
                        },
                        {
                          "label": "ToNY",
                          "messages": [
                            {
                              "target": "33S",
                              "secondsToArrival": "929",
                              "arrivalTimeMessage": "16 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "33rd Street via Hoboken",
                              "lastUpdated": "2024-01-21T20:51:50.446158-05:00"
                            },
                            {
                              "target": "33S",
                              "secondsToArrival": "2031",
                              "arrivalTimeMessage": "34 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "33rd Street via Hoboken",
                              "lastUpdated": "2024-01-21T20:51:50.446158-05:00"
                            }
                          ]
                        }
                      ]
                    },
                    {
                      "consideredStation": "33S",
                      "destinations": [
                        {
                          "label": "ToNJ",
                          "messages": [
                            {
                              "target": "JSQ",
                              "secondsToArrival": "39",
                              "arrivalTimeMessage": "1 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "Journal Square via Hoboken",
                              "lastUpdated": "2024-01-21T20:52:20.459978-05:00"
                            },
                            {
                              "target": "JSQ",
                              "secondsToArrival": "1239",
                              "arrivalTimeMessage": "21 min",
                              "lineColor": "4D92FB,FF9900",
                              "headSign": "Journal Square via Hoboken",
                              "lastUpdated": "2024-01-21T20:52:20.459978-05:00"
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
            """.trimIndent()
    }

    private val response2 =
        """
            {
              "results": [
                {
                  "consideredStation": "NWK",
                  "destinations": [
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "WTC",
                          "secondsToArrival": "557",
                          "arrivalTimeMessage": "9 min",
                          "lineColor": "D93A30",
                          "headSign": "World Trade Center",
                          "lastUpdated": "2024-01-21T16:50:42.8322-05:00"
                        },
                        {
                          "target": "WTC",
                          "secondsToArrival": "1757",
                          "arrivalTimeMessage": "29 min",
                          "lineColor": "D93A30",
                          "headSign": "World Trade Center",
                          "lastUpdated": "2024-01-21T16:50:42.8322-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "HAR",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "NWK",
                          "secondsToArrival": "414",
                          "arrivalTimeMessage": "7 min",
                          "lineColor": "D93A30",
                          "headSign": "Newark",
                          "lastUpdated": "2024-01-21T16:50:22.548511-05:00"
                        },
                        {
                          "target": "NWK",
                          "secondsToArrival": "1309",
                          "arrivalTimeMessage": "22 min",
                          "lineColor": "D93A30",
                          "headSign": "Newark",
                          "lastUpdated": "2024-01-21T16:50:22.548511-05:00"
                        }
                      ]
                    },
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "WTC",
                          "secondsToArrival": "664",
                          "arrivalTimeMessage": "11 min",
                          "lineColor": "D93A30",
                          "headSign": "World Trade Center",
                          "lastUpdated": "2024-01-21T16:50:37.561098-05:00"
                        },
                        {
                          "target": "WTC",
                          "secondsToArrival": "1864",
                          "arrivalTimeMessage": "31 min",
                          "lineColor": "D93A30",
                          "headSign": "World Trade Center",
                          "lastUpdated": "2024-01-21T16:50:37.561098-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "JSQ",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "NWK",
                          "secondsToArrival": "850",
                          "arrivalTimeMessage": "14 min",
                          "lineColor": "D93A30",
                          "headSign": "Newark",
                          "lastUpdated": "2024-01-21T16:50:27.786473-05:00"
                        },
                        {
                          "target": "NWK",
                          "secondsToArrival": "2050",
                          "arrivalTimeMessage": "34 min",
                          "lineColor": "D93A30",
                          "headSign": "Newark",
                          "lastUpdated": "2024-01-21T16:50:27.786473-05:00"
                        }
                      ]
                    },
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "WTC",
                          "secondsToArrival": "84",
                          "arrivalTimeMessage": "2 min",
                          "lineColor": "D93A30",
                          "headSign": "World Trade Center",
                          "lastUpdated": "2024-01-21T16:50:12.777306-05:00"
                        },
                        {
                          "target": "33S",
                          "secondsToArrival": "587",
                          "arrivalTimeMessage": "10 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "33rd Street via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:12.777306-05:00"
                        },
                        {
                          "target": "33S",
                          "secondsToArrival": "1307",
                          "arrivalTimeMessage": "22 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "33rd Street via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:12.777306-05:00"
                        },
                        {
                          "target": "WTC",
                          "secondsToArrival": "1349",
                          "arrivalTimeMessage": "23 min",
                          "lineColor": "D93A30",
                          "headSign": "World Trade Center",
                          "lastUpdated": "2024-01-21T16:50:12.777306-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "GRV",
                  "destinations": [
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "33S",
                          "secondsToArrival": "125",
                          "arrivalTimeMessage": "2 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "33rd Street via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:37.561098-05:00"
                        },
                        {
                          "target": "WTC",
                          "secondsToArrival": "359",
                          "arrivalTimeMessage": "6 min",
                          "lineColor": "D93A30",
                          "headSign": "World Trade Center",
                          "lastUpdated": "2024-01-21T16:50:37.561098-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "NEW",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "JSQ",
                          "secondsToArrival": "632",
                          "arrivalTimeMessage": "11 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "Journal Square via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:52.591801-05:00"
                        },
                        {
                          "target": "JSQ",
                          "secondsToArrival": "1394",
                          "arrivalTimeMessage": "23 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "Journal Square via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:52.591801-05:00"
                        },
                        {
                          "target": "JSQ",
                          "secondsToArrival": "2089",
                          "arrivalTimeMessage": "35 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "Journal Square via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:52.591801-05:00"
                        }
                      ]
                    },
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "33S",
                          "secondsToArrival": "375",
                          "arrivalTimeMessage": "6 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "33rd Street via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:27.786473-05:00"
                        },
                        {
                          "target": "33S",
                          "secondsToArrival": "1094",
                          "arrivalTimeMessage": "18 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "33rd Street via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:27.786473-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "EXP",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "NWK",
                          "secondsToArrival": "491",
                          "arrivalTimeMessage": "8 min",
                          "lineColor": "D93A30",
                          "headSign": "Newark",
                          "lastUpdated": "2024-01-21T16:50:12.777306-05:00"
                        }
                      ]
                    },
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "WTC",
                          "secondsToArrival": "546",
                          "arrivalTimeMessage": "9 min",
                          "lineColor": "D93A30",
                          "headSign": "World Trade Center",
                          "lastUpdated": "2024-01-21T16:50:42.8322-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "HOB",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "JSQ",
                          "secondsToArrival": "420",
                          "arrivalTimeMessage": "7 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "Journal Square via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:42.8322-05:00"
                        },
                        {
                          "target": "JSQ",
                          "secondsToArrival": "1182",
                          "arrivalTimeMessage": "20 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "Journal Square via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:42.8322-05:00"
                        }
                      ]
                    },
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "33S",
                          "secondsToArrival": "85",
                          "arrivalTimeMessage": "Delayed",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "33rd Street via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:33.806549-05:00"
                        },
                        {
                          "target": "33S",
                          "secondsToArrival": "170",
                          "arrivalTimeMessage": "3 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "33rd Street via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:33.806549-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "WTC",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "NWK",
                          "secondsToArrival": "262",
                          "arrivalTimeMessage": "5 min",
                          "lineColor": "D93A30",
                          "headSign": "Newark",
                          "lastUpdated": "2024-01-21T16:50:37.561098-05:00"
                        },
                        {
                          "target": "NWK",
                          "secondsToArrival": "1462",
                          "arrivalTimeMessage": "25 min",
                          "lineColor": "D93A30",
                          "headSign": "Newark",
                          "lastUpdated": "2024-01-21T16:50:37.561098-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "CHR",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "JSQ",
                          "secondsToArrival": "389",
                          "arrivalTimeMessage": "7 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "Journal Square via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:37.561098-05:00"
                        },
                        {
                          "target": "JSQ",
                          "secondsToArrival": "1084",
                          "arrivalTimeMessage": "18 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "Journal Square via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:37.561098-05:00"
                        }
                      ]
                    },
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "33S",
                          "secondsToArrival": "37",
                          "arrivalTimeMessage": "1 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "33rd Street via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:27.786473-05:00"
                        },
                        {
                          "target": "33S",
                          "secondsToArrival": "490",
                          "arrivalTimeMessage": "8 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "33rd Street via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:27.786473-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "09S",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "JSQ",
                          "secondsToArrival": "254",
                          "arrivalTimeMessage": "4 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "Journal Square via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:52.591801-05:00"
                        },
                        {
                          "target": "JSQ",
                          "secondsToArrival": "949",
                          "arrivalTimeMessage": "16 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "Journal Square via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:52.591801-05:00"
                        }
                      ]
                    },
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "33S",
                          "secondsToArrival": "114",
                          "arrivalTimeMessage": "2 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "33rd Street via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:17.548873-05:00"
                        },
                        {
                          "target": "33S",
                          "secondsToArrival": "593",
                          "arrivalTimeMessage": "10 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "33rd Street via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:17.548873-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "14S",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "JSQ",
                          "secondsToArrival": "218",
                          "arrivalTimeMessage": "4 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "Journal Square via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:17.548873-05:00"
                        },
                        {
                          "target": "JSQ",
                          "secondsToArrival": "924",
                          "arrivalTimeMessage": "16 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "Journal Square via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:17.548873-05:00"
                        }
                      ]
                    },
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "33S",
                          "secondsToArrival": "169",
                          "arrivalTimeMessage": "3 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "33rd Street via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:22.548511-05:00"
                        },
                        {
                          "target": "33S",
                          "secondsToArrival": "681",
                          "arrivalTimeMessage": "12 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "33rd Street via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:22.548511-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "23S",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "JSQ",
                          "secondsToArrival": "74",
                          "arrivalTimeMessage": "1 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "Journal Square via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:52.591801-05:00"
                        },
                        {
                          "target": "JSQ",
                          "secondsToArrival": "769",
                          "arrivalTimeMessage": "13 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "Journal Square via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:52.591801-05:00"
                        }
                      ]
                    },
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "33S",
                          "secondsToArrival": "273",
                          "arrivalTimeMessage": "5 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "33rd Street via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:47.563155-05:00"
                        },
                        {
                          "target": "33S",
                          "secondsToArrival": "783",
                          "arrivalTimeMessage": "13 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "33rd Street via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:47.563155-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "33S",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "JSQ",
                          "secondsToArrival": "682",
                          "arrivalTimeMessage": "12 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "Journal Square via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:37.561098-05:00"
                        },
                        {
                          "target": "JSQ",
                          "secondsToArrival": "1402",
                          "arrivalTimeMessage": "24 min",
                          "lineColor": "4D92FB,FF9900",
                          "headSign": "Journal Square via Hoboken",
                          "lastUpdated": "2024-01-21T16:50:37.561098-05:00"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

    val response3 =
        """
            {
              "results": [
                {
                  "consideredStation": "NWK",
                  "destinations": [
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "WTC",
                          "secondsToArrival": "20",
                          "arrivalTimeMessage": "0 min",
                          "lineColor": "D93A30",
                          "headSign": "World Trade Center",
                          "lastUpdated": "2024-01-22T09:05:39.609265-05:00"
                        },
                        {
                          "target": "WTC",
                          "secondsToArrival": "320",
                          "arrivalTimeMessage": "5 min",
                          "lineColor": "D93A30",
                          "headSign": "World Trade Center",
                          "lastUpdated": "2024-01-22T09:05:39.609265-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "HAR",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "NWK",
                          "secondsToArrival": "190",
                          "arrivalTimeMessage": "3 min",
                          "lineColor": "D93A30",
                          "headSign": "Newark",
                          "lastUpdated": "2024-01-22T09:05:04.593336-05:00"
                        },
                        {
                          "target": "NWK",
                          "secondsToArrival": "368",
                          "arrivalTimeMessage": "6 min",
                          "lineColor": "D93A30",
                          "headSign": "Newark",
                          "lastUpdated": "2024-01-22T09:05:04.593336-05:00"
                        }
                      ]
                    },
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "WTC",
                          "secondsToArrival": "157",
                          "arrivalTimeMessage": "3 min",
                          "lineColor": "D93A30",
                          "headSign": "World Trade Center",
                          "lastUpdated": "2024-01-22T09:05:04.593336-05:00"
                        },
                        {
                          "target": "WTC",
                          "secondsToArrival": "457",
                          "arrivalTimeMessage": "8 min",
                          "lineColor": "D93A30",
                          "headSign": "World Trade Center",
                          "lastUpdated": "2024-01-22T09:05:04.593336-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "JSQ",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "NWK",
                          "secondsToArrival": "15",
                          "arrivalTimeMessage": "0 min",
                          "lineColor": "D93A30",
                          "headSign": "Newark",
                          "lastUpdated": "2024-01-22T09:05:34.604757-05:00"
                        },
                        {
                          "target": "NWK",
                          "secondsToArrival": "350",
                          "arrivalTimeMessage": "6 min",
                          "lineColor": "D93A30",
                          "headSign": "Newark",
                          "lastUpdated": "2024-01-22T09:05:34.604757-05:00"
                        }
                      ]
                    },
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "33S",
                          "secondsToArrival": "0",
                          "arrivalTimeMessage": "0 min",
                          "lineColor": "FF9900",
                          "headSign": "33rd Street",
                          "lastUpdated": "2024-01-22T09:05:34.604757-05:00"
                        },
                        {
                          "target": "WTC",
                          "secondsToArrival": "0",
                          "arrivalTimeMessage": "0 min",
                          "lineColor": "D93A30",
                          "headSign": "World Trade Center",
                          "lastUpdated": "2024-01-22T09:05:34.604757-05:00"
                        },
                        {
                          "target": "WTC",
                          "secondsToArrival": "121",
                          "arrivalTimeMessage": "2 min",
                          "lineColor": "D93A30",
                          "headSign": "World Trade Center",
                          "lastUpdated": "2024-01-22T09:05:34.604757-05:00"
                        },
                        {
                          "target": "33S",
                          "secondsToArrival": "205",
                          "arrivalTimeMessage": "4 min",
                          "lineColor": "FF9900",
                          "headSign": "33rd Street",
                          "lastUpdated": "2024-01-22T09:05:34.604757-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "GRV",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "NWK",
                          "secondsToArrival": "90",
                          "arrivalTimeMessage": "2 min",
                          "lineColor": "D93A30",
                          "headSign": "Newark",
                          "lastUpdated": "2024-01-22T09:04:54.580669-05:00"
                        },
                        {
                          "target": "JSQ",
                          "secondsToArrival": "184",
                          "arrivalTimeMessage": "3 min",
                          "lineColor": "FF9900",
                          "headSign": "Journal Square",
                          "lastUpdated": "2024-01-22T09:04:54.580669-05:00"
                        }
                      ]
                    },
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "33S",
                          "secondsToArrival": "272",
                          "arrivalTimeMessage": "5 min",
                          "lineColor": "FF9900",
                          "headSign": "33rd Street",
                          "lastUpdated": "2024-01-22T09:05:39.609265-05:00"
                        },
                        {
                          "target": "WTC",
                          "secondsToArrival": "357",
                          "arrivalTimeMessage": "6 min",
                          "lineColor": "D93A30",
                          "headSign": "World Trade Center",
                          "lastUpdated": "2024-01-22T09:05:39.609265-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "NEW",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "JSQ",
                          "secondsToArrival": "252",
                          "arrivalTimeMessage": "4 min",
                          "lineColor": "FF9900",
                          "headSign": "Journal Square",
                          "lastUpdated": "2024-01-22T09:05:19.857843-05:00"
                        },
                        {
                          "target": "HOB",
                          "secondsToArrival": "278",
                          "arrivalTimeMessage": "5 min",
                          "lineColor": "65C100",
                          "headSign": "Hoboken",
                          "lastUpdated": "2024-01-22T09:05:19.857843-05:00"
                        }
                      ]
                    },
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "WTC",
                          "secondsToArrival": "133",
                          "arrivalTimeMessage": "2 min",
                          "lineColor": "65C100",
                          "headSign": "World Trade Center",
                          "lastUpdated": "2024-01-22T09:05:19.857843-05:00"
                        },
                        {
                          "target": "33S",
                          "secondsToArrival": "363",
                          "arrivalTimeMessage": "6 min",
                          "lineColor": "FF9900",
                          "headSign": "33rd Street",
                          "lastUpdated": "2024-01-22T09:05:19.857843-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "EXP",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "NWK",
                          "secondsToArrival": "133",
                          "arrivalTimeMessage": "2 min",
                          "lineColor": "D93A30",
                          "headSign": "Newark",
                          "lastUpdated": "2024-01-22T09:05:34.604757-05:00"
                        },
                        {
                          "target": "HOB",
                          "secondsToArrival": "307",
                          "arrivalTimeMessage": "5 min",
                          "lineColor": "65C100",
                          "headSign": "Hoboken",
                          "lastUpdated": "2024-01-22T09:05:34.604757-05:00"
                        }
                      ]
                    },
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "WTC",
                          "secondsToArrival": "43",
                          "arrivalTimeMessage": "1 min",
                          "lineColor": "D93A30",
                          "headSign": "World Trade Center",
                          "lastUpdated": "2024-01-22T09:05:24.596167-05:00"
                        },
                        {
                          "target": "WTC",
                          "secondsToArrival": "428",
                          "arrivalTimeMessage": "7 min",
                          "lineColor": "65C100",
                          "headSign": "World Trade Center",
                          "lastUpdated": "2024-01-22T09:05:24.596167-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "HOB",
                  "destinations": [
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "33S",
                          "secondsToArrival": "10",
                          "arrivalTimeMessage": "0 min",
                          "lineColor": "4D92FB",
                          "headSign": "33rd Street",
                          "lastUpdated": "2024-01-22T09:04:49.658201-05:00"
                        },
                        {
                          "target": "WTC",
                          "secondsToArrival": "310",
                          "arrivalTimeMessage": "5 min",
                          "lineColor": "65C100",
                          "headSign": "World Trade Center",
                          "lastUpdated": "2024-01-22T09:04:49.658201-05:00"
                        },
                        {
                          "target": "33S",
                          "secondsToArrival": "490",
                          "arrivalTimeMessage": "8 min",
                          "lineColor": "4D92FB",
                          "headSign": "33rd Street",
                          "lastUpdated": "2024-01-22T09:04:49.658201-05:00"
                        },
                        {
                          "target": "WTC",
                          "secondsToArrival": "790",
                          "arrivalTimeMessage": "13 min",
                          "lineColor": "65C100",
                          "headSign": "World Trade Center",
                          "lastUpdated": "2024-01-22T09:04:49.658201-05:00"
                        },
                        {
                          "target": "WTC",
                          "secondsToArrival": "1270",
                          "arrivalTimeMessage": "21 min",
                          "lineColor": "65C100",
                          "headSign": "World Trade Center",
                          "lastUpdated": "2024-01-22T09:04:49.658201-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "WTC",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "HOB",
                          "secondsToArrival": "125",
                          "arrivalTimeMessage": "2 min",
                          "lineColor": "65C100",
                          "headSign": "Hoboken",
                          "lastUpdated": "2024-01-22T09:04:54.580669-05:00"
                        },
                        {
                          "target": "NWK",
                          "secondsToArrival": "245",
                          "arrivalTimeMessage": "4 min",
                          "lineColor": "D93A30",
                          "headSign": "Newark",
                          "lastUpdated": "2024-01-22T09:04:54.580669-05:00"
                        },
                        {
                          "target": "NWK",
                          "secondsToArrival": "545",
                          "arrivalTimeMessage": "9 min",
                          "lineColor": "D93A30",
                          "headSign": "Newark",
                          "lastUpdated": "2024-01-22T09:04:54.580669-05:00"
                        },
                        {
                          "target": "HOB",
                          "secondsToArrival": "605",
                          "arrivalTimeMessage": "10 min",
                          "lineColor": "65C100",
                          "headSign": "Hoboken",
                          "lastUpdated": "2024-01-22T09:04:54.580669-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "CHR",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "HOB",
                          "secondsToArrival": "110",
                          "arrivalTimeMessage": "Delayed",
                          "lineColor": "4D92FB",
                          "headSign": "Hoboken",
                          "lastUpdated": "2024-01-22T09:05:29.596733-05:00"
                        },
                        {
                          "target": "JSQ",
                          "secondsToArrival": "249",
                          "arrivalTimeMessage": "4 min",
                          "lineColor": "FF9900",
                          "headSign": "Journal Square",
                          "lastUpdated": "2024-01-22T09:05:29.596733-05:00"
                        }
                      ]
                    },
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "33S",
                          "secondsToArrival": "50",
                          "arrivalTimeMessage": "1 min",
                          "lineColor": "4D92FB",
                          "headSign": "33rd Street",
                          "lastUpdated": "2024-01-22T09:05:14.631786-05:00"
                        },
                        {
                          "target": "33S",
                          "secondsToArrival": "451",
                          "arrivalTimeMessage": "8 min",
                          "lineColor": "FF9900",
                          "headSign": "33rd Street",
                          "lastUpdated": "2024-01-22T09:05:14.631786-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "09S",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "HOB",
                          "secondsToArrival": "0",
                          "arrivalTimeMessage": "0 min",
                          "lineColor": "4D92FB",
                          "headSign": "Hoboken",
                          "lastUpdated": "2024-01-22T09:05:04.593336-05:00"
                        },
                        {
                          "target": "JSQ",
                          "secondsToArrival": "154",
                          "arrivalTimeMessage": "3 min",
                          "lineColor": "FF9900",
                          "headSign": "Journal Square",
                          "lastUpdated": "2024-01-22T09:05:04.593336-05:00"
                        }
                      ]
                    },
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "33S",
                          "secondsToArrival": "43",
                          "arrivalTimeMessage": "1 min",
                          "lineColor": "FF9900",
                          "headSign": "33rd Street",
                          "lastUpdated": "2024-01-22T09:05:14.631786-05:00"
                        },
                        {
                          "target": "33S",
                          "secondsToArrival": "170",
                          "arrivalTimeMessage": "3 min",
                          "lineColor": "4D92FB",
                          "headSign": "33rd Street",
                          "lastUpdated": "2024-01-22T09:05:14.631786-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "14S",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "JSQ",
                          "secondsToArrival": "64",
                          "arrivalTimeMessage": "1 min",
                          "lineColor": "FF9900",
                          "headSign": "Journal Square",
                          "lastUpdated": "2024-01-22T09:05:34.604757-05:00"
                        },
                        {
                          "target": "JSQ",
                          "secondsToArrival": "367",
                          "arrivalTimeMessage": "6 min",
                          "lineColor": "FF9900",
                          "headSign": "Journal Square",
                          "lastUpdated": "2024-01-22T09:05:34.604757-05:00"
                        }
                      ]
                    },
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "33S",
                          "secondsToArrival": "113",
                          "arrivalTimeMessage": "2 min",
                          "lineColor": "FF9900",
                          "headSign": "33rd Street",
                          "lastUpdated": "2024-01-22T09:05:04.593336-05:00"
                        },
                        {
                          "target": "33S",
                          "secondsToArrival": "240",
                          "arrivalTimeMessage": "4 min",
                          "lineColor": "4D92FB",
                          "headSign": "33rd Street",
                          "lastUpdated": "2024-01-22T09:05:04.593336-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "23S",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "JSQ",
                          "secondsToArrival": "267",
                          "arrivalTimeMessage": "5 min",
                          "lineColor": "FF9900",
                          "headSign": "Journal Square",
                          "lastUpdated": "2024-01-22T09:05:14.631786-05:00"
                        },
                        {
                          "target": "HOB",
                          "secondsToArrival": "352",
                          "arrivalTimeMessage": "6 min",
                          "lineColor": "4D92FB",
                          "headSign": "Hoboken",
                          "lastUpdated": "2024-01-22T09:05:14.631786-05:00"
                        }
                      ]
                    },
                    {
                      "label": "ToNY",
                      "messages": [
                        {
                          "target": "33S",
                          "secondsToArrival": "15",
                          "arrivalTimeMessage": "0 min",
                          "lineColor": "FF9900",
                          "headSign": "33rd Street",
                          "lastUpdated": "2024-01-22T09:05:19.857843-05:00"
                        },
                        {
                          "target": "33S",
                          "secondsToArrival": "218",
                          "arrivalTimeMessage": "4 min",
                          "lineColor": "FF9900",
                          "headSign": "33rd Street",
                          "lastUpdated": "2024-01-22T09:05:19.857843-05:00"
                        }
                      ]
                    }
                  ]
                },
                {
                  "consideredStation": "33S",
                  "destinations": [
                    {
                      "label": "ToNJ",
                      "messages": [
                        {
                          "target": "JSQ",
                          "secondsToArrival": "175",
                          "arrivalTimeMessage": "3 min",
                          "lineColor": "FF9900",
                          "headSign": "Journal Square",
                          "lastUpdated": "2024-01-22T09:05:04.593336-05:00"
                        },
                        {
                          "target": "HOB",
                          "secondsToArrival": "235",
                          "arrivalTimeMessage": "4 min",
                          "lineColor": "4D92FB",
                          "headSign": "Hoboken",
                          "lastUpdated": "2024-01-22T09:05:04.593336-05:00"
                        },
                        {
                          "target": "JSQ",
                          "secondsToArrival": "475",
                          "arrivalTimeMessage": "8 min",
                          "lineColor": "FF9900",
                          "headSign": "Journal Square",
                          "lastUpdated": "2024-01-22T09:05:04.593336-05:00"
                        },
                        {
                          "target": "HOB",
                          "secondsToArrival": "715",
                          "arrivalTimeMessage": "12 min",
                          "lineColor": "4D92FB",
                          "headSign": "Hoboken",
                          "lastUpdated": "2024-01-22T09:05:04.593336-05:00"
                        },
                        {
                          "target": "JSQ",
                          "secondsToArrival": "775",
                          "arrivalTimeMessage": "13 min",
                          "lineColor": "FF9900",
                          "headSign": "Journal Square",
                          "lastUpdated": "2024-01-22T09:05:04.593336-05:00"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

    val response4 = """
        {
          "results": [
            {
              "consideredStation": "NWK",
              "destinations": [
                {
                  "label": "ToNY",
                  "messages": [
                    {
                      "target": "WTC",
                      "secondsToArrival": "190",
                      "arrivalTimeMessage": "3 min",
                      "lineColor": "D93A30",
                      "headSign": "World Trade Center",
                      "lastUpdated": "2024-01-22T09:17:49.984832-05:00"
                    },
                    {
                      "target": "WTC",
                      "secondsToArrival": "490",
                      "arrivalTimeMessage": "8 min",
                      "lineColor": "D93A30",
                      "headSign": "World Trade Center",
                      "lastUpdated": "2024-01-22T09:17:49.984832-05:00"
                    }
                  ]
                }
              ]
            },
            {
              "consideredStation": "HAR",
              "destinations": [
                {
                  "label": "ToNJ",
                  "messages": [
                    {
                      "target": "NWK",
                      "secondsToArrival": "225",
                      "arrivalTimeMessage": "4 min",
                      "lineColor": "D93A30",
                      "headSign": "Newark",
                      "lastUpdated": "2024-01-22T09:17:30.025803-05:00"
                    },
                    {
                      "target": "NWK",
                      "secondsToArrival": "498",
                      "arrivalTimeMessage": "8 min",
                      "lineColor": "D93A30",
                      "headSign": "Newark",
                      "lastUpdated": "2024-01-22T09:17:30.025803-05:00"
                    }
                  ]
                },
                {
                  "label": "ToNY",
                  "messages": [
                    {
                      "target": "WTC",
                      "secondsToArrival": "4",
                      "arrivalTimeMessage": "0 min",
                      "lineColor": "D93A30",
                      "headSign": "World Trade Center",
                      "lastUpdated": "2024-01-22T09:17:45.035094-05:00"
                    },
                    {
                      "target": "WTC",
                      "secondsToArrival": "297",
                      "arrivalTimeMessage": "5 min",
                      "lineColor": "D93A30",
                      "headSign": "World Trade Center",
                      "lastUpdated": "2024-01-22T09:17:45.035094-05:00"
                    }
                  ]
                }
              ]
            },
            {
              "consideredStation": "JSQ",
              "destinations": [
                {
                  "label": "ToNJ",
                  "messages": [
                    {
                      "target": "NWK",
                      "secondsToArrival": "157",
                      "arrivalTimeMessage": "3 min",
                      "lineColor": "D93A30",
                      "headSign": "Newark",
                      "lastUpdated": "2024-01-22T09:18:05.015333-05:00"
                    },
                    {
                      "target": "NWK",
                      "secondsToArrival": "463",
                      "arrivalTimeMessage": "8 min",
                      "lineColor": "D93A30",
                      "headSign": "Newark",
                      "lastUpdated": "2024-01-22T09:18:05.015333-05:00"
                    }
                  ]
                },
                {
                  "label": "ToNY",
                  "messages": [
                    {
                      "target": "33S",
                      "secondsToArrival": "65",
                      "arrivalTimeMessage": "1 min",
                      "lineColor": "FF9900",
                      "headSign": "33rd Street",
                      "lastUpdated": "2024-01-22T09:17:55.295568-05:00"
                    },
                    {
                      "target": "WTC",
                      "secondsToArrival": "76",
                      "arrivalTimeMessage": "1 min",
                      "lineColor": "D93A30",
                      "headSign": "World Trade Center",
                      "lastUpdated": "2024-01-22T09:17:55.295568-05:00"
                    },
                    {
                      "target": "33S",
                      "secondsToArrival": "365",
                      "arrivalTimeMessage": "6 min",
                      "lineColor": "FF9900",
                      "headSign": "33rd Street",
                      "lastUpdated": "2024-01-22T09:17:55.295568-05:00"
                    },
                    {
                      "target": "WTC",
                      "secondsToArrival": "399",
                      "arrivalTimeMessage": "7 min",
                      "lineColor": "D93A30",
                      "headSign": "World Trade Center",
                      "lastUpdated": "2024-01-22T09:17:55.295568-05:00"
                    }
                  ]
                }
              ]
            },
            {
              "consideredStation": "GRV",
              "destinations": [
                {
                  "label": "ToNJ",
                  "messages": [
                    {
                      "target": "NWK",
                      "secondsToArrival": "163",
                      "arrivalTimeMessage": "3 min",
                      "lineColor": "D93A30",
                      "headSign": "Newark",
                      "lastUpdated": "2024-01-22T09:18:05.015333-05:00"
                    },
                    {
                      "target": "JSQ",
                      "secondsToArrival": "248",
                      "arrivalTimeMessage": "4 min",
                      "lineColor": "FF9900",
                      "headSign": "Journal Square",
                      "lastUpdated": "2024-01-22T09:18:05.015333-05:00"
                    }
                  ]
                },
                {
                  "label": "ToNY",
                  "messages": [
                    {
                      "target": "WTC",
                      "secondsToArrival": "106",
                      "arrivalTimeMessage": "2 min",
                      "lineColor": "D93A30",
                      "headSign": "World Trade Center",
                      "lastUpdated": "2024-01-22T09:17:34.999003-05:00"
                    },
                    {
                      "target": "33S",
                      "secondsToArrival": "191",
                      "arrivalTimeMessage": "3 min",
                      "lineColor": "FF9900",
                      "headSign": "33rd Street",
                      "lastUpdated": "2024-01-22T09:17:34.999003-05:00"
                    }
                  ]
                }
              ]
            },
            {
              "consideredStation": "NEW",
              "destinations": [
                {
                  "label": "ToNJ",
                  "messages": [
                    {
                      "target": "JSQ",
                      "secondsToArrival": "38",
                      "arrivalTimeMessage": "1 min",
                      "lineColor": "FF9900",
                      "headSign": "Journal Square",
                      "lastUpdated": "2024-01-22T09:17:45.035094-05:00"
                    },
                    {
                      "target": "HOB",
                      "secondsToArrival": "381",
                      "arrivalTimeMessage": "7 min",
                      "lineColor": "65C100",
                      "headSign": "Hoboken",
                      "lastUpdated": "2024-01-22T09:17:45.035094-05:00"
                    }
                  ]
                },
                {
                  "label": "ToNY",
                  "messages": [
                    {
                      "target": "33S",
                      "secondsToArrival": "44",
                      "arrivalTimeMessage": "1 min",
                      "lineColor": "FF9900",
                      "headSign": "33rd Street",
                      "lastUpdated": "2024-01-22T09:17:25.270874-05:00"
                    },
                    {
                      "target": "WTC",
                      "secondsToArrival": "202",
                      "arrivalTimeMessage": "4 min",
                      "lineColor": "65C100",
                      "headSign": "World Trade Center",
                      "lastUpdated": "2024-01-22T09:17:25.270874-05:00"
                    }
                  ]
                }
              ]
            },
            {
              "consideredStation": "EXP",
              "destinations": [
                {
                  "label": "ToNJ",
                  "messages": [
                    {
                      "target": "NWK",
                      "secondsToArrival": "5",
                      "arrivalTimeMessage": "0 min",
                      "lineColor": "D93A30",
                      "headSign": "Newark",
                      "lastUpdated": "2024-01-22T09:17:40.310208-05:00"
                    },
                    {
                      "target": "HOB",
                      "secondsToArrival": "90",
                      "arrivalTimeMessage": "2 min",
                      "lineColor": "65C100",
                      "headSign": "Hoboken",
                      "lastUpdated": "2024-01-22T09:17:40.310208-05:00"
                    }
                  ]
                },
                {
                  "label": "ToNY",
                  "messages": [
                    {
                      "target": "WTC",
                      "secondsToArrival": "73",
                      "arrivalTimeMessage": "1 min",
                      "lineColor": "65C100",
                      "headSign": "World Trade Center",
                      "lastUpdated": "2024-01-22T09:18:05.015333-05:00"
                    },
                    {
                      "target": "WTC",
                      "secondsToArrival": "256",
                      "arrivalTimeMessage": "4 min",
                      "lineColor": "D93A30",
                      "headSign": "World Trade Center",
                      "lastUpdated": "2024-01-22T09:18:05.015333-05:00"
                    }
                  ]
                }
              ]
            },
            {
              "consideredStation": "HOB",
              "destinations": [
                {
                  "label": "ToNY",
                  "messages": [
                    {
                      "target": "33S",
                      "secondsToArrival": "265",
                      "arrivalTimeMessage": "5 min",
                      "lineColor": "4D92FB",
                      "headSign": "33rd Street",
                      "lastUpdated": "2024-01-22T09:17:34.999003-05:00"
                    },
                    {
                      "target": "WTC",
                      "secondsToArrival": "505",
                      "arrivalTimeMessage": "9 min",
                      "lineColor": "65C100",
                      "headSign": "World Trade Center",
                      "lastUpdated": "2024-01-22T09:17:34.999003-05:00"
                    },
                    {
                      "target": "33S",
                      "secondsToArrival": "865",
                      "arrivalTimeMessage": "15 min",
                      "lineColor": "4D92FB",
                      "headSign": "33rd Street",
                      "lastUpdated": "2024-01-22T09:17:34.999003-05:00"
                    },
                    {
                      "target": "WTC",
                      "secondsToArrival": "985",
                      "arrivalTimeMessage": "17 min",
                      "lineColor": "65C100",
                      "headSign": "World Trade Center",
                      "lastUpdated": "2024-01-22T09:17:34.999003-05:00"
                    }
                  ]
                }
              ]
            },
            {
              "consideredStation": "WTC",
              "destinations": [
                {
                  "label": "ToNJ",
                  "messages": [
                    {
                      "target": "NWK",
                      "secondsToArrival": "65",
                      "arrivalTimeMessage": "1 min",
                      "lineColor": "D93A30",
                      "headSign": "Newark",
                      "lastUpdated": "2024-01-22T09:17:55.295568-05:00"
                    },
                    {
                      "target": "HOB",
                      "secondsToArrival": "305",
                      "arrivalTimeMessage": "5 min",
                      "lineColor": "65C100",
                      "headSign": "Hoboken",
                      "lastUpdated": "2024-01-22T09:17:55.295568-05:00"
                    },
                    {
                      "target": "NWK",
                      "secondsToArrival": "365",
                      "arrivalTimeMessage": "6 min",
                      "lineColor": "D93A30",
                      "headSign": "Newark",
                      "lastUpdated": "2024-01-22T09:17:55.295568-05:00"
                    },
                    {
                      "target": "HOB",
                      "secondsToArrival": "845",
                      "arrivalTimeMessage": "14 min",
                      "lineColor": "65C100",
                      "headSign": "Hoboken",
                      "lastUpdated": "2024-01-22T09:17:55.295568-05:00"
                    },
                    {
                      "target": "HOB",
                      "secondsToArrival": "1445",
                      "arrivalTimeMessage": "24 min",
                      "lineColor": "65C100",
                      "headSign": "Hoboken",
                      "lastUpdated": "2024-01-22T09:17:55.295568-05:00"
                    }
                  ]
                }
              ]
            },
            {
              "consideredStation": "CHR",
              "destinations": [
                {
                  "label": "ToNJ",
                  "messages": [
                    {
                      "target": "JSQ",
                      "secondsToArrival": "106",
                      "arrivalTimeMessage": "2 min",
                      "lineColor": "FF9900",
                      "headSign": "Journal Square",
                      "lastUpdated": "2024-01-22T09:17:45.035094-05:00"
                    },
                    {
                      "target": "HOB",
                      "secondsToArrival": "364",
                      "arrivalTimeMessage": "6 min",
                      "lineColor": "4D92FB",
                      "headSign": "Hoboken",
                      "lastUpdated": "2024-01-22T09:17:45.035094-05:00"
                    }
                  ]
                },
                {
                  "label": "ToNY",
                  "messages": [
                    {
                      "target": "33S",
                      "secondsToArrival": "254",
                      "arrivalTimeMessage": "4 min",
                      "lineColor": "4D92FB",
                      "headSign": "33rd Street",
                      "lastUpdated": "2024-01-22T09:17:34.999003-05:00"
                    },
                    {
                      "target": "33S",
                      "secondsToArrival": "431",
                      "arrivalTimeMessage": "7 min",
                      "lineColor": "FF9900",
                      "headSign": "33rd Street",
                      "lastUpdated": "2024-01-22T09:17:34.999003-05:00"
                    }
                  ]
                }
              ]
            },
            {
              "consideredStation": "09S",
              "destinations": [
                {
                  "label": "ToNJ",
                  "messages": [
                    {
                      "target": "JSQ",
                      "secondsToArrival": "0",
                      "arrivalTimeMessage": "0 min",
                      "lineColor": "FF9900",
                      "headSign": "Journal Square",
                      "lastUpdated": "2024-01-22T09:17:25.270874-05:00"
                    },
                    {
                      "target": "HOB",
                      "secondsToArrival": "264",
                      "arrivalTimeMessage": "5 min",
                      "lineColor": "4D92FB",
                      "headSign": "Hoboken",
                      "lastUpdated": "2024-01-22T09:17:25.270874-05:00"
                    }
                  ]
                },
                {
                  "label": "ToNY",
                  "messages": [
                    {
                      "target": "33S",
                      "secondsToArrival": "47",
                      "arrivalTimeMessage": "1 min",
                      "lineColor": "FF9900",
                      "headSign": "33rd Street",
                      "lastUpdated": "2024-01-22T09:17:34.999003-05:00"
                    },
                    {
                      "target": "33S",
                      "secondsToArrival": "374",
                      "arrivalTimeMessage": "6 min",
                      "lineColor": "4D92FB",
                      "headSign": "33rd Street",
                      "lastUpdated": "2024-01-22T09:17:34.999003-05:00"
                    }
                  ]
                }
              ]
            },
            {
              "consideredStation": "14S",
              "destinations": [
                {
                  "label": "ToNJ",
                  "messages": [
                    {
                      "target": "HOB",
                      "secondsToArrival": "174",
                      "arrivalTimeMessage": "3 min",
                      "lineColor": "4D92FB",
                      "headSign": "Hoboken",
                      "lastUpdated": "2024-01-22T09:17:55.295568-05:00"
                    },
                    {
                      "target": "JSQ",
                      "secondsToArrival": "259",
                      "arrivalTimeMessage": "4 min",
                      "lineColor": "FF9900",
                      "headSign": "Journal Square",
                      "lastUpdated": "2024-01-22T09:17:55.295568-05:00"
                    }
                  ]
                },
                {
                  "label": "ToNY",
                  "messages": [
                    {
                      "target": "33S",
                      "secondsToArrival": "0",
                      "arrivalTimeMessage": "0 min",
                      "lineColor": "4D92FB",
                      "headSign": "33rd Street",
                      "lastUpdated": "2024-01-22T09:17:25.270874-05:00"
                    },
                    {
                      "target": "33S",
                      "secondsToArrival": "117",
                      "arrivalTimeMessage": "2 min",
                      "lineColor": "FF9900",
                      "headSign": "33rd Street",
                      "lastUpdated": "2024-01-22T09:17:25.270874-05:00"
                    }
                  ]
                }
              ]
            },
            {
              "consideredStation": "23S",
              "destinations": [
                {
                  "label": "ToNJ",
                  "messages": [
                    {
                      "target": "HOB",
                      "secondsToArrival": "39",
                      "arrivalTimeMessage": "1 min",
                      "lineColor": "4D92FB",
                      "headSign": "Hoboken",
                      "lastUpdated": "2024-01-22T09:18:10.303376-05:00"
                    },
                    {
                      "target": "JSQ",
                      "secondsToArrival": "124",
                      "arrivalTimeMessage": "2 min",
                      "lineColor": "FF9900",
                      "headSign": "Journal Square",
                      "lastUpdated": "2024-01-22T09:18:10.303376-05:00"
                    }
                  ]
                },
                {
                  "label": "ToNY",
                  "messages": [
                    {
                      "target": "33S",
                      "secondsToArrival": "64",
                      "arrivalTimeMessage": "1 min",
                      "lineColor": "4D92FB",
                      "headSign": "33rd Street",
                      "lastUpdated": "2024-01-22T09:18:00.02415-05:00"
                    },
                    {
                      "target": "33S",
                      "secondsToArrival": "202",
                      "arrivalTimeMessage": "4 min",
                      "lineColor": "FF9900",
                      "headSign": "33rd Street",
                      "lastUpdated": "2024-01-22T09:18:00.02415-05:00"
                    }
                  ]
                }
              ]
            },
            {
              "consideredStation": "33S",
              "destinations": [
                {
                  "label": "ToNJ",
                  "messages": [
                    {
                      "target": "JSQ",
                      "secondsToArrival": "5",
                      "arrivalTimeMessage": "0 min",
                      "lineColor": "FF9900",
                      "headSign": "Journal Square",
                      "lastUpdated": "2024-01-22T09:17:55.295568-05:00"
                    },
                    {
                      "target": "JSQ",
                      "secondsToArrival": "305",
                      "arrivalTimeMessage": "5 min",
                      "lineColor": "FF9900",
                      "headSign": "Journal Square",
                      "lastUpdated": "2024-01-22T09:17:55.295568-05:00"
                    },
                    {
                      "target": "HOB",
                      "secondsToArrival": "425",
                      "arrivalTimeMessage": "7 min",
                      "lineColor": "4D92FB",
                      "headSign": "Hoboken",
                      "lastUpdated": "2024-01-22T09:17:55.295568-05:00"
                    },
                    {
                      "target": "JSQ",
                      "secondsToArrival": "605",
                      "arrivalTimeMessage": "10 min",
                      "lineColor": "FF9900",
                      "headSign": "Journal Square",
                      "lastUpdated": "2024-01-22T09:17:55.295568-05:00"
                    },
                    {
                      "target": "HOB",
                      "secondsToArrival": "905",
                      "arrivalTimeMessage": "15 min",
                      "lineColor": "4D92FB",
                      "headSign": "Hoboken",
                      "lastUpdated": "2024-01-22T09:17:55.295568-05:00"
                    }
                  ]
                }
              ]
            }
          ]
        }
    """.trimIndent()

    val response5 = "{\"results\":[{\"consideredStation\":\"NWK\",\"destinations\":[{\"label\":\"ToNY\",\"messages\":[{\"target\":\"WTC\",\"secondsToArrival\":\"979\",\"arrivalTimeMessage\":\"16 min\",\"lineColor\":\"D93A30\",\"headSign\":\"World Trade Center\",\"lastUpdated\":\"2024-02-03T18:43:40.983504-05:00\"},{\"target\":\"WTC\",\"secondsToArrival\":\"2179\",\"arrivalTimeMessage\":\"36 min\",\"lineColor\":\"D93A30\",\"headSign\":\"World Trade Center\",\"lastUpdated\":\"2024-02-03T18:43:40.983504-05:00\"}]}]},{\"consideredStation\":\"HAR\",\"destinations\":[{\"label\":\"ToNJ\",\"messages\":[{\"target\":\"NWK\",\"secondsToArrival\":\"994\",\"arrivalTimeMessage\":\"17 min\",\"lineColor\":\"D93A30\",\"headSign\":\"Newark\",\"lastUpdated\":\"2024-02-03T18:43:15.688801-05:00\"},{\"target\":\"NWK\",\"secondsToArrival\":\"2066\",\"arrivalTimeMessage\":\"35 min\",\"lineColor\":\"D93A30\",\"headSign\":\"Newark\",\"lastUpdated\":\"2024-02-03T18:43:15.688801-05:00\"}]},{\"label\":\"ToNY\",\"messages\":[{\"target\":\"WTC\",\"secondsToArrival\":\"1086\",\"arrivalTimeMessage\":\"18 min\",\"lineColor\":\"D93A30\",\"headSign\":\"World Trade Center\",\"lastUpdated\":\"2024-02-03T18:43:20.956625-05:00\"},{\"target\":\"WTC\",\"secondsToArrival\":\"2286\",\"arrivalTimeMessage\":\"38 min\",\"lineColor\":\"D93A30\",\"headSign\":\"World Trade Center\",\"lastUpdated\":\"2024-02-03T18:43:20.956625-05:00\"}]}]},{\"consideredStation\":\"JSQ\",\"destinations\":[{\"label\":\"ToNJ\",\"messages\":[{\"target\":\"NWK\",\"secondsToArrival\":\"329\",\"arrivalTimeMessage\":\"6 min\",\"lineColor\":\"D93A30\",\"headSign\":\"Newark\",\"lastUpdated\":\"2024-02-03T18:43:20.956625-05:00\"},{\"target\":\"NWK\",\"secondsToArrival\":\"1401\",\"arrivalTimeMessage\":\"24 min\",\"lineColor\":\"D93A30\",\"headSign\":\"Newark\",\"lastUpdated\":\"2024-02-03T18:43:20.956625-05:00\"}]},{\"label\":\"ToNY\",\"messages\":[{\"target\":\"33S\",\"secondsToArrival\":\"264\",\"arrivalTimeMessage\":\"5 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"33rd Street via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:35.981295-05:00\"},{\"target\":\"WTC\",\"secondsToArrival\":\"289\",\"arrivalTimeMessage\":\"5 min\",\"lineColor\":\"D93A30\",\"headSign\":\"World Trade Center\",\"lastUpdated\":\"2024-02-03T18:43:35.981295-05:00\"},{\"target\":\"33S\",\"secondsToArrival\":\"984\",\"arrivalTimeMessage\":\"17 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"33rd Street via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:35.981295-05:00\"},{\"target\":\"WTC\",\"secondsToArrival\":\"1535\",\"arrivalTimeMessage\":\"26 min\",\"lineColor\":\"D93A30\",\"headSign\":\"World Trade Center\",\"lastUpdated\":\"2024-02-03T18:43:35.981295-05:00\"}]}]},{\"consideredStation\":\"GRV\",\"destinations\":[{\"label\":\"ToNY\",\"messages\":[{\"target\":\"WTC\",\"secondsToArrival\":\"511\",\"arrivalTimeMessage\":\"9 min\",\"lineColor\":\"D93A30\",\"headSign\":\"World Trade Center\",\"lastUpdated\":\"2024-02-03T18:43:25.955906-05:00\"},{\"target\":\"33S\",\"secondsToArrival\":\"596\",\"arrivalTimeMessage\":\"10 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"33rd Street via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:25.955906-05:00\"}]}]},{\"consideredStation\":\"NEW\",\"destinations\":[{\"label\":\"ToNJ\",\"messages\":[{\"target\":\"JSQ\",\"secondsToArrival\":\"448\",\"arrivalTimeMessage\":\"8 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"Journal Square via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:40.983504-05:00\"},{\"target\":\"JSQ\",\"secondsToArrival\":\"1118\",\"arrivalTimeMessage\":\"19 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"Journal Square via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:40.983504-05:00\"}]},{\"label\":\"ToNY\",\"messages\":[{\"target\":\"33S\",\"secondsToArrival\":\"0\",\"arrivalTimeMessage\":\"0 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"33rd Street via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:15.688801-05:00\"},{\"target\":\"33S\",\"secondsToArrival\":\"806\",\"arrivalTimeMessage\":\"14 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"33rd Street via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:15.688801-05:00\"}]}]},{\"consideredStation\":\"EXP\",\"destinations\":[{\"label\":\"ToNJ\",\"messages\":[{\"target\":\"NWK\",\"secondsToArrival\":\"0\",\"arrivalTimeMessage\":\"0 min\",\"lineColor\":\"D93A30\",\"headSign\":\"Newark\",\"lastUpdated\":\"2024-02-03T18:40:45.669089-05:00\"}]},{\"label\":\"ToNY\",\"messages\":[{\"target\":\"WTC\",\"secondsToArrival\":\"40\",\"arrivalTimeMessage\":\"1 min\",\"lineColor\":\"D93A30\",\"headSign\":\"World Trade Center\",\"lastUpdated\":\"2024-02-03T18:43:15.688801-05:00\"},{\"target\":\"WTC\",\"secondsToArrival\":\"675\",\"arrivalTimeMessage\":\"11 min\",\"lineColor\":\"D93A30\",\"headSign\":\"World Trade Center\",\"lastUpdated\":\"2024-02-03T18:43:15.688801-05:00\"}]}]},{\"consideredStation\":\"HOB\",\"destinations\":[{\"label\":\"ToNJ\",\"messages\":[{\"target\":\"JSQ\",\"secondsToArrival\":\"220\",\"arrivalTimeMessage\":\"4 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"Journal Square via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:30.755819-05:00\"},{\"target\":\"JSQ\",\"secondsToArrival\":\"896\",\"arrivalTimeMessage\":\"15 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"Journal Square via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:30.755819-05:00\"}]},{\"label\":\"ToNY\",\"messages\":[{\"target\":\"33S\",\"secondsToArrival\":\"488\",\"arrivalTimeMessage\":\"8 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"33rd Street via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:15.688801-05:00\"},{\"target\":\"33S\",\"secondsToArrival\":\"1304\",\"arrivalTimeMessage\":\"22 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"33rd Street via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:15.688801-05:00\"}]}]},{\"consideredStation\":\"WTC\",\"destinations\":[{\"label\":\"ToNJ\",\"messages\":[{\"target\":\"NWK\",\"secondsToArrival\":\"699\",\"arrivalTimeMessage\":\"12 min\",\"lineColor\":\"D93A30\",\"headSign\":\"Newark\",\"lastUpdated\":\"2024-02-03T18:43:20.956625-05:00\"},{\"target\":\"NWK\",\"secondsToArrival\":\"1899\",\"arrivalTimeMessage\":\"32 min\",\"lineColor\":\"D93A30\",\"headSign\":\"Newark\",\"lastUpdated\":\"2024-02-03T18:43:20.956625-05:00\"}]}]},{\"consideredStation\":\"CHR\",\"destinations\":[{\"label\":\"ToNJ\",\"messages\":[{\"target\":\"JSQ\",\"secondsToArrival\":\"98\",\"arrivalTimeMessage\":\"Delayed\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"Journal Square via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:40.983504-05:00\"},{\"target\":\"JSQ\",\"secondsToArrival\":\"781\",\"arrivalTimeMessage\":\"13 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"Journal Square via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:40.983504-05:00\"}]},{\"label\":\"ToNY\",\"messages\":[{\"target\":\"33S\",\"secondsToArrival\":\"345\",\"arrivalTimeMessage\":\"6 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"33rd Street via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:25.955906-05:00\"},{\"target\":\"33S\",\"secondsToArrival\":\"1010\",\"arrivalTimeMessage\":\"17 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"33rd Street via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:25.955906-05:00\"}]}]},{\"consideredStation\":\"09S\",\"destinations\":[{\"label\":\"ToNJ\",\"messages\":[{\"target\":\"JSQ\",\"secondsToArrival\":\"0\",\"arrivalTimeMessage\":\"0 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"Journal Square via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:20.956625-05:00\"},{\"target\":\"JSQ\",\"secondsToArrival\":\"681\",\"arrivalTimeMessage\":\"12 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"Journal Square via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:20.956625-05:00\"}]},{\"label\":\"ToNY\",\"messages\":[{\"target\":\"33S\",\"secondsToArrival\":\"455\",\"arrivalTimeMessage\":\"8 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"33rd Street via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:35.981295-05:00\"},{\"target\":\"33S\",\"secondsToArrival\":\"1130\",\"arrivalTimeMessage\":\"19 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"33rd Street via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:35.981295-05:00\"}]}]},{\"consideredStation\":\"14S\",\"destinations\":[{\"label\":\"ToNJ\",\"messages\":[{\"target\":\"JSQ\",\"secondsToArrival\":\"586\",\"arrivalTimeMessage\":\"10 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"Journal Square via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:55.713501-05:00\"},{\"target\":\"JSQ\",\"secondsToArrival\":\"1306\",\"arrivalTimeMessage\":\"22 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"Journal Square via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:55.713501-05:00\"}]},{\"label\":\"ToNY\",\"messages\":[{\"target\":\"33S\",\"secondsToArrival\":\"510\",\"arrivalTimeMessage\":\"9 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"33rd Street via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:40.983504-05:00\"},{\"target\":\"33S\",\"secondsToArrival\":\"1198\",\"arrivalTimeMessage\":\"20 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"33rd Street via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:40.983504-05:00\"}]}]},{\"consideredStation\":\"23S\",\"destinations\":[{\"label\":\"ToNJ\",\"messages\":[{\"target\":\"JSQ\",\"secondsToArrival\":\"511\",\"arrivalTimeMessage\":\"9 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"Journal Square via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:10.75075-05:00\"},{\"target\":\"JSQ\",\"secondsToArrival\":\"1231\",\"arrivalTimeMessage\":\"21 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"Journal Square via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:10.75075-05:00\"}]},{\"label\":\"ToNY\",\"messages\":[{\"target\":\"33S\",\"secondsToArrival\":\"32\",\"arrivalTimeMessage\":\"1 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"33rd Street via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:10.75075-05:00\"},{\"target\":\"33S\",\"secondsToArrival\":\"660\",\"arrivalTimeMessage\":\"11 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"33rd Street via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:10.75075-05:00\"}]}]},{\"consideredStation\":\"33S\",\"destinations\":[{\"label\":\"ToNJ\",\"messages\":[{\"target\":\"JSQ\",\"secondsToArrival\":\"389\",\"arrivalTimeMessage\":\"7 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"Journal Square via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:30.755819-05:00\"},{\"target\":\"JSQ\",\"secondsToArrival\":\"1109\",\"arrivalTimeMessage\":\"19 min\",\"lineColor\":\"4D92FB,FF9900\",\"headSign\":\"Journal Square via Hoboken\",\"lastUpdated\":\"2024-02-03T18:43:30.755819-05:00\"}]}]}]}"
}