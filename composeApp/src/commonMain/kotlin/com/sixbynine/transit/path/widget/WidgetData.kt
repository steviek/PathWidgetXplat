package com.sixbynine.transit.path.widget

import com.sixbynine.transit.path.ui.ColorWrapper
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

data class WidgetData(
    val stations: List<StationData>,
    val fetchTime: Instant,
    val nextFetchTime: Instant,
) {
    data class StationData(
        val id: String,
        val displayName: String,
        val signs: List<SignData>,
        val trains: List<TrainData>,
    )

    data class SignData(
        val title: String,
        val colors: List<ColorWrapper>,
        val projectedArrivals: List<Instant>
    )

    data class TrainData(
        val id: String,
        val title: String,
        val colors: List<ColorWrapper>,
        val projectedArrival: Instant
    ) {
        fun isPast(now: Instant): Boolean {
            return projectedArrival < now - 1.minutes
        }
    }
}
