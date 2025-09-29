package com.desaiwang.transit.path.app.ui.advancedsettings

import com.desaiwang.transit.path.app.settings.CommutingConfiguration
import com.desaiwang.transit.path.app.settings.SettingsManager
import com.desaiwang.transit.path.app.ui.BaseViewModel
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.BottomSheetType.AvoidMissingTrains
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.BottomSheetType.CommutingSchedule
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.BottomSheetType.StationLimit
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.BottomSheetType.TimeDisplay
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Effect
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Effect.GoBack
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.AvoidMissingTrainsChanged
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.AvoidMissingTrainsClicked
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.BackClicked
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.BottomSheetDismissed
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.CommutingConfigurationChanged
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.CommutingScheduleClicked
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.StationLimitClicked
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.StationLimitSelected
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.TimeDisplayChanged
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.TimeDisplayClicked
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent.TrainGroupingClicked
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AdvancedSettingsViewModel : BaseViewModel<State, Intent, Effect>(
    initialState = createInitialState()
) {

    init {
        updateStateOnEach(SettingsManager.timeDisplay) { copy(timeDisplay = it) }
        updateStateOnEach(SettingsManager.avoidMissingTrains) { copy(avoidMissingTrains = it) }
        updateStateOnEach(SettingsManager.stationLimit) { copy(stationLimit = it) }
        updateStateOnEach(SettingsManager.commutingConfiguration) {
            copy(commutingConfiguration = it)
        }
        updateStateOnEach(SettingsManager.groupTrains) {
            copy(groupTrains = it)
        }
    }

    override val rateLimitedIntents: Set<Intent> = setOf(
        AvoidMissingTrainsClicked,
        BackClicked,
        CommutingScheduleClicked,
        StationLimitClicked,
        TimeDisplayClicked
    )

    override suspend fun performIntent(intent: Intent) {
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

            is CommutingConfigurationChanged -> {
                updateState {
                    copy(commutingConfiguration = intent.configuration)
                }
                SettingsManager.updateCommutingConfiguration(intent.configuration)
            }

            is StationLimitSelected -> {
                SettingsManager.updateStationLimit(intent.limit)
                updateState { copy(bottomSheet = null) }
            }

            StationLimitClicked -> {
                updateState { copy(bottomSheet = StationLimit) }
            }

            is TimeDisplayChanged -> {
                SettingsManager.updateTimeDisplay(intent.display)
                updateState { copy(bottomSheet = null) }
            }

            TimeDisplayClicked -> {
                updateState { copy(bottomSheet = TimeDisplay) }
            }

            is TrainGroupingClicked -> {
                SettingsManager.updateGroupTrains(!intent.isEnabled)
            }
        }
    }

    private inline fun <T> updateStateOnEach(flow: Flow<T>, crossinline block: State.(T) -> State) {
        flow.onEach { updateState { block(it) } }.launchIn(lightweightScope)
    }

    private companion object {
        fun createInitialState(): State {
            return State(
                avoidMissingTrains = SettingsManager.avoidMissingTrains.value,
                timeDisplay = SettingsManager.timeDisplay.value,
                stationLimit = SettingsManager.stationLimit.value,
                groupTrains = SettingsManager.groupTrains.value,
                commutingConfiguration = CommutingConfiguration.default(),
            )
        }
    }
}
