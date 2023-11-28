package com.sixbynine.transit.path.api

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

data class DepartureBoardTrain(
    val headsign: String,
    val projectedArrival: Instant,
    val lineColors: List<Color>
)
