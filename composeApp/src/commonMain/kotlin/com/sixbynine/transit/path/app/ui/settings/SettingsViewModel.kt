package com.sixbynine.transit.path.app.ui.settings

import com.sixbynine.transit.path.app.settings.SettingsManager
import com.sixbynine.transit.path.app.ui.BaseViewModel
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.BottomSheetType.StationLimit
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.BottomSheetType.StationSort
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.BottomSheetType.TimeDisplay
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.BottomSheetType.TrainFilter
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Effect
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Effect.GoBack
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.BackClicked
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.BottomSheetDismissed
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.ShowPresumedTrainsChanged
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.StationLimitClicked
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.StationLimitSelected
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.StationSortClicked
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.StationSortSelected
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.TimeDisplayChanged
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.TimeDisplayClicked
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.TrainFilterChanged
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.TrainFilterClicked
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SettingsViewModel : BaseViewModel<State, Intent, Effect>(
    State(
        timeDisplay = SettingsManager.timeDisplay.value,
        trainFilter = SettingsManager.trainFilter.value,
        stationLimit = SettingsManager.stationLimit.value,
        stationSort = SettingsManager.stationSort.value,
        showPresumedTrains = SettingsManager.displayPresumedTrains.value
    )
) {
    init {
        updateStateOnEach(SettingsManager.timeDisplay) { copy(timeDisplay = it) }
        updateStateOnEach(SettingsManager.trainFilter) { copy(trainFilter = it) }
        updateStateOnEach(SettingsManager.stationLimit) { copy(stationLimit = it) }
        updateStateOnEach(SettingsManager.stationSort) { copy(stationSort = it) }
        updateStateOnEach(SettingsManager.displayPresumedTrains) { copy(showPresumedTrains = it) }


        combine(
            SettingsManager.timeDisplay,
            SettingsManager.trainFilter,
            SettingsManager.stationLimit,
            SettingsManager.stationSort,
            SettingsManager.displayPresumedTrains
        ) { timeDisplay, trainFilter, stationLimit, stationSort, showPresumedTrains ->
            State(
                timeDisplay = timeDisplay,
                trainFilter = trainFilter,
                stationLimit = stationLimit,
                stationSort = stationSort,
                showPresumedTrains = showPresumedTrains
            )
        }
    }

    private inline fun <T> updateStateOnEach(flow: Flow<T>, crossinline block: State.(T) -> State) {
        flow.onEach { updateState { block(it) } }.launchIn(viewModelScope)
    }

    override fun onIntent(intent: Intent) {
        when (intent) {
            is TimeDisplayChanged -> {
                SettingsManager.updateTimeDisplay(intent.display)
                updateState { copy(bottomSheet = null) }
            }

            is TrainFilterChanged -> SettingsManager.updateTrainFilter(intent.filter)

            is StationLimitSelected -> {
                SettingsManager.updateStationLimit(intent.limit)
                updateState { copy(bottomSheet = null) }
            }

            is StationSortSelected -> {
                SettingsManager.updateStationSort(intent.sort)
                updateState { copy(bottomSheet = null) }
            }

            is ShowPresumedTrainsChanged -> {
                SettingsManager.updateDisplayPresumedTrains(intent.show)
            }

            BackClicked -> sendEffect(GoBack)

            StationLimitClicked -> {
                updateState { copy(bottomSheet = StationLimit) }
            }

            StationSortClicked -> {
                updateState { copy(bottomSheet = StationSort) }
            }

            TimeDisplayClicked -> {
                updateState { copy(bottomSheet = TimeDisplay) }
            }

            BottomSheetDismissed -> {
                updateState { copy(bottomSheet = null) }
            }

            TrainFilterClicked -> {
                updateState { copy(bottomSheet = TrainFilter) }
            }
        }
    }
}
