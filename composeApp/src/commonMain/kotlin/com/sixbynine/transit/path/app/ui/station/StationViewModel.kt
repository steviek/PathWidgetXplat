package com.sixbynine.transit.path.app.ui.station

import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.TrainFilter
import com.sixbynine.transit.path.api.TrainFilter.Companion.matchesFilter
import com.sixbynine.transit.path.api.impl.SchedulePathApi
import com.sixbynine.transit.path.api.matches
import com.sixbynine.transit.path.app.lifecycle.AppLifecycleObserver
import com.sixbynine.transit.path.app.settings.SettingsManager
import com.sixbynine.transit.path.app.settings.TimeDisplay
import com.sixbynine.transit.path.app.ui.BaseViewModel
import com.sixbynine.transit.path.app.ui.common.toAppUiTrainData
import com.sixbynine.transit.path.app.ui.home.HomeScreenViewModel.Companion.toDepartureBoardData
import com.sixbynine.transit.path.app.ui.home.WidgetDataFetchingUseCase
import com.sixbynine.transit.path.app.ui.station.StationContract.Effect
import com.sixbynine.transit.path.app.ui.station.StationContract.Effect.GoBack
import com.sixbynine.transit.path.app.ui.station.StationContract.Intent
import com.sixbynine.transit.path.app.ui.station.StationContract.Intent.BackClicked
import com.sixbynine.transit.path.app.ui.station.StationContract.State
import com.sixbynine.transit.path.time.now
import com.sixbynine.transit.path.util.await
import com.sixbynine.transit.path.util.collectIn
import com.sixbynine.transit.path.util.repeatEvery
import com.sixbynine.transit.path.widget.toCommonUiTrainData
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.milliseconds

class StationViewModel(private val stationId: String?) : BaseViewModel<State, Intent, Effect>(
    initialState = createInitialState()
) {

    private val fetchingUseCase = WidgetDataFetchingUseCase.get(this)
    private val schedulePathApi = SchedulePathApi()
    private val station = stationId?.let { Stations.byId(it) }

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

        viewModelScope.launch {
            station ?: return@launch
            val scheduleData =
                schedulePathApi
                    .getUpcomingDepartures(
                        now = now(),
                        minTrainTime = now(),
                        maxTrainTime = Instant.DISTANT_FUTURE,
                    )
                    .await()
                    .data
                    ?: return@launch

            val scheduledTrains = scheduleData.getTrainsAt(station) ?: return@launch
            val allScheduledTrains =
                scheduledTrains
                    .map { it.toCommonUiTrainData().toAppUiTrainData(timeDisplay = TimeDisplay.Clock) }
                    .sortedBy { it.projectedArrival }
            updateState {
                copy(scheduledTrains = allScheduledTrains)
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
                scheduledTrains = emptyList(),
                timeDisplay = SettingsManager.timeDisplay.value,
                groupByDestination = SettingsManager.groupTrains.value,
            )
        }
    }
}
