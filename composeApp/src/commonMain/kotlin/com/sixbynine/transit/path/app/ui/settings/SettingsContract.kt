package com.sixbynine.transit.path.app.ui.settings

import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.DepartureBoardTrainFilter
import com.sixbynine.transit.path.app.ui.ScreenScope
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.State

object SettingsContract {
    data class State(
        val locationSetting: LocationSettingState,
        val trainFilter: DepartureBoardTrainFilter,
        val lines: Set<Line>,
        val stationSort: StationSort,
        val showPresumedTrains: Boolean,
        val bottomSheet: BottomSheetType? = null,
        val hasLocationPermission: Boolean,
        val devOptionsEnabled: Boolean,
    )

    enum class BottomSheetType {
        StationSort, TrainFilter, Lines
    }

    enum class LocationSettingState {
        NotAvailable, Disabled, Enabled
    }

    sealed interface Intent {
        data class TrainFilterChanged(val filter: DepartureBoardTrainFilter) : Intent
        data class LineFilterToggled(val filter: Line, val isChecked: Boolean) : Intent
        data class StationSortSelected(val sort: StationSort) : Intent
        data class ShowPresumedTrainsChanged(val show: Boolean) : Intent
        data class LocationSettingChanged(val use: Boolean) : Intent
        data object StationSortClicked : Intent
        data object TrainFilterClicked : Intent
        data object LinesClicked : Intent
        data object BottomSheetDismissed : Intent
        data object BackClicked : Intent
        data object SendFeedbackClicked : Intent
        data object RateAppClicked : Intent
        data object ShareAppClicked : Intent
        data object BuyMeACoffeeClicked : Intent
        data object AdvancedSettingsClicked : Intent
        data object HeaderTapped : Intent
        data object DevOptionsClicked : Intent
    }

    sealed interface Effect {
        data object GoBack : Effect
        data object GoToAdvancedSettings : Effect
    }
}

typealias SettingsScope = ScreenScope<State, Intent>