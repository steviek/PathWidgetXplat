package com.sixbynine.transit.path.widget

import com.sixbynine.transit.path.api.*
import com.sixbynine.transit.path.api.alerts.Alert
import com.sixbynine.transit.path.app.ui.ColorWrapper
import com.sixbynine.transit.path.widget.WidgetData.StationData
import com.sixbynine.transit.path.widget.WidgetData.TrainData
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.minutes

@Serializable
data class WidgetData(
    val stations: List<StationData>,
    val fetchTime: Instant,
    val nextFetchTime: Instant,
    val closestStationId: String?,
    val isPathApiBroken: Boolean?,
    val scheduleName: String?,
    val globalAlerts: List<Alert>  = emptyList(),
) {
    @Serializable
    data class StationData(
        val id: String,
        val displayName: String,
        val signs: List<SignData>,
        val trains: List<TrainData>,
        val state: State,
        val alerts: List<Alert>? = null,
    )

    @Serializable
    data class SignData(
        val title: String,
        val colors: List<ColorWrapper>,
        val projectedArrivals: List<Instant>,
        val lines: Set<Line>? = null,
    )

    @Serializable
    data class TrainData(
        val id: String,
        val title: String,
        val colors: List<ColorWrapper>,
        val projectedArrival: Instant,
        val isDelayed: Boolean = false,
        val backfillSource: BackfillSource? = null,
        val lines: Set<Line>? = null,
    ) {
        fun isPast(now: Instant): Boolean {
            return projectedArrival < now - 1.minutes
        }

        val isBackfilled: Boolean
            get() = backfillSource != null
    }
}

fun StationData.longId(): Long {
    return Stations.All
        .indexOfFirst { it.pathApiName == id }
        .takeIf { it != -1 }
        ?.toLong()
        ?: (id.hashCode().toLong() + 10)
}

fun DepartureBoardTrain.toCommonUiTrainData(): TrainData {
    val colors = lineColors.distinct()
    return TrainData(
        id = "$headsign:$projectedArrival",
        title = headsign,
        colors = colors,
        projectedArrival = projectedArrival,
        isDelayed = isDelayed,
        backfillSource = backfillSource,
        lines = lines,
    )
}
