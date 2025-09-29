package com.desaiwang.transit.path.api.impl

import com.desaiwang.transit.path.Logging
import com.desaiwang.transit.path.api.BackfillSource
import com.desaiwang.transit.path.api.DepartingTrain
import com.desaiwang.transit.path.api.State
import com.desaiwang.transit.path.api.State.NewJersey
import com.desaiwang.transit.path.api.State.NewYork
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
import com.desaiwang.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.HOB_WTC
import com.desaiwang.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.NWK_WTC
import com.desaiwang.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.OK_33S_HOB
import com.desaiwang.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.OK_33S_JSQ
import com.desaiwang.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.OK_HOB_33S
import com.desaiwang.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.OK_JSQ_33S
import com.desaiwang.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.PAIN_33S_JSQ
import com.desaiwang.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.PAIN_JSQ_33S
import com.desaiwang.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.WTC_33S
import com.desaiwang.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.WTC_FROM_33S
import com.desaiwang.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.WTC_HOB
import com.desaiwang.transit.path.api.impl.TrainBackfillHelper.LineId.Companion.WTC_NWK
import com.desaiwang.transit.path.api.isInNewYork
import com.desaiwang.transit.path.model.ColorWrapper
import com.desaiwang.transit.path.model.Colors
import com.desaiwang.transit.path.time.NewYorkTimeZone
import com.desaiwang.transit.path.util.orElse
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek.SATURDAY
import kotlinx.datetime.DayOfWeek.SUNDAY
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Helper class that predicts train arrivals at future stations based on known departures.
 * 
 * This class:
 * 1. Maintains travel time data between station pairs
 * 2. Has different timing tables for peak/off-peak service
 * 3. Handles train routing logic for different service patterns
 * 4. Helps fill in arrival predictions when real-time data is missing
 * 5. Accounts for different travel times based on time of day and day of week
 */
object TrainBackfillHelper {

    private const val ShouldLog = false

