package com.sixbynine.transit.path.api.impl

import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.api.DepartureBoardTrain
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.app.ui.Colors
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TrainBackfillHelperTest {
    @BeforeTest
    fun setUp() {
        Logging.setTest()
    }

    @Test
    fun `easy backfill`() {
        val trains = departuresMap {
            station(Stations.WorldTradeCenter) {
                newarkTrainAt(10, 0)
                newarkTrainAt(10, 20)
            }

            station(Stations.ExchangePlace) {
                newarkTrainAt(10, 3)
            }
        }

        val backfilled = TrainBackfillHelper.withBackfill(trains)

        val expTrains = backfilled[Stations.ExchangePlace]
        assertNotNull(expTrains)
        assertContains(expTrains.map { it.projectedArrival.toUtcLocalTime() }, LocalTime(10, 3))
        assertContains(
            expTrains.map { it.projectedArrival.toUtcLocalTime() },
            LocalTime(10, 23, 49)
        )
    }

    @Test
    fun `do not replace close existing trains`() {
        val trains = departuresMap {
            station(Stations.WorldTradeCenter) {
                newarkTrainAt(10, 0)
            }

            station(Stations.ExchangePlace) {
                newarkTrainAt(10, 6)
            }
        }

        val backfilled = TrainBackfillHelper.withBackfill(trains)
        val expTrains = backfilled[Stations.ExchangePlace]
        assertTrue(expTrains?.size == 1)
    }

    @Test
    fun `backfill from multiple stations back`() {
        val trains = departuresMap {
            station(Stations.WorldTradeCenter) {
                newarkTrainAt(10, 0)
                newarkTrainAt(10, 20)
            }

            station(Stations.ExchangePlace) {
                newarkTrainAt(10, 6)
                newarkTrainAt(10, 43)
            }

            station(Stations.GroveStreet) {
                newarkTrainAt(10, 10)
            }
        }

        val backfilled = TrainBackfillHelper.withBackfill(trains)
        val expTrains = backfilled[Stations.ExchangePlace]
        val expTrainsTimes = expTrains?.map { it.projectedArrival.printForAssertions() }
        assertNotNull(expTrainsTimes)
        assertContains(expTrainsTimes, "10:06")
        assertContains(expTrainsTimes, "10:23")
        assertContains(expTrainsTimes, "10:43")

        val grvTrains = backfilled[Stations.GroveStreet]
        val grvTrainsTimes = grvTrains?.map { it.projectedArrival.printForAssertions() }
        assertNotNull(grvTrainsTimes)
        assertContains(grvTrainsTimes, "10:10")
        assertContains(grvTrainsTimes, "10:26")
        assertContains(grvTrainsTimes, "10:46")
    }

    @Test
    fun `multiple backfills`() {
        val trains = departuresMap {
            station(Stations.WorldTradeCenter) {
                newarkTrainAt(10, 0)
                newarkTrainAt(10, 20)
            }

            station(Stations.ExchangePlace) {
                newarkTrainAt(10, 3)
                wtcTrainAt(10, 8)
            }

            station(Stations.GroveStreet) {
                wtcTrainAt(10, 5)
                wtcTrainAt(10, 25)
            }
        }

        val backfilled = TrainBackfillHelper.withBackfill(trains)

        val expWtcTrains =
            backfilled[Stations.ExchangePlace]!!.filter { it.headsign == "World Trade Center" }
        val expWtcTrainsTimes = expWtcTrains.map { it.projectedArrival.printForAssertions() }
        assertEquals(expWtcTrainsTimes, listOf("10:08", "10:28"))

        val expNwkTrains =
            backfilled[Stations.ExchangePlace]!!.filter { it.headsign == "Newark" }
        val expNwkTrainsTimes = expNwkTrains.map { it.projectedArrival.printForAssertions() }
        assertEquals(expNwkTrainsTimes, listOf("10:03", "10:23"))
    }

    @Test
    fun `do not backfill when no trains matching head sign`() {
        val trains = departuresMap {
            station(Stations.WorldTradeCenter) {
                newarkTrainAt(10, 0)
            }

            station(Stations.ExchangePlace) {
                newarkTrainAt(10, 3)
                wtcTrainAt(10, 8)
            }

            station(Stations.GroveStreet) {
                wtcTrainAt(10, 5)
                // No Newark train stopping here :'(
            }
        }

        val backfilled = TrainBackfillHelper.withBackfill(trains)

        val grvTrains = backfilled[Stations.GroveStreet]
        val grvTrainsTimes = grvTrains?.map { it.projectedArrival.printForAssertions() }
        assertEquals(grvTrainsTimes, listOf("10:05"))
    }

    private fun departuresMap(
        builder: DeparturesMapBuilder.() -> Unit
    ): Map<Station, List<DepartureBoardTrain>> {
        return DeparturesMapBuilder().apply(builder).build()
    }

    private class DeparturesMapBuilder(private val date: LocalDate = LocalDate(2021, JANUARY, 21)) {
        private val map = mutableMapOf<Station, MutableList<DepartureBoardTrain>>()

        fun station(station: Station, block: StationScope.() -> Unit) {
            val scope = object : StationScope {
                override fun newarkTrainAt(hour: Int, minute: Int) {
                    val time = date.atTime(hour, minute)
                    map.getOrPut(station) { mutableListOf() }
                        .add(newarkTrain(time.toUtcInstant()))
                }

                override fun wtcTrainAt(hour: Int, minute: Int) {
                    val time = date.atTime(hour, minute)
                    map.getOrPut(station) { mutableListOf() }
                        .add(wtcTrain(time.toUtcInstant()))
                }
            }
            block(scope)
        }

        interface StationScope {
            fun newarkTrainAt(hour: Int, minute: Int)
            fun wtcTrainAt(hour: Int, minute: Int)
        }

        fun build(): Map<Station, List<DepartureBoardTrain>> {
            return map
        }
    }

    private companion object {
        fun LocalDateTime.toUtcInstant(): Instant {
            return toInstant(TimeZone.UTC)
        }

        fun Instant.toUtcLocalTime(): LocalTime {
            return toLocalDateTime(TimeZone.UTC).time
        }

        fun Instant.printForAssertions(): String {
            val time = toUtcLocalTime()
            return "${time.hour.toString().padStart(2, '0')}:" +
                    time.minute.toString().padStart(2, '0')
        }

        fun newarkTrain(projectedArrival: Instant): DepartureBoardTrain {
            return DepartureBoardTrain(
                headsign = "Newark",
                projectedArrival = projectedArrival,
                lineColors = Colors.NwkWtc.map { it.color }
            )
        }

        fun wtcTrain(projectedArrival: Instant): DepartureBoardTrain {
            return DepartureBoardTrain(
                headsign = "World Trade Center",
                projectedArrival = projectedArrival,
                lineColors = Colors.NwkWtc.map { it.color }
            )
        }
    }
}
