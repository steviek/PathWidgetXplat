package com.desaiwang.transit.path.widget.ui

import com.desaiwang.transit.path.util.DataResult
import com.desaiwang.transit.path.model.DepartureBoardData
import kotlinx.datetime.Instant

data class WidgetState(
    val result: DataResult<DepartureBoardData>,
    val updateTime: Instant,
    val needsSetup: Boolean,
)