    private data class LineId(
        val headSign: String,
        val colors: List<ColorWrapper>,
        val direction: State?,
        val code: String? = null,
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
            val WTC_33S = LineId(
                headSign = "33rd Street",
                colors = Colors.HobWtc,
                direction = NewYork,
                code = "WTC_33S",
            )
            val WTC_FROM_33S = LineId(
                headSign = "World Trade Center",
                colors = Colors.HobWtc,
                direction = NewYork,
                code = "33S_WTC",
            )
        }
    }

    private val LineIdToCheckpointsFaster = mapOf(
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
            Harrison to 22.minutes + 33.seconds
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
        WTC_33S to mapOf(
            WorldTradeCenter to 0.minutes,
            ExchangePlace to 3.minutes + 42.seconds,
            Newport to 8.minutes + 38.seconds,
            ChristopherStreet to 15.minutes + 13.seconds,
            NinthStreet to 17.minutes + 13.seconds,
            FourteenthStreet to 18.minutes + 14.seconds,
            TwentyThirdStreet to 20.minutes + 14.seconds,
        ),
        WTC_FROM_33S to mapOf(
            ThirtyThirdStreet to 0.minutes,
            TwentyThirdStreet to 1.minutes + 42.seconds,
            FourteenthStreet to 3.minutes + 31.seconds,
            NinthStreet to 4.minutes + 31.seconds,
            ChristopherStreet to 6.minutes + 31.seconds,
            Newport to 16.minutes + 31.seconds,
            ExchangePlace to 21.minutes + 31.seconds,
        )
    )

    private val LineIdToCheckpointsSlower = mapOf(
        NWK_WTC to mapOf(
            Newark to 0.minutes,
            Harrison to 2.minutes,
            JournalSquare to 16.minutes,
            GroveStreet to 21.minutes,
            ExchangePlace to 24.minutes
        ),
        WTC_NWK to mapOf(
            WorldTradeCenter to 0.minutes,
            ExchangePlace to 4.minutes,
            GroveStreet to 7.minutes,
            JournalSquare to 12.minutes,
            Harrison to 23.minutes
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
        WTC_33S to LineIdToCheckpointsFaster[WTC_33S],
        WTC_FROM_33S to LineIdToCheckpointsFaster[WTC_FROM_33S],
    )

    fun getCheckpoints(route: String, isSlowTime: Boolean): Map<Station, Duration>? {
        val checkpoints =
            if (isSlowTime) LineIdToCheckpointsSlower else LineIdToCheckpointsFaster
        return when (route) {
            "NWK_WTC" -> checkpoints[NWK_WTC]
            "WTC_NWK" -> checkpoints[WTC_NWK]
            "JSQ_33S" -> checkpoints[OK_JSQ_33S]
            "33S_JSQ" -> checkpoints[OK_33S_JSQ]
            "JSQ_HOB_33S" -> checkpoints[PAIN_JSQ_33S]
            "33S_HOB_JSQ" -> checkpoints[PAIN_33S_JSQ]
            "WTC_HOB" -> checkpoints[WTC_HOB]
            "HOB_WTC" -> checkpoints[HOB_WTC]
            "HOB_33S" -> checkpoints[OK_HOB_33S]
            "33S_HOB" -> checkpoints[OK_33S_HOB]
            "WTC_JSQ" -> checkpoints[WTC_NWK]
                ?.filterKeys { it != Harrison && it != JournalSquare }

            "JSQ_WTC" -> checkpoints[NWK_WTC]
                ?.filterKeys { it != Newark && it != Harrison }
                ?.mapValues { (_, checkpoint) ->
                    checkpoint - checkpoints[NWK_WTC]!![JournalSquare]!!
                }

            "NWK_HAR" -> mapOf(Newark to 0.minutes)
            "HAR_NWK" -> mapOf(Harrison to 0.minutes)
            "WTC_33S" -> checkpoints[WTC_33S]
            "33S_WTC" -> checkpoints[WTC_FROM_33S]
            else -> null
        }
    }

    // Map to check for when trains are heading in the same direction with the same color as the
    // main line, but stopping at an earlier station. This checks that e.g. a train from World Trade
    // Center heading to Journal Square with the right color will match with the WTC-NWK line.
    private val LineIdAliases: Map<LineId, LineId> = run {
        val aliases = mutableMapOf<LineId, LineId>()
        LineIdToCheckpointsFaster.forEach { (lineId, checkpoints) ->
            checkpoints.keys.forEach { station ->
                val alias = lineId.copy(headSign = station.displayName)
                aliases[alias] = lineId
            }
        }
        aliases
    }

    fun withBackfill(
        trains: Map<String, List<DepartingTrain>>,
    ): Map<String, List<DepartingTrain>> {
        val backfilled =
            trains.mapValues { (_, value) -> value.toMutableList() }.toMutableMap()

        trains.forEach eachStation@{ (stationKey, stationTrains) ->
            val station = Stations.byId(stationKey) ?: return@eachStation
            if (ShouldLog) {
                Logging.d("Backfill from station: ${station.displayName}")
            }
            stationTrains.forEach eachTrain@{ train ->
                val lineId = train.getLineId(station)
                val checkpoints = LineIdToCheckpointsFaster[lineId].orElse {
                    if (ShouldLog) {
                        Logging.d(
                            "    Backfill: No checkpoints for ${station.displayName} for" +
                                    "$lineId!"
                        )
                    }
                    return@eachTrain
                }

                infix fun Station.isLaterInLineThan(other: Station): Boolean {
                    val d1 = checkpoints[this] ?: return false
                    val d2 = checkpoints[other] ?: return true
                    return d2 < d1
                }

                val stationCheckpointDuration = checkpoints[station] ?: return@eachTrain
                checkpoints.forEach eachCheckpoint@{ (futureStation, futureStationDuration) ->
                    val travelTime = futureStationDuration - stationCheckpointDuration
                    if (travelTime <= Duration.ZERO) {
                        // earlier station in the line if this is negative...
                        return@eachCheckpoint
                    }

                    val trainTerminalStation = Stations.fromHeadSign(train.headsign)
                    if (trainTerminalStation != null &&
                        checkpoints.containsKey(trainTerminalStation) &&
                        !(trainTerminalStation isLaterInLineThan futureStation)
                    ) {
                        // Don't backfill stations with trains that stop earlier than the station.
                        // e.g. don't backfill Harrison with a WTC-JSQ train.
                        return@eachCheckpoint
                    }

                    val hypotheticalTrain = train.copy(
                        projectedArrival = train.projectedArrival + travelTime,
                        backfillSource = train.backfillSource ?: BackfillSource(
                            station = station,
                            projectedArrival = train.projectedArrival,
                        )
                    )

                    val trainsForStation =
                        backfilled.getOrPut(futureStation.pathApiName) { arrayListOf() }
                    for (i in trainsForStation.indices.reversed()) {
                        val knownTrain = trainsForStation[i]
                        if (knownTrain.getLineId(station) != lineId) continue
                        if ((knownTrain.projectedArrival - hypotheticalTrain.projectedArrival)
                                .absoluteValue > getCloseTrainThreshold()
                        ) {
                            continue
                        }

                        if (knownTrain.backfillSource == null ||
                            knownTrain.backfillSource.station isLaterInLineThan station
                        ) {
                            // live time or better backfill. move along
                            return@eachCheckpoint
                        }

                        trainsForStation.removeAt(i)
                    }

                    trainsForStation.add(hypotheticalTrain)
                    if (ShouldLog) {
                        Logging.d("Add train to ${futureStation.displayName} at ${hypotheticalTrain.projectedArrival} from ${station.displayName} train at ${train.projectedArrival}")
                    }
                }
            }
        }

        backfilled.values.forEach { it.sortBy { it.projectedArrival } }
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

    private fun DepartingTrain.getLineId(station: Station): LineId {
        val destination = Stations.fromHeadSign(headsign)
        if (station.isInNewYork && destination == WorldTradeCenter) {
            return WTC_FROM_33S
        }

        if (station == ExchangePlace && destination == ThirtyThirdStreet) {
            return WTC_33S
        }

        if (station == WorldTradeCenter && destination == ThirtyThirdStreet) {
            return WTC_33S
        }

        if (station == Newport && destination == ThirtyThirdStreet && lineColors == Colors.HobWtc) {
            return WTC_33S
        }

        val id = LineId(headsign, lineColors, directionState)
        return LineIdAliases[id] ?: id
    }
}
