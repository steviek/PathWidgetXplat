package com.sixbynine.transit.path.app.ui.station

import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.app.ui.ScreenScope
import com.sixbynine.transit.path.app.ui.station.StationContract.Intent
import com.sixbynine.transit.path.app.ui.station.StationContract.State

object StationContract {
    data class State(
        val station: Station?
    )



    sealed interface Intent {
        data object BackClicked : Intent

    }

    sealed interface Effect {
        data object GoBack : Effect
    }
}

typealias StationScope = ScreenScope<State, Intent>