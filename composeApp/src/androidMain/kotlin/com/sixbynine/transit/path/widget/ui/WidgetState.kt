package com.sixbynine.transit.path.widget.ui

import com.sixbynine.transit.path.util.DataResult
import com.sixbynine.transit.path.model.DepartureBoardData
import kotlinx.datetime.Instant

data class WidgetState(
    val result: DataResult<DepartureBoardData>,
    val updateTime: Instant,
    val needsSetup: Boolean,
)
