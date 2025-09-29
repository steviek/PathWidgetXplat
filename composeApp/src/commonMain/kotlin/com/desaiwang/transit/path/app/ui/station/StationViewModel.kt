package com.desaiwang.transit.path.app.ui.station

import com.desaiwang.transit.path.api.TrainFilter
import com.desaiwang.transit.path.api.TrainFilter.Companion.matchesFilter
import com.desaiwang.transit.path.api.matches
import com.desaiwang.transit.path.app.lifecycle.AppLifecycleObserver
import com.desaiwang.transit.path.app.settings.SettingsManager
import com.desaiwang.transit.path.app.ui.BaseViewModel
import com.desaiwang.transit.path.app.ui.home.HomeScreenViewModel.Companion.toDepartureBoardData
import com.desaiwang.transit.path.app.ui.home.WidgetDataFetchingUseCase
import com.desaiwang.transit.path.app.ui.station.StationContract.Effect
import com.desaiwang.transit.path.app.ui.station.StationContract.Effect.GoBack
import com.desaiwang.transit.path.app.ui.station.StationContract.Intent
import com.desaiwang.transit.path.app.ui.station.StationContract.Intent.BackClicked
import com.desaiwang.transit.path.app.ui.station.StationContract.State
import com.desaiwang.transit.path.util.collectIn
import com.desaiwang.transit.path.util.repeatEvery
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class StationViewModel(private val stationId: String?) : BaseViewModel<State, Intent, Effect>(
    initialState = createInitialState()
) {

    private val fetchingUseCase = WidgetDataFetchingUseCase.get(this)

    init {
        lightweightScope.launch {
            fetchingUseCase.fetchData.collectLatest { fetchData ->
                stationId ?: return@collectLatest

                repeatEvery(250.milliseconds) {
                    AppLifecycleObserver.isActive.first { it } // Make sure the UI is visible

                    val stationData =
                        fetchData
                            .data
                            ?.toDepartureBoardData(trainFilter = TrainFilter.All)
                            ?.stations
                            ?.find { it.id == stationId }

                    val allTrains = stationData?.trains.orEmpty()
                    val trainFilter = SettingsManager.trainFilter.value
                    val (matching, notMatching) = allTrains.partition { train ->
                        SettingsManager.lineFilter.value
                            .any { it.matches(train, stationId = stationId) } &&
                                matchesFilter(
                                    stationData?.station ?: return@partition false,
                                    train,
                                    trainFilter
                                )
                    }

                    updateState {
                        copy(
                            station = stationData,
                            trainsMatchingFilters = matching,
                            otherTrains = notMatching,
                        )
                    }
                }
            }
        }

        SettingsManager.timeDisplay.collectIn(viewModelScope) {
            updateState {
                copy(timeDisplay = it)
            }
        }

        SettingsManager.groupTrains.collectIn(viewModelScope) {
            updateState {
                copy(groupByDestination = it)
            }
        }
    }

    override suspend fun performIntent(intent: Intent) {
        when (intent) {
            BackClicked -> sendEffect(GoBack)
        }
    }

    override fun onCleared() {
        fetchingUseCase.unsubscribe(this)
    }

    private companion object {
        fun createInitialState(): State {
            return State(
                trainsMatchingFilters = emptyList(),
                otherTrains = emptyList(),
                timeDisplay = SettingsManager.timeDisplay.value,
                groupByDestination = SettingsManager.groupTrains.value,
            )
        }
    }
}
