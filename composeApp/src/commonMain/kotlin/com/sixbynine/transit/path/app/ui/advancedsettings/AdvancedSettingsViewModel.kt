package com.sixbynine.transit.path.app.ui.advancedsettings

import com.sixbynine.transit.path.app.settings.SettingsManager
import com.sixbynine.transit.path.app.ui.BaseViewModel
import com.sixbynine.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.BottomSheetType.AvoidMissingTrains
import com.sixbynine.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.BottomSheetType.CommutingSchedule
import com.sixbynine.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Effect
import com.sixbynine.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Effect.GoBack
import com.sixbynine.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent
import com.sixbynine.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.AvoidMissingTrainsChanged
import com.sixbynine.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.AvoidMissingTrainsClicked
import com.sixbynine.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.BackClicked
import com.sixbynine.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.BottomSheetDismissed
import com.sixbynine.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.CommutingScheduleClicked
import com.sixbynine.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AdvancedSettingsViewModel : BaseViewModel<State, Intent, Effect>(
    initialState = createInitialState()
) {

    init {
        updateStateOnEach(SettingsManager.avoidMissingTrains) { copy(avoidMissingTrains = it) }
    }

    override fun onIntent(intent: Intent) {
        when (intent) {
            BackClicked -> sendEffect(GoBack)
            is AvoidMissingTrainsChanged -> {
                SettingsManager.updateAvoidMissingTrains(intent.option)
                updateState { copy(bottomSheet = null) }
            }

            AvoidMissingTrainsClicked -> {
                updateState { copy(bottomSheet = AvoidMissingTrains) }
            }

            BottomSheetDismissed -> {
                updateState { copy(bottomSheet = null) }
            }

            CommutingScheduleClicked -> {
                updateState { copy(bottomSheet = CommutingSchedule) }
            }
        }
    }

    private inline fun <T> updateStateOnEach(flow: Flow<T>, crossinline block: State.(T) -> State) {
        flow.onEach { updateState { block(it) } }.launchIn(viewModelScope)
    }

    private companion object {
        fun createInitialState(): State {
            return State(
                avoidMissingTrains = SettingsManager.avoidMissingTrains.value
            )
        }
    }
}
