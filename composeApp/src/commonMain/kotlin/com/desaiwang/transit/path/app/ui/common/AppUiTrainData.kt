package com.desaiwang.transit.path.app.ui.common

import com.desaiwang.transit.path.api.BackfillSource
import com.desaiwang.transit.path.api.Station
import com.desaiwang.transit.path.model.ColorWrapper
import kotlinx.datetime.Instant

data class AppUiTrainData(
    val id: String,
    val title: String,
    val colors: List<ColorWrapper>,
    val projectedArrival: Instant,
    val displayText: String,
    val isDelayed: Boolean = false,
    val backfill: AppUiBackfillSource? = null,
) {
    val isBackfilled: Boolean
        get() = backfill != null
}

data class AppUiBackfillSource(
    val source: BackfillSource,
    val displayText: String,
) {
    val projectedArrival: Instant
        get() = source.projectedArrival

    val station: Station
        get() = source.station
}
