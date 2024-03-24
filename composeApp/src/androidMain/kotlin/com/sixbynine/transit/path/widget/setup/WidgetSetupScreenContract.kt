package com.sixbynine.transit.path.widget.setup

import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.State.NewJersey
import com.sixbynine.transit.path.api.State.NewYork
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.TrainFilter
import com.sixbynine.transit.path.api.state
import com.sixbynine.transit.path.app.ui.ScreenScope
import com.sixbynine.transit.path.location.AndroidLocationProvider
import com.sixbynine.transit.path.widget.StationByDisplayNameComparator
import com.sixbynine.transit.path.widget.setup.WidgetSetupScreenContract.Intent
import com.sixbynine.transit.path.widget.setup.WidgetSetupScreenContract.State

object WidgetSetupScreenContract {
    data class State(
        val isClosestStationAvailable: Boolean =
            AndroidLocationProvider.isLocationSupportedByDevice,
        val useClosestStation: Boolean = false,
        val njStations: List<StationRow> = defaultStations(NewJersey),
        val nyStations: List<StationRow> = defaultStations(NewYork),
        val sortOrder: StationSort = StationSort.Alphabetical,
        val filter: TrainFilter = TrainFilter.All,
        val lines: Set<Line> = Line.entries.toSet(),
        val appWidgetId: Int = INVALID_APPWIDGET_ID,
    ) {
        val isConfirmButtonEnabled =
            useClosestStation ||
                    njStations.any { it.checked } ||
                    nyStations.any { it.checked }
    }

    private fun defaultStations(state: com.sixbynine.transit.path.api.State): List<StationRow> {
        return Stations.All
            .filter { it.state == state }
            .sortedWith(StationByDisplayNameComparator)
            .map { StationRow(it.pathApiName, it.displayName, false) }
    }

    data class StationRow(
        val id: String,
        val displayName: String,
        val checked: Boolean,
    )

    sealed interface Intent {
        data class UseClosestStationToggled(val checked: Boolean) : Intent
        data class StationToggled(val id: String, val checked: Boolean) : Intent
        data class LineToggled(val line: Line, val checked: Boolean) : Intent
        data class SortOrderSelected(val sortOrder: StationSort) : Intent
        data class TrainFilterSelected(val filter: TrainFilter) : Intent
        data object ConfirmClicked : Intent
    }

    sealed interface Effect {
        data class CompleteConfigurationIntent(val appWidgetId: Int) : Effect
    }
}

typealias WidgetSetupScreenScope = ScreenScope<State, Intent>
