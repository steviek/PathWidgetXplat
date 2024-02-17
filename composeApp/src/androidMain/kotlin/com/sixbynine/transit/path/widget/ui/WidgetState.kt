package com.sixbynine.transit.path.widget.ui

import com.sixbynine.transit.path.util.DataResult
import com.sixbynine.transit.path.widget.WidgetData
import kotlinx.datetime.Instant

data class WidgetState(
    val result: DataResult<WidgetData>,
    val updateTime: Instant,
    val needsSetup: Boolean,
)
