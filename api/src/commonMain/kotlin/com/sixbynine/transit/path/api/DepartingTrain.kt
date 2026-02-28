package com.sixbynine.transit.path.api

import com.sixbynine.transit.path.model.ColorWrapper
import com.sixbynine.transit.path.util.InstantAsISO8601Serializer
import kotlinx.serialization.Serializable
import kotlin.time.Instant

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
    @Serializable(InstantAsISO8601Serializer::class)
    val projectedArrival: Instant,
)

val DepartingTrain.terminalStation: Station? get() = Stations.fromHeadSign(headsign)
