package com.sixbynine.transit.path.widget.configuration

import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.TrainFilter
import kotlinx.serialization.Serializable
import kotlin.contracts.contract

/** The persisted data for a widget. */
@Serializable
data class StoredWidgetConfiguration(
    val fixedStations: Set<String>? = null,
    val useClosestStation: Boolean = false,
    val sortOrder: StationSort? = null,
    val filter: TrainFilter? = null,
)

fun StoredWidgetConfiguration?.needsSetup(): Boolean {
    contract { returns(false) implies (this@needsSetup != null)}
    return this == null || (fixedStations.isNullOrEmpty() && !useClosestStation)
}
