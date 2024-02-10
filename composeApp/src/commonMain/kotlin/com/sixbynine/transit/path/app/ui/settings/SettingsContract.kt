package com.sixbynine.transit.path.app.ui.settings

import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.TrainFilter
import com.sixbynine.transit.path.app.settings.StationLimit
import com.sixbynine.transit.path.app.settings.TimeDisplay
import com.sixbynine.transit.path.app.ui.ScreenScope
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.State

object SettingsContract {
    data class State(
        val timeDisplay: TimeDisplay,
        val trainFilter: TrainFilter,
        val stationLimit: StationLimit,
        val stationSort: StationSort,
        val showPresumedTrains: Boolean,
        val bottomSheet: BottomSheetType? = null,
    )

    enum class BottomSheetType {
        StationLimit, StationSort, TimeDisplay, TrainFilter
    }

    sealed interface Intent {
        data class TrainFilterChanged(val filter: TrainFilter) : Intent
        data class TimeDisplayChanged(val display: TimeDisplay) : Intent
        data class StationSortSelected(val sort: StationSort) : Intent
        data class StationLimitSelected(val limit: StationLimit) : Intent
        data class ShowPresumedTrainsChanged(val show: Boolean) : Intent
        data object StationLimitClicked : Intent
        data object StationSortClicked : Intent
        data object TimeDisplayClicked : Intent
        data object TrainFilterClicked : Intent
        data object BottomSheetDismissed : Intent
        data object BackClicked : Intent
        data object SendFeedbackClicked : Intent
        data object RateAppClicked : Intent
        data object ShareAppClicked : Intent
    }

    sealed interface Effect {
        data object GoBack : Effect
    }
}

typealias SettingsScope = ScreenScope<State, Intent>