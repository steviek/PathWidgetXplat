package com.sixbynine.transit.path.api.impl

import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.api.BackfillSource
import com.sixbynine.transit.path.api.DepartureBoardTrain
import com.sixbynine.transit.path.api.State
import com.sixbynine.transit.path.api.State.NewJersey
import com.sixbynine.transit.path.api.State.NewYork
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.Stations
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
import com.sixbynine.transit.path.time.NewYorkTimeZone
import com.sixbynine.transit.path.util.orElse
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek.SATURDAY
import kotlinx.datetime.DayOfWeek.SUNDAY
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object TrainBackfillHelper {

    private const val ShouldLog = false

    private data class LineId(
        val headSign: String,
        val colors: List<ColorWrapper>,
        val direction: State?
    ) {
        companion object {

            val NWK_WTC = LineId(
                headSign = "World Trade Center",
                colors = Colors.NwkWtc,
                direction = NewYork
            )
            val WTC_NWK = LineId(
                headSign = "Newark",
                colors = Colors.NwkWtc,
                direction = NewJersey
            )
            val OK_33S_JSQ = LineId(
                headSign = "Journal Square",
                colors = Colors.Jsq33s,
                direction = NewJersey
            )
            val OK_JSQ_33S = LineId(
                headSign = "33rd Street",
                colors = Colors.Jsq33s,
                direction = NewYork
            )
            val PAIN_33S_JSQ = LineId(
                headSign = "Journal Square via Hoboken",
                colors = Colors.Hob33s + Colors.Jsq33s,
                direction = NewJersey
            )
            val PAIN_JSQ_33S = LineId(
                headSign = "33rd Street via Hoboken",
                colors = Colors.Hob33s + Colors.Jsq33s,
                direction = NewYork
            )
            val HOB_WTC = LineId(
                headSign = "World Trade Center",
                colors = Colors.HobWtc,
                direction = NewYork
            )
            val WTC_HOB = LineId(
                headSign = "Hoboken",
                colors = Colors.HobWtc,
                direction = NewJersey
            )
            val OK_HOB_33S = LineId(
                headSign = "33rd Street",
                colors = Colors.Hob33s,
                direction = NewYork
            )
            val OK_33S_HOB = LineId(
                headSign = "Hoboken",
                colors = Colors.Hob33s,
                direction = NewJersey
            )
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

    fun getCheckpoints(route: String): Map<Station, Duration>? {
        return when (route) {
            "NWK_WTC" -> LineIdToCheckpoints[NWK_WTC]
            "WTC_NWK" -> LineIdToCheckpoints[WTC_NWK]
            "JSQ_33S" -> LineIdToCheckpoints[OK_JSQ_33S]
            "33S_JSQ" -> LineIdToCheckpoints[OK_33S_JSQ]
            "JSQ_HOB_33S" -> LineIdToCheckpoints[PAIN_JSQ_33S]
            "33S_HOB_JSQ" -> LineIdToCheckpoints[PAIN_33S_JSQ]
            "WTC_HOB" -> LineIdToCheckpoints[WTC_HOB]
            "HOB_WTC" -> LineIdToCheckpoints[HOB_WTC]
            "HOB_33S" -> LineIdToCheckpoints[OK_HOB_33S]
            "33S_HOB" -> LineIdToCheckpoints[OK_33S_HOB]
            "WTC_JSQ" -> LineIdToCheckpoints[WTC_NWK]
                ?.filterKeys { it != Harrison && it != JournalSquare }
            "JSQ_WTC" -> LineIdToCheckpoints[NWK_WTC]
                ?.filterKeys { it != Newark && it != Harrison }
                ?.mapValues { (_, checkpoint) -> checkpoint - 12.minutes + 43.seconds }
            "NWK_HAR" -> mapOf(Newark to 0.minutes)
            "HAR_NWK" -> mapOf(Harrison to 0.minutes)
            else -> null
        }
    }

    // Map to check for when trains are heading in the same direction with the same color as the
    // main line, but stopping at an earlier station. This checks that e.g. a train from World Trade
    // Center heading to Journal Square with the right color will match with the WTC-NWK line.
    private val LineIdAliases: Map<LineId, LineId> = run {
        val aliases = mutableMapOf<LineId, LineId>()
        LineIdToCheckpoints.forEach { (lineId, checkpoints) ->
            checkpoints.keys.forEach { station ->
                val alias = lineId.copy(headSign = station.displayName)
                aliases[alias] = lineId
            }
        }
        aliases
    }

    fun withBackfill(
        trains: Map<String, List<DepartureBoardTrain>>,
    ): Map<String, List<DepartureBoardTrain>> {
        val backfilled = trains.toMutableMap()
        trains.keys.forEach eachStation@{ stationKey ->
            val station =
                Stations.All.firstOrNull { it.pathApiName == stationKey }
                    ?: return@eachStation
            if (ShouldLog) {
                Logging.d("Backfill station: ${station.displayName}")
            }
            val lineIdToEarliestTrain =
                trains[stationKey]
                    .orElse { return@eachStation }
                    .groupBy { it.lineId }
                    .mapValues { (_, trains) ->
                        trains.minByOrNull { it.projectedArrival }?.projectedArrival
                    }
                    .toMutableMap()
            if (station == ExchangePlace) {
                // We avoid backfilling from lines that aren't already on the departure board for
                // the station. But Exchange Place is a special case, if we have a train going there
                // from one of the WTC lines, we assume the other line is also stopping there.
                lineIdToEarliestTrain[HOB_WTC] =
                    listOfNotNull(
                        lineIdToEarliestTrain[HOB_WTC],
                        lineIdToEarliestTrain[NWK_WTC]
                    ).minOrNull()
                lineIdToEarliestTrain[NWK_WTC] =
                    listOfNotNull(
                        lineIdToEarliestTrain[HOB_WTC],
                        lineIdToEarliestTrain[NWK_WTC]
                    ).minOrNull()
            }
            lineIdToEarliestTrain.forEach eachHeadSign@{ (lineId, earliestTrain) ->
                val checkpointsInLine = LineIdToCheckpoints[lineId] ?: run {
                    if (ShouldLog) {
                        Logging.d("\tBackfill: No checkpoints for ${station.displayName} for $lineId!")
                    }
                    return@eachHeadSign
                }
                val stationCheckpoint = checkpointsInLine[station] ?: return@eachHeadSign
                checkpointsInLine
                    .filterValues { it < stationCheckpoint }
                    .toList()
                    .sortedByDescending { (_, checkpoint) -> checkpoint }
                    .forEach { (priorStation, priorStationCheckpoint) ->
                        val travelTimeBetweenStations = stationCheckpoint - priorStationCheckpoint
                        trains[priorStation.pathApiName]
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
                            ?.filter { earliestTrain != null && it.projectedArrival > earliestTrain }
                            ?.forEach eachTrain@ { hypotheticalTrain ->
                                val trainMatches: (DepartureBoardTrain) -> Boolean = trainMatches@{
                                    if (it.lineId != lineId) return@trainMatches false
                                    val timeDelta =
                                        (hypotheticalTrain.projectedArrival - it.projectedArrival)
                                            .absoluteValue
                                    timeDelta <= getCloseTrainThreshold()
                                }
                                val currentTrains =
                                    backfilled[station.pathApiName] ?: return@eachStation

                                val trainTerminalStation =
                                    Stations.fromHeadSign(hypotheticalTrain.headsign)
                                val trainTerminalCheckpoint =
                                    trainTerminalStation?.let { checkpointsInLine[it] }
                                if (trainTerminalCheckpoint != null &&
                                    trainTerminalCheckpoint <= stationCheckpoint) {
                                    // This covers a case where the train is running on a modified
                                    // route stopping before this station. Example: don't backfill
                                    // Harrison with a 'Newark'-line train from WTC that is
                                    // terminating at JSQ.
                                    return@eachTrain
                                }

                                if (currentTrains.none { trainMatches(it) }) {
                                    if (ShouldLog) {
                                        Logging.d(
                                            "\tBackfilling ${station.displayName} with a train from " +
                                                    "${priorStation.displayName} to $lineId" +
                                                    " hypothetically departing at " +
                                                    "${hypotheticalTrain.projectedArrival}," +
                                                    "train going to $trainTerminalStation"
                                        )
                                    }

                                    backfilled[station.pathApiName] =
                                        currentTrains + hypotheticalTrain
                                } else {
                                    if (ShouldLog) {
                                        Logging.d(
                                            "\tSkip backfilling ${station.displayName} with a train from " +
                                                    "${priorStation.displayName} to $lineId" +
                                                    " hypothetically departing at " +
                                                    "${hypotheticalTrain.projectedArrival}"
                                        )
                                    }
                                }
                            }
                    }
            }
        }
        return backfilled
    }

    private fun getCloseTrainThreshold(): Duration {
        val date = Clock.System.now().toLocalDateTime(NewYorkTimeZone)
        if (date.dayOfWeek in listOf(SATURDAY, SUNDAY)) {
            return 10.minutes
        }

        if (date.time.hour in 6..10 || date.time.hour in 16..20) {
            return 4.minutes
        }

        return 10.minutes
    }

    private val DepartureBoardTrain.lineId: LineId
        get() {
            val id = LineId(headsign, lineColors, directionState)
            return LineIdAliases[id] ?: id
        }
}
