package com.desaiwang.transit.path.app.ui.setup

import com.desaiwang.transit.path.api.Station

object SetupScreenContract {
    data class State(val selectedStations: Set<Station> = emptySet())

    sealed interface Intent {
        data class StationCheckedChanged(val station: Station, val isChecked: Boolean) : Intent
        data object ConfirmClicked : Intent
    }

    sealed interface Effect {
        data object NavigateToHome : Effect
    }
}