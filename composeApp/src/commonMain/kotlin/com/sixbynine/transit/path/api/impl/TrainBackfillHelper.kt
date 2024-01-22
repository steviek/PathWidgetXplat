package com.sixbynine.transit.path.api.impl

import com.sixbynine.transit.path.Logging
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
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object TrainBackfillHelper {

    private val HeadSignToCheckpoints = mapOf(
        "World Trade Center" to mapOf(
            Newark to 0.seconds,
            Harrison to 1.minutes + 48.seconds,
            JournalSquare to 12.minutes + 2.seconds,
            GroveStreet to 16.minutes + 37.seconds,
            ExchangePlace to 19.minutes + 44.seconds
        ),
        "Newark" to mapOf(
            WorldTradeCenter to 0.seconds,
            ExchangePlace to 3.minutes + 49.seconds,
            GroveStreet to 6.minutes + 56.seconds,
            JournalSquare to 9.minutes + 48.seconds,
            Harrison to 16.minutes + 54.seconds
        ),
        "Journal Square via Hoboken" to mapOf(
            ThirtyThirdStreet to 0.seconds,
            TwentyThirdStreet to 1.minutes + 52.seconds,
            FourteenthStreet to 4.minutes + 7.seconds,
            NinthStreet to 5.minutes + 23.seconds,
            ChristopherStreet to 7.minutes + 23.seconds,
            Hoboken to 17.minutes + 30.seconds,
            Newport to 26.minutes + 48.seconds,
            GroveStreet to 30.minutes + 53.seconds,
        ),
        "33rd Street via Hoboken" to mapOf(
            JournalSquare to 0.seconds,
            GroveStreet to 3.minutes + 54.seconds,
            Newport to 7.minutes + 59.seconds,
            Hoboken to 16.minutes + 17.seconds,
            ChristopherStreet to 26.minutes + 13.seconds,
            NinthStreet to 27.minutes + 53.seconds,
            FourteenthStreet to 29.minutes + 3.seconds,
            TwentyThirdStreet to 31.minutes + 13.seconds
        ),
    )

    fun withBackfill(
        trains: Map<Station, List<DepartureBoardTrain>>,
    ): Map<Station, List<DepartureBoardTrain>> {
        val backfilled = trains.toMutableMap()
        trains.keys.forEach eachStation@{ station ->
            val headSigns =
                backfilled[station]?.map { it.headsign }?.distinct() ?: return@eachStation
            headSigns.forEach eachHeadSign@{ headSign ->
                val checkpointsInLine = HeadSignToCheckpoints[headSign] ?: return@eachHeadSign
                val stationCheckpoint = checkpointsInLine[station] ?: return@eachHeadSign
                checkpointsInLine
                    .filterValues { it < stationCheckpoint }
                    .toList()
                    .sortedByDescending { (_, checkpoint) -> checkpoint }
                    .forEach { (priorStation, priorStationCheckpoint) ->
                        val travelTimeBetweenStations = stationCheckpoint - priorStationCheckpoint
                        backfilled[priorStation]
                            ?.filter { it.headsign == headSign }
                            ?.map {
                                it.copy(
                                    projectedArrival = it.projectedArrival +
                                            travelTimeBetweenStations
                                )
                            }
                            ?.forEach { hypotheticalTrain ->
                                val trainMatches: (DepartureBoardTrain) -> Boolean = trainMatches@{
                                    if (it.headsign != headSign) return@trainMatches false
                                    val timeDelta =
                                        (hypotheticalTrain.projectedArrival - it.projectedArrival)
                                            .absoluteValue
                                    timeDelta <= 10.minutes
                                }
                                val currentTrains = backfilled[station] ?: return@eachStation
                                if (currentTrains.none { trainMatches(it) }) {
                                    Logging.d(
                                        "Backfilling ${station.displayName} with a train from " +
                                                "${priorStation.displayName} to $headSign" +
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


}
