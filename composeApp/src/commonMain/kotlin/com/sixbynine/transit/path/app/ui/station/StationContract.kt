package com.sixbynine.transit.path.app.ui.station

import com.sixbynine.transit.path.app.settings.TimeDisplay
import com.sixbynine.transit.path.app.ui.ScreenScope
import com.sixbynine.transit.path.app.ui.common.AppUiTrainData
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.StationData
import com.sixbynine.transit.path.app.ui.station.StationContract.Intent
import com.sixbynine.transit.path.app.ui.station.StationContract.State

object StationContract {
    data class State(
        val station: StationData? = null,
        val trainsMatchingFilters: List<AppUiTrainData>,
        val otherTrains: List<AppUiTrainData>,
        val scheduledTrains: List<AppUiTrainData>,
        val scheduleName: String?,
        val timeDisplay: TimeDisplay,
        val groupByDestination: Boolean,
    )

    sealed interface Intent {
        data object BackClicked : Intent

    }

    sealed interface Effect {
        data object GoBack : Effect
    }
}

typealias StationScope = ScreenScope<State, Intent>