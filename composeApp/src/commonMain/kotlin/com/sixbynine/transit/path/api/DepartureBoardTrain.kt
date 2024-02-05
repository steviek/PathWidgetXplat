package com.sixbynine.transit.path.api

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

data class DepartureBoardTrain(
    val headsign: String,
    val projectedArrival: Instant,
    val lineColors: List<Color>,
    val isDelayed: Boolean,
    val backfillSource: BackfillSource?,
    val directionState: State?,
)

@Serializable
data class BackfillSource(
    val station: Station,
    val projectedArrival: Instant,
)
