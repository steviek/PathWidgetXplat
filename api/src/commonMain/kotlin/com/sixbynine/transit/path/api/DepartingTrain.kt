package com.desaiwang.transit.path.api

import com.desaiwang.transit.path.model.ColorWrapper
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

data class DepartingTrain(
    val headsign: String,
    val projectedArrival: Instant,
    val lineColors: List<ColorWrapper>,
    val isDelayed: Boolean,
    val backfillSource: BackfillSource?,
    val directionState: State?,
    val lines: Set<Line>,
)

@Serializable
data class BackfillSource(
    val station: Station,
    val projectedArrival: Instant,
)
