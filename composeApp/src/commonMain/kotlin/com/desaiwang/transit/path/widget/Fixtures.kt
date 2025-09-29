package com.desaiwang.transit.path.widget

import com.desaiwang.transit.path.api.Line.Hoboken33rd
import com.desaiwang.transit.path.api.Line.HobokenWtc
import com.desaiwang.transit.path.api.Line.JournalSquare33rd
import com.desaiwang.transit.path.api.Line.NewarkWtc
import com.desaiwang.transit.path.api.State.NewJersey
import com.desaiwang.transit.path.api.State.NewYork
import com.desaiwang.transit.path.model.Colors
import com.desaiwang.transit.path.model.DepartureBoardData
import com.desaiwang.transit.path.model.DepartureBoardData.SignData
import com.desaiwang.transit.path.model.DepartureBoardData.StationData
import com.desaiwang.transit.path.model.DepartureBoardData.TrainData
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.minutes

object Fixtures {

    fun widgetData(limit: Int = Int.MAX_VALUE): DepartureBoardData {
        val now = Clock.System.now()
        return DepartureBoardData(
            stations = listOf(
                StationData(
                    id = "JSQ",
                    displayName = "Journal Square",
                    signs = listOf(
                        SignData(
                            title = "WTC",
                            colors = Colors.NwkWtc,
                            lines = setOf(NewarkWtc),
                            projectedArrivals = listOf(
                                now + 1.minutes,
                                now + 6.minutes,
                                now + 11.minutes
                            )
                        ),
                        SignData(
                            title = "NWK",
                            colors = Colors.NwkWtc,
                            lines = setOf(NewarkWtc),
                            projectedArrivals = listOf(now + 2.minutes, now + 7.minutes)
                        ),
                        SignData(
                            title = "33S",
                            colors = Colors.Jsq33s,
                            lines = setOf(JournalSquare33rd),
                            projectedArrivals = listOf(now + 3.minutes, now + 13.minutes)
                        ),
                    ),
                    trains = listOf(
                        TrainData(
                            id = "JSQ:1",
                            title = "33S",
                            colors = Colors.Jsq33s,
                            projectedArrival = now + 3.minutes,
                            lines = setOf(JournalSquare33rd)
                        ),
                        TrainData(
                            id = "JSQ:2",
                            title = "NWK",
                            colors = Colors.NwkWtc,
                            projectedArrival = now + 2.minutes,
                            lines = setOf(NewarkWtc)
                        ),
                        TrainData(
                            id = "JSQ:3",
                            title = "WTC",
                            colors = Colors.NwkWtc,
                            projectedArrival = now + 6.minutes,
                            lines = setOf(NewarkWtc)
                        ),
                        TrainData(
                            id = "JSQ:4",
                            title = "NWK",
                            colors = Colors.NwkWtc,
                            projectedArrival = now + 7.minutes,
                            lines = setOf(NewarkWtc)
                        )
                    ),
                    state = NewJersey,
                ),
                StationData(
                    id = "WTC",
                    displayName = "World Trade Center",
                    signs = listOf(
                        SignData(
                            title = "HOB",
                            colors = Colors.HobWtc,
                            lines = setOf(HobokenWtc),
                            projectedArrivals = listOf(now, now + 7.minutes)
                        ),
                        SignData(
                            title = "NWK",
                            colors = Colors.NwkWtc,
                            lines = setOf(NewarkWtc),
                            projectedArrivals = listOf(now + 3.minutes, now + 8.minutes)
                        )
                    ),
                    trains = listOf(
                        TrainData(
                            id = "WTC:1",
                            title = "HOB",
                            colors = Colors.HobWtc,
                            projectedArrival = now + 0.minutes,
                            lines = setOf(HobokenWtc)
                        ),
                        TrainData(
                            id = "WTC:2",
                            title = "NWK",
                            colors = Colors.NwkWtc,
                            projectedArrival = now + 2.minutes,
                            lines = setOf(NewarkWtc)
                        ),
                        TrainData(
                            id = "WTC:3",
                            title = "HOB",
                            colors = Colors.HobWtc,
                            projectedArrival = now + 5.minutes,
                            lines = setOf(HobokenWtc)
                        ),
                        TrainData(
                            id = "WTC:4",
                            title = "NWK",
                            colors = Colors.NwkWtc,
                            projectedArrival = now + 7.minutes,
                            lines = setOf(NewarkWtc)
                        )
                    ),
                    state = NewYork,
                ),
                StationData(
                    id = "NWK",
                    displayName = "Newark",
                    signs = listOf(
                        SignData(
                            title = "WTC",
                            colors = Colors.NwkWtc,
                            lines = setOf(NewarkWtc),
                            projectedArrivals = listOf(
                                now,
                                now + 2.minutes,
                                now + 5.minutes,
                                now + 8.minutes
                            ),
                        )
                    ),
                    trains = listOf(
                        TrainData(
                            id = "NWK:1",
                            title = "WTC",
                            colors = Colors.NwkWtc,
                            projectedArrival = now + 0.minutes,
                            lines = setOf(NewarkWtc)
                        ),
                        TrainData(
                            id = "NWK:2",
                            title = "WTC",
                            colors = Colors.NwkWtc,
                            projectedArrival = now + 2.minutes,
                            lines = setOf(NewarkWtc)
                        ),
                        TrainData(
                            id = "NWK:3",
                            title = "WTC",
                            colors = Colors.NwkWtc,
                            projectedArrival = now + 5.minutes,
                            lines = setOf(NewarkWtc)
                        ),
                        TrainData(
                            id = "NWK:4",
                            title = "WTC",
                            colors = Colors.NwkWtc,
                            projectedArrival = now + 8.minutes,
                            lines = setOf(NewarkWtc)
                        )
                    ),
                    state = NewJersey,
                ),
                StationData(
                    id = "33S",
                    displayName = "33rd Street",
                    signs = listOf(
                        SignData(
                            title = "HOB",
                            colors = Colors.Hob33s,
                            lines = setOf(Hoboken33rd),
                            projectedArrivals = listOf(now + 1.minutes, now + 6.minutes),
                        ),
                        SignData(
                            title = "JSQ",
                            colors = Colors.Jsq33s,
                            lines = setOf(JournalSquare33rd),
                            projectedArrivals = listOf(
                                now + 3.minutes,
                                now + 7.minutes,
                                now + 11.minutes
                            ),
                        )
                    ),
                    trains = listOf(
                        TrainData(
                            id = "33S:1",
                            title = "HOB",
                            colors = Colors.Hob33s,
                            projectedArrival = now + 1.minutes,
                            lines = setOf(Hoboken33rd)
                        ),
                        TrainData(
                            id = "33S:2",
                            title = "JSQ",
                            colors = Colors.Jsq33s,
                            projectedArrival = now + 3.minutes,
                            lines = setOf(JournalSquare33rd)
                        ),
                        TrainData(
                            id = "33S:3",
                            title = "HOB",
                            colors = Colors.Hob33s,
                            projectedArrival = now + 6.minutes,
                            lines = setOf(Hoboken33rd)
                        ),
                        TrainData(
                            id = "33S:4",
                            title = "JSQ",
                            colors = Colors.Jsq33s,
                            projectedArrival = now + 7.minutes,
                            lines = setOf(JournalSquare33rd)
                        )
                    ),
                    state = NewYork,
                )
            ).take(limit),
            fetchTime = now,
            nextFetchTime = now + 12.minutes,
            closestStationId = null,
            isPathApiBroken = false,
            scheduleName = null,
        )
    }
}
