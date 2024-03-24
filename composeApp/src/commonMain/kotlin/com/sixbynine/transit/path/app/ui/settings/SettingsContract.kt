package com.sixbynine.transit.path.app.ui.settings

import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.TrainFilter
import com.sixbynine.transit.path.app.settings.StationLimit
import com.sixbynine.transit.path.app.settings.TimeDisplay
import com.sixbynine.transit.path.app.ui.ScreenScope
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.State

object SettingsContract {
    data class State(
        val locationSetting: LocationSettingState,
        val timeDisplay: TimeDisplay,
        val trainFilter: TrainFilter,
        val lines: Set<Line>,
        val stationLimit: StationLimit,
        val stationSort: StationSort,
        val showPresumedTrains: Boolean,
        val bottomSheet: BottomSheetType? = null,
        val hasLocationPermission: Boolean,
    )

    enum class BottomSheetType {
        StationLimit, StationSort, TimeDisplay, TrainFilter, Lines
    }

    enum class LocationSettingState {
        NotAvailable, Disabled, Enabled
    }

    sealed interface Intent {
        data class TrainFilterChanged(val filter: TrainFilter) : Intent
        data class LineFilterToggled(val filter: Line, val isChecked: Boolean) : Intent
        data class TimeDisplayChanged(val display: TimeDisplay) : Intent
        data class StationSortSelected(val sort: StationSort) : Intent
        data class StationLimitSelected(val limit: StationLimit) : Intent
        data class ShowPresumedTrainsChanged(val show: Boolean) : Intent
        data class LocationSettingChanged(val use: Boolean) : Intent
        data object StationLimitClicked : Intent
        data object StationSortClicked : Intent
        data object TimeDisplayClicked : Intent
        data object TrainFilterClicked : Intent
        data object LinesClicked : Intent
        data object BottomSheetDismissed : Intent
        data object BackClicked : Intent
        data object SendFeedbackClicked : Intent
        data object RateAppClicked : Intent
        data object ShareAppClicked : Intent
        data object BuyMeACoffeeClicked : Intent
    }

    sealed interface Effect {
        data object GoBack : Effect
    }
}

typealias SettingsScope = ScreenScope<State, Intent>