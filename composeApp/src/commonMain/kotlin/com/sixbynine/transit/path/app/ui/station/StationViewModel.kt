package com.sixbynine.transit.path.app.ui.station

import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.app.ui.BaseViewModel
import com.sixbynine.transit.path.app.ui.station.StationContract.Effect
import com.sixbynine.transit.path.app.ui.station.StationContract.Effect.GoBack
import com.sixbynine.transit.path.app.ui.station.StationContract.Intent
import com.sixbynine.transit.path.app.ui.station.StationContract.Intent.BackClicked
import com.sixbynine.transit.path.app.ui.station.StationContract.State

class StationViewModel(private val stationId: String?) : BaseViewModel<State, Intent, Effect>(
    initialState = createInitialState(stationId)
) {

    override suspend fun performIntent(intent: Intent) {
        when (intent) {
            BackClicked -> sendEffect(GoBack)
        }
    }

    private companion object {
        fun createInitialState(stationId: String?): State {
            val station = stationId?.let { Stations.byId(it) }
            return State(station = station)
        }
    }
}
