package com.sixbynine.transit.path.app.ui.home

import androidx.compose.ui.unit.Dp
import com.sixbynine.transit.path.api.BackfillSource
import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.state
import com.sixbynine.transit.path.app.settings.TimeDisplay
import com.sixbynine.transit.path.app.station.StationSelection
import com.sixbynine.transit.path.app.ui.ColorWrapper
import com.sixbynine.transit.path.app.ui.layout.LayoutOption
import kotlinx.datetime.Instant

object HomeScreenContract {
    data class State(
        val isTablet: Boolean,
        val selectedStations: List<Station>,
        val unselectedStations: List<Station>,
        val layoutOption: LayoutOption,
        val isEditing: Boolean,
        val timeDisplay: TimeDisplay,
        val stationSort: StationSort = StationSort.Alphabetical,
        val isLoading: Boolean = true,
        val hasError: Boolean = false,
        val isPathApiBusted: Boolean = false,
        val scheduleName: String? = null,
        val data: DepartureBoardData? = null,
        val showStationSelectionDialog: Boolean = false,
        val showAddStationBottomSheet: Boolean = false,
        val useColumnForFooter: Boolean = false,
        val updateFooterText: String? = null,
        val groupByDestination: Boolean = true,
    )

    data class DepartureBoardData(
        val stations: List<StationData>,
        val globalAlerts: List<GlobalAlert>
    )

    data class StationData(
        val station: Station,
        val trains: List<TrainData>,
        val isClosest: Boolean,
        val alertText: String?,
        val alertUrl: String?,
        val alertIsWarning: Boolean,
    ) {
        val id get() = station.pathApiName
        val state get() = station.state
    }

    data class TrainData(
        val id: String,
        val title: String,
        val colors: List<ColorWrapper>,
        val projectedArrival: Instant,
        val displayText: String,
        val isDelayed: Boolean = false,
        val backfill: HomeBackfillSource? = null,
    ) {
        val isBackfilled: Boolean
            get() = backfill != null
    }

    data class HomeBackfillSource(
        val source: BackfillSource,
        val displayText: String,
    ) {
        val projectedArrival: Instant
            get() = source.projectedArrival

        val station: Station
            get() = source.station
    }

    data class GlobalAlert(
        val text: String,
        val url: String?,
        val isWarning: Boolean,
        val lines: Set<Line>?,
    )

    sealed interface Intent {
        data object RetryClicked : Intent
        data object EditClicked : Intent
        data object StopEditingClicked : Intent
        data object UpdateNowClicked : Intent
        data object SettingsClicked : Intent
        data class MoveStationUpClicked(val id: String) : Intent
        data class MoveStationDownClicked(val id: String) : Intent
        data class RemoveStationClicked(val id: String) : Intent
        data class StationSelectionDialogDismissed(val state: StationSelection) : Intent
        data object StationBottomSheetDismissed : Intent
        data class StationBottomSheetSelection(val station: Station) : Intent
        data object AddStationClicked : Intent
        data class ConstraintsChanged(val maxWidth: Dp, val maxHeight: Dp) : Intent
        data class StationClicked(val id: String) : Intent
    }

    sealed interface Effect {
        data object NavigateToSettings : Effect
        data class NavigateToStation(val stationId: String) : Effect
    }
}
