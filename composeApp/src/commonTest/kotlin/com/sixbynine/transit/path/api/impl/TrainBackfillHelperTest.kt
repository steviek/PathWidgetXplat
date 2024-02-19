package com.sixbynine.transit.path.api.impl

import androidx.compose.ui.graphics.Color
import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.api.DepartureBoardTrain
import com.sixbynine.transit.path.api.State.NewJersey
import com.sixbynine.transit.path.api.State.NewYork
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.Stations.ExchangePlace
import com.sixbynine.transit.path.api.Stations.GroveStreet
import com.sixbynine.transit.path.api.Stations.Newport
import com.sixbynine.transit.path.api.Stations.WorldTradeCenter
import com.sixbynine.transit.path.app.ui.ColorWrapper
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
            station(WorldTradeCenter) {
                newarkTrainAt(10, 0)
                newarkTrainAt(10, 20)
            }

            station(ExchangePlace) {
                newarkTrainAt(10, 3)
            }
        }

        val backfilled = TrainBackfillHelper.withBackfill(trains)

        val expTrains = backfilled[ExchangePlace]
        val expTrainsTimes = expTrains?.map { it.projectedArrival.printForAssertions() }
        assertNotNull(expTrainsTimes)
        assertContains(expTrainsTimes, "10:03")
        assertContains(expTrainsTimes, "10:23")
    }

    @Test
    fun `backfill distinguishing by line color`() {
        val trains = departuresMap {
            station(GroveStreet) {
                nwkWtcTrainAt(10, 0)
                nwkWtcTrainAt(10, 20)
            }

            station(Newport) {
                hobWtcTrainAt(10, 2)
                hobWtcTrainAt(10, 22)
            }

            station(ExchangePlace) {
                nwkWtcTrainAt(10, 4)
                hobWtcTrainAt(10, 27)
            }
        }

        val backfilled = TrainBackfillHelper.withBackfill(trains)
        val expTrains = backfilled[ExchangePlace]
        val expTrainsTimes =
            expTrains?.map { it.lineColors to it.projectedArrival.printForAssertions() }
        assertNotNull(expTrainsTimes)
        assertContains(expTrainsTimes, Colors.NwkWtc to "10:04")
        assertContains(expTrainsTimes, Colors.HobWtc to "10:07")
        assertContains(expTrainsTimes, Colors.NwkWtc to "10:23")
        assertContains(expTrainsTimes, Colors.HobWtc to "10:27")
    }

    @Test
    fun `do not replace close existing trains`() {
        val trains = departuresMap {
            station(WorldTradeCenter) {
                newarkTrainAt(10, 0)
            }

            station(ExchangePlace) {
                newarkTrainAt(10, 6)
            }
        }

        val backfilled = TrainBackfillHelper.withBackfill(trains)
        val expTrains = backfilled[ExchangePlace]
        assertTrue(expTrains?.size == 1)
    }

    @Test
    fun `backfill from multiple stations back`() {
        val trains = departuresMap {
            station(WorldTradeCenter) {
                newarkTrainAt(10, 0)
                newarkTrainAt(10, 20)
            }

            station(ExchangePlace) {
                newarkTrainAt(10, 6)
                newarkTrainAt(10, 43)
            }

            station(GroveStreet) {
                newarkTrainAt(10, 10)
            }
        }

        val backfilled = TrainBackfillHelper.withBackfill(trains)
        val expTrains = backfilled[ExchangePlace]
        val expTrainsTimes = expTrains?.map { it.projectedArrival.printForAssertions() }
        assertNotNull(expTrainsTimes)
        assertContains(expTrainsTimes, "10:06")
        assertContains(expTrainsTimes, "10:23")
        assertContains(expTrainsTimes, "10:43")

        val grvTrains = backfilled[GroveStreet]
        val grvTrainsTimes = grvTrains?.map { it.projectedArrival.printForAssertions() }
        assertNotNull(grvTrainsTimes)
        assertContains(grvTrainsTimes, "10:10")
        assertContains(grvTrainsTimes, "10:26")
        assertContains(grvTrainsTimes, "10:46")
    }

    @Test
    fun `multiple backfills`() {
        val trains = departuresMap {
            station(WorldTradeCenter) {
                newarkTrainAt(10, 0)
                newarkTrainAt(10, 20)
            }

            station(ExchangePlace) {
                newarkTrainAt(10, 3)
                nwkWtcTrainAt(10, 8)
            }

            station(GroveStreet) {
                nwkWtcTrainAt(10, 5)
                nwkWtcTrainAt(10, 25)
            }
        }

        val backfilled = TrainBackfillHelper.withBackfill(trains)

        val expWtcTrains =
            backfilled[ExchangePlace]!!.filter { it.headsign == "World Trade Center" }
        val expWtcTrainsTimes = expWtcTrains.map { it.projectedArrival.printForAssertions() }
        assertEquals(expWtcTrainsTimes, listOf("10:08", "10:28"))

        val expNwkTrains =
            backfilled[ExchangePlace]!!.filter { it.headsign == "Newark" }
        val expNwkTrainsTimes = expNwkTrains.map { it.projectedArrival.printForAssertions() }
        assertEquals(listOf("10:03", "10:23"), expNwkTrainsTimes)
    }

    @Test
    fun `do not backfill when no trains matching head sign`() {
        val trains = departuresMap {
            station(WorldTradeCenter) {
                newarkTrainAt(10, 0)
            }

            station(ExchangePlace) {
                newarkTrainAt(10, 3)
                nwkWtcTrainAt(10, 8)
            }

            station(GroveStreet) {
                nwkWtcTrainAt(10, 5)
                // No Newark train stopping here :'(
            }
        }

        val backfilled = TrainBackfillHelper.withBackfill(trains)

        val grvTrains = backfilled[GroveStreet]
        val grvTrainsTimes = grvTrains?.map { it.projectedArrival.printForAssertions() }
        assertEquals(listOf("10:05"), grvTrainsTimes)
    }

    @Test
    fun `another case`() {
        val trains = departuresMap {
            station(WorldTradeCenter) {
                newarkTrainAt(21, 4)
                wtcHobTrainAt(21, 7)
                newarkTrainAt(21, 14)
                wtcHobTrainAt(21, 19)
            }

            station(ExchangePlace) {
                newarkTrainAt(21, 7)
                wtcHobTrainAt(21, 10)
            }
        }

        val backfilled = TrainBackfillHelper.withBackfill(trains)
        val expHobTrains = backfilled[ExchangePlace]!!.filter { it.headsign == "Hoboken" }
        val expTrainsTimes = expHobTrains.map { it.projectedArrival.printForAssertions() }
        assertContains(expTrainsTimes, "21:10")
        assertContains(expTrainsTimes, "21:22")
    }

    @Test
    fun `match for shortened routes`() {
        val trains = departuresMap {
            station(WorldTradeCenter) {
                wtcJsqTrainAt(22, 45)
                wtcJsqTrainAt(23, 20)
            }

            station(ExchangePlace) {
                wtcJsqTrainAt(22, 48)
            }
        }

        val backfilled = TrainBackfillHelper.withBackfill(trains)
        val expTrains = backfilled[ExchangePlace]
        val expTrainTimes = expTrains?.map { it.projectedArrival.printForAssertions() }
        assertEquals(listOf("22:48", "23:23"), expTrainTimes)
    }

    @Test
    fun `can backfill exchange place with nwkwtc trains if we have hobwtc trains`() {
        val trains = departuresMap {
            station(GroveStreet) {
                nwkWtcTrainAt(10, 30)
            }

            station(ExchangePlace) {
                hobWtcTrainAt(10, 30)
            }
        }

        val backfilled = TrainBackfillHelper.withBackfill(trains)
        val expTrains = backfilled[ExchangePlace]
        val expTrainTimes = expTrains?.map { it.projectedArrival.printForAssertions() }
        assertEquals(listOf("10:30", "10:33"), expTrainTimes)
    }

    @Test
    fun `can backfill exchange place with hobwtc trains if we have nwkwtc trains`() {
        val trains = departuresMap {
            station(Newport) {
                hobWtcTrainAt(10, 30)
            }

            station(ExchangePlace) {
                nwkWtcTrainAt(10, 30)
            }
        }

        val backfilled = TrainBackfillHelper.withBackfill(trains)
        val expTrains = backfilled[ExchangePlace]
        val expTrainTimes = expTrains?.map { it.projectedArrival.printForAssertions() }
        assertEquals(listOf("10:30", "10:35"), expTrainTimes)
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

                override fun nwkWtcTrainAt(hour: Int, minute: Int) {
                    val time = date.atTime(hour, minute)
                    map.getOrPut(station) { mutableListOf() }
                        .add(nwkWtcTrain(time.toUtcInstant()))
                }

                override fun hobWtcTrainAt(hour: Int, minute: Int) {
                    val time = date.atTime(hour, minute)
                    map.getOrPut(station) { mutableListOf() }
                        .add(hobWtcTrain(time.toUtcInstant()))
                }

                override fun wtcHobTrainAt(hour: Int, minute: Int) {
                    val time = date.atTime(hour, minute)
                    map.getOrPut(station) { mutableListOf() }
                        .add(wtcHobTrain(time.toUtcInstant()))
                }

                override fun wtcJsqTrainAt(hour: Int, minute: Int) {
                    val time = date.atTime(hour, minute)
                    map.getOrPut(station) { mutableListOf() }
                        .add(wtcJsqTrain(time.toUtcInstant()))
                }
            }
            block(scope)
        }

        interface StationScope {
            fun newarkTrainAt(hour: Int, minute: Int)
            fun nwkWtcTrainAt(hour: Int, minute: Int)
            fun hobWtcTrainAt(hour: Int, minute: Int)
            fun wtcHobTrainAt(hour: Int, minute: Int)
            fun wtcJsqTrainAt(hour: Int, minute: Int)
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
                lineColors = Colors.NwkWtc,
                isDelayed = false,
                backfillSource = null,
                directionState = NewJersey
            )
        }

        fun nwkWtcTrain(projectedArrival: Instant): DepartureBoardTrain {
            return DepartureBoardTrain(
                headsign = "World Trade Center",
                projectedArrival = projectedArrival,
                lineColors = Colors.NwkWtc,
                isDelayed = false,
                backfillSource = null,
                directionState = NewYork
            )
        }

        fun hobWtcTrain(projectedArrival: Instant): DepartureBoardTrain {
            return DepartureBoardTrain(
                headsign = "World Trade Center",
                projectedArrival = projectedArrival,
                lineColors = Colors.HobWtc,
                isDelayed = false,
                backfillSource = null,
                directionState = NewYork
            )
        }

        fun wtcHobTrain(projectedArrival: Instant): DepartureBoardTrain {
            return DepartureBoardTrain(
                headsign = "Hoboken",
                projectedArrival = projectedArrival,
                lineColors = Colors.HobWtc,
                isDelayed = false,
                backfillSource = null,
                directionState = NewJersey
            )
        }

        fun wtcJsqTrain(projectedArrival: Instant): DepartureBoardTrain {
            return DepartureBoardTrain(
                headsign = "Journal Square",
                projectedArrival = projectedArrival,
                lineColors = Colors.NwkWtc,
                isDelayed = false,
                backfillSource = null,
                directionState = NewJersey
            )
        }

        private fun List<ColorWrapper>.unwrap(): List<Color> {
            return map { it.color }
        }
    }
}
