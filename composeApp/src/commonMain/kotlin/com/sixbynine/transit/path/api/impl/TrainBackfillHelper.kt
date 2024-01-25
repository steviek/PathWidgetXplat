package com.sixbynine.transit.path.api.impl

import androidx.compose.ui.graphics.Color
import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.api.BackfillSource
import com.sixbynine.transit.path.api.DepartureBoardTrain
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.Stations.ChristopherStreet
import com.sixbynine.transit.path.api.Stations.ExchangePlace
import com.sixbynine.transit.path.api.Stations.FourteenthStreet
import com.sixbynine.transit.path.api.Stations.GroveStreet
import com.sixbynine.transit.path.api.Stations.Harrison
import com.sixbynine.transit.path.api.Stations.Hoboken
import com.sixbynine.transit.path.api.Stations.JournalSquare
import com.sixbynine.transit.path.api.Stations.Newark
import com.sixbynine.transit.path.api.Stations.Newport
import com.sixbynine.transit.path.api.Stations.NinthStreet
import com.sixbynine.transit.path.api.Stations.ThirtyThirdStreet
import com.sixbynine.transit.path.api.Stations.TwentyThirdStreet
import com.sixbynine.transit.path.api.Stations.WorldTradeCenter
import com.sixbynine.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.HOB_WTC
import com.sixbynine.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.NWK_WTC
import com.sixbynine.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.OK_33S_HOB
import com.sixbynine.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.OK_33S_JSQ
import com.sixbynine.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.OK_HOB_33S
import com.sixbynine.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.OK_JSQ_33S
import com.sixbynine.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.PAIN_33S_JSQ
import com.sixbynine.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.PAIN_JSQ_33S
import com.sixbynine.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.WTC_HOB
import com.sixbynine.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.WTC_NWK
import com.sixbynine.transit.path.app.ui.ColorWrapper
import com.sixbynine.transit.path.app.ui.Colors
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object TrainBackfillHelper {

    private data class LineId(val headSign: String, val colors: List<Color>) {
        companion object {
            private fun List<ColorWrapper>.unwrap(): List<Color> {
                return map { it.color }
            }

            val NWK_WTC = LineId("World Trade Center", Colors.NwkWtc.unwrap())
            val WTC_NWK = NWK_WTC.copy(headSign = "Newark")
            val OK_33S_JSQ = LineId("Journal Square", Colors.Jsq33s.unwrap())
            val OK_JSQ_33S = OK_33S_JSQ.copy(headSign = "33rd Street")
            val PAIN_33S_JSQ = LineId("Journal Square via Hoboken", Colors.Hob33s.unwrap() + Colors.Jsq33s.unwrap())
            val PAIN_JSQ_33S = PAIN_33S_JSQ.copy(headSign = "33rd Street via Hoboken")
            val HOB_WTC = LineId("World Trade Center", Colors.HobWtc.unwrap())
            val WTC_HOB = HOB_WTC.copy(headSign = "Hoboken")
            val OK_HOB_33S = LineId("33rd Street", Colors.Hob33s.unwrap())
            val OK_33S_HOB = OK_HOB_33S.copy(headSign = "Hoboken")
        }
    }

    private val LineIdToCheckpoints = mapOf(
        NWK_WTC to mapOf(
            Newark to 0.minutes,
            Harrison to 1.minutes + 42.seconds,
            JournalSquare to 12.minutes + 43.seconds,
            GroveStreet to 17.minutes + 30.seconds,
            ExchangePlace to 20.minutes + 30.seconds
        ),
        WTC_NWK to mapOf(
            WorldTradeCenter to 0.minutes,
            ExchangePlace to 3.minutes + 24.seconds,
            GroveStreet to 6.minutes + 27.seconds,
            JournalSquare to 11.minutes + 27.seconds,
            Harrison to 16.minutes + 33.seconds
        ),
        HOB_WTC to mapOf(
            Hoboken to 0.minutes,
            Newport to 3.minutes + 42.seconds,
            ExchangePlace to 8.minutes + 42.seconds,
        ),
        WTC_HOB to mapOf(
            WorldTradeCenter to 0.minutes,
            ExchangePlace to 3.minutes + 42.seconds,
            Newport to 8.minutes + 38.seconds,
        ),
        OK_33S_JSQ to mapOf(
            ThirtyThirdStreet to 0.minutes,
            TwentyThirdStreet to 1.minutes + 42.seconds,
            FourteenthStreet to 3.minutes + 31.seconds,
            NinthStreet to 4.minutes + 31.seconds,
            ChristopherStreet to 6.minutes + 31.seconds,
            Newport to 16.minutes + 31.seconds,
            GroveStreet to 20.minutes + 31.seconds,
        ),
        PAIN_33S_JSQ to mapOf(
            ThirtyThirdStreet to 0.minutes,
            TwentyThirdStreet to 1.minutes + 42.seconds,
            FourteenthStreet to 3.minutes + 31.seconds,
            NinthStreet to 4.minutes + 31.seconds,
            ChristopherStreet to 6.minutes + 31.seconds,
            Hoboken to 19.minutes + 48.seconds,
            Newport to 23.minutes + 30.seconds,
            GroveStreet to 27.minutes + 30.seconds,
        ),
        OK_JSQ_33S to mapOf(
            JournalSquare to 0.minutes,
            GroveStreet to 4.minutes + 37.seconds,
            Newport to 8.minutes + 37.seconds,
            ChristopherStreet to 15.minutes + 13.seconds,
            NinthStreet to 17.minutes + 13.seconds,
            FourteenthStreet to 18.minutes + 14.seconds,
            TwentyThirdStreet to 20.minutes + 14.seconds,
        ),
        PAIN_JSQ_33S to mapOf(
            JournalSquare to 0.minutes,
            GroveStreet to 4.minutes + 37.seconds,
            Newport to 8.minutes + 37.seconds,
            Hoboken to 16.minutes + 55.seconds,
            ChristopherStreet to 25.minutes + 37.seconds,
            NinthStreet to 27.minutes + 37.seconds,
            FourteenthStreet to 28.minutes + 38.seconds,
            TwentyThirdStreet to 30.minutes + 38.seconds,
        ),
        OK_33S_HOB to mapOf(
            ThirtyThirdStreet to 0.minutes,
            TwentyThirdStreet to 1.minutes + 42.seconds,
            FourteenthStreet to 3.minutes + 31.seconds,
            NinthStreet to 4.minutes + 31.seconds,
            ChristopherStreet to 6.minutes + 31.seconds,
        ),
        OK_HOB_33S to mapOf(
            Hoboken to 0.minutes,
            ChristopherStreet to 8.minutes + 42.seconds,
            NinthStreet to 10.minutes + 42.seconds,
            FourteenthStreet to 11.minutes + 43.seconds,
            TwentyThirdStreet to 13.minutes + 43.seconds,
        ),
    )

    fun withBackfill(
        trains: Map<Station, List<DepartureBoardTrain>>,
    ): Map<Station, List<DepartureBoardTrain>> {
        val backfilled = trains.toMutableMap()
        trains.keys.forEach eachStation@{ station ->
            val lineIds =
                backfilled[station]?.map { it.lineId }?.distinct() ?: return@eachStation
            lineIds.forEach eachHeadSign@{ lineId ->
                val checkpointsInLine = LineIdToCheckpoints[lineId] ?: return@eachHeadSign
                val stationCheckpoint = checkpointsInLine[station] ?: return@eachHeadSign
                checkpointsInLine
                    .filterValues { it < stationCheckpoint }
                    .toList()
                    .sortedByDescending { (_, checkpoint) -> checkpoint }
                    .forEach { (priorStation, priorStationCheckpoint) ->
                        val travelTimeBetweenStations = stationCheckpoint - priorStationCheckpoint
                        trains[priorStation]
                            ?.filter { it.lineId == lineId }
                            ?.map {
                                it.copy(
                                    projectedArrival = it.projectedArrival +
                                            travelTimeBetweenStations,
                                    backfillSource = BackfillSource(
                                        station = priorStation,
                                        projectedArrival = it.projectedArrival,
                                    )
                                )
                            }
                            ?.forEach { hypotheticalTrain ->
                                val trainMatches: (DepartureBoardTrain) -> Boolean = trainMatches@{
                                    if (it.lineId != lineId) return@trainMatches false
                                    val timeDelta =
                                        (hypotheticalTrain.projectedArrival - it.projectedArrival)
                                            .absoluteValue
                                    timeDelta <= 10.minutes
                                }
                                val currentTrains = backfilled[station] ?: return@eachStation
                                if (currentTrains.none { trainMatches(it) }) {
                                    Logging.d(
                                        "Backfilling ${station.displayName} with a train from " +
                                                "${priorStation.displayName} to $lineId" +
                                                " hypothetically departing at " +
                                                "${hypotheticalTrain.projectedArrival}"
                                    )
                                    backfilled[station] = currentTrains + hypotheticalTrain
                                }
                            }
                    }

            }
        }
        return backfilled
    }

    private val DepartureBoardTrain.lineId get() = LineId(headsign, lineColors)
}
