package com.desaiwang.transit.path.widget.configuration

import com.desaiwang.transit.path.api.Line
import com.desaiwang.transit.path.api.StationSort
import com.desaiwang.transit.path.api.TrainFilter
import com.desaiwang.transit.path.preferences.IntPersistable
import kotlinx.serialization.Serializable
import kotlin.contracts.contract

/** The persisted data for a widget. */
@Serializable
data class StoredWidgetConfiguration(
    val fixedStations: Set<String>? = null,
    private val linesBitmask: Int? = null,
    val useClosestStation: Boolean = false,
    val sortOrder: StationSort? = null,
    val filter: TrainFilter? = null,
    val version: Int = 1,
) {
    val lines: Set<Line>
        get() = IntPersistable.fromBitmask<Line>(linesBitmask ?: 0)
}

fun StoredWidgetConfiguration?.needsSetup(): Boolean {
    contract { returns(false) implies (this@needsSetup != null) }
    return this == null || (fixedStations.isNullOrEmpty() && !useClosestStation)
}
