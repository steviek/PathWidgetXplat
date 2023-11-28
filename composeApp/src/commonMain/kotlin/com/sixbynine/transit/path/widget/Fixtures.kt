package com.sixbynine.transit.path.widget

import com.sixbynine.transit.path.ui.Colors
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.minutes

object Fixtures {

    fun widgetData(limit: Int = Int.MAX_VALUE): WidgetData {
        val now = Clock.System.now()
        return WidgetData(
            stations = listOf(
                WidgetData.StationData(
                    id = "JSQ",
                    displayName = "Journal Square",
                    signs = emptyList(),
                    trains = listOf(
                        WidgetData.TrainData(
                            id = "JSQ:1",
                            title = "33S",
                            colors = Colors.Jsq33s,
                            projectedArrival = now + 3.minutes
                        ),
                        WidgetData.TrainData(
                            id = "JSQ:2",
                            title = "NWK",
                            colors = Colors.NwkWtc,
                            projectedArrival = now + 2.minutes
                        ),
                        WidgetData.TrainData(
                            id = "JSQ:3",
                            title = "WTC",
                            colors = Colors.NwkWtc,
                            projectedArrival = now + 6.minutes
                        ),
                        WidgetData.TrainData(
                            id = "JSQ:4",
                            title = "NWK",
                            colors = Colors.NwkWtc,
                            projectedArrival = now + 7.minutes
                        )
                    ),
                ),
                WidgetData.StationData(
                    id = "WTC",
                    displayName = "World Trade Center",
                    signs = emptyList(),
                    trains = listOf(
                        WidgetData.TrainData(
                            id = "WTC:1",
                            title = "HOB",
                            colors = Colors.HobWtc,
                            projectedArrival = now + 0.minutes
                        ),
                        WidgetData.TrainData(
                            id = "WTC:2",
                            title = "NWK",
                            colors = Colors.NwkWtc,
                            projectedArrival = now + 2.minutes
                        ),
                        WidgetData.TrainData(
                            id = "WTC:3",
                            title = "HOB",
                            colors = Colors.HobWtc,
                            projectedArrival = now + 5.minutes
                        ),
                        WidgetData.TrainData(
                            id = "WTC:4",
                            title = "NWK",
                            colors = Colors.NwkWtc,
                            projectedArrival = now + 7.minutes
                        )
                    ),
                ),
                WidgetData.StationData(
                    id = "NWK",
                    displayName = "Newark",
                    signs = emptyList(),
                    trains = listOf(
                        WidgetData.TrainData(
                            id = "NWK:1",
                            title = "WTC",
                            colors = Colors.NwkWtc,
                            projectedArrival = now + 0.minutes
                        ),
                        WidgetData.TrainData(
                            id = "NWK:2",
                            title = "WTC",
                            colors = Colors.NwkWtc,
                            projectedArrival = now + 2.minutes
                        ),
                        WidgetData.TrainData(
                            id = "NWK:3",
                            title = "WTC",
                            colors = Colors.NwkWtc,
                            projectedArrival = now + 5.minutes
                        ),
                        WidgetData.TrainData(
                            id = "NWK:4",
                            title = "WTC",
                            colors = Colors.NwkWtc,
                            projectedArrival = now + 8.minutes
                        )
                    ),
                ),
                WidgetData.StationData(
                    id = "33S",
                    displayName = "33rd Street",
                    signs = emptyList(),
                    trains = listOf(
                        WidgetData.TrainData(
                            id = "33S:1",
                            title = "HOB",
                            colors = Colors.Hob33s,
                            projectedArrival = now + 1.minutes
                        ),
                        WidgetData.TrainData(
                            id = "33S:2",
                            title = "JSQ",
                            colors = Colors.Jsq33s,
                            projectedArrival = now + 3.minutes
                        ),
                        WidgetData.TrainData(
                            id = "33S:3",
                            title = "HOB",
                            colors = Colors.Hob33s,
                            projectedArrival = now + 6.minutes
                        ),
                        WidgetData.TrainData(
                            id = "33S:4",
                            title = "JSQ",
                            colors = Colors.Jsq33s,
                            projectedArrival = now + 7.minutes
                        )
                    ),
                )
            ).take(limit),
            fetchTime = now,
            nextFetchTime = now + 12.minutes,
        )
    }
}
