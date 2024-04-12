package com.sixbynine.transit.path.app.ui.home

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.api.LocationSetting.Enabled
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.StationSort.Alphabetical
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.TrainFilter.Companion.matchesFilter
import com.sixbynine.transit.path.api.alerts.AlertText
import com.sixbynine.transit.path.api.alerts.isDisplayedNow
import com.sixbynine.transit.path.api.isInNewJersey
import com.sixbynine.transit.path.api.isInNewYork
import com.sixbynine.transit.path.api.matches
import com.sixbynine.transit.path.app.external.ExternalRoutingManager
import com.sixbynine.transit.path.app.lifecycle.AppLifecycleObserver
import com.sixbynine.transit.path.app.settings.SettingsManager
import com.sixbynine.transit.path.app.settings.TimeDisplay
import com.sixbynine.transit.path.app.station.StationSelectionManager
import com.sixbynine.transit.path.app.ui.PathViewModel
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.DepartureBoardData
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Effect
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Effect.NavigateToSettings
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.HomeBackfillSource
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.AddStationClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.AlertUrlClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.ConstraintsChanged
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.EditClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.MoveStationDownClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.MoveStationUpClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.RemoveStationClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.RetryClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.SettingsClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.StationBottomSheetDismissed
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.StationBottomSheetSelection
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.StationSelectionDialogDismissed
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.StopEditingClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.UpdateNowClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.State
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.StationData
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.TrainData
import com.sixbynine.transit.path.app.ui.layout.LayoutOption.OneColumn
import com.sixbynine.transit.path.app.ui.layout.LayoutOption.ThreeColumns
import com.sixbynine.transit.path.app.ui.layout.LayoutOption.TwoColumns
import com.sixbynine.transit.path.app.ui.layout.LayoutOptionManager
import com.sixbynine.transit.path.time.now
import com.sixbynine.transit.path.util.launchAndReturnUnit
import com.sixbynine.transit.path.util.repeatEvery
import com.sixbynine.transit.path.widget.WidgetData
import com.sixbynine.transit.path.widget.WidgetDataFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.delay_time
import pathwidgetxplat.composeapp.generated.resources.langauge_code
import pathwidgetxplat.composeapp.generated.resources.update_footer_text
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

class HomeScreenViewModel(maxWidth: Dp, maxHeight: Dp) : PathViewModel<State, Intent, Effect>() {

    private val fetchingUseCase = WidgetDataFetchingUseCase(viewModelScope)
    private val fetchData get() = fetchingUseCase.fetchData.value

    private val _state = MutableStateFlow(
        State(
            isLoading = fetchData.isFetching,
            hasError = fetchData.hasError,
            isTablet = maxWidth >= 480.dp && maxHeight >= 480.dp,
            selectedStations = StationSelectionManager.selection.value.selectedStations,
            unselectedStations = StationSelectionManager.selection.value.unselectedStations,
            layoutOption = LayoutOptionManager.layoutOption ?: defaultLayout(maxWidth),
            useColumnForFooter = maxWidth < 480.dp,
            isEditing = false,
            timeDisplay = SettingsManager.timeDisplay.value,
            stationSort = SettingsManager.stationSort.value,
            updateFooterText = null,
            data = null
        )
    )
    override val state = _state.asStateFlow()

    private val _effects = Channel<Effect>()
    override val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            val footerText = createFooterText()
            val initialData = fetchData.data?.toDepartureBoardData()?.adjustedForLatestSettings()
            updateState {
                copy(
                    updateFooterText = updateFooterText ?: footerText,
                    data = data ?: initialData
                )
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            fetchingUseCase.fetchData.collectLatest { fetchData ->
                repeatEvery(250.milliseconds) {
                    AppLifecycleObserver.isActive.first { it } // Make sure the UI is visible

                    updateState {
                        copy(
                            isLoading = fetchData.isFetching,
                            hasError = fetchData.hasError,
                            updateFooterText = createFooterText(),
                            data = createDepartureBoardData()
                        )
                    }
                }
            }
        }

        updateStateOnEach(StationSelectionManager.selection) {
            copy(
                selectedStations = it.selectedStations,
                unselectedStations = it.unselectedStations,
            )
        }

        updateStateOnEach(SettingsManager.settings) {
            copy(
                stationSort = it.stationSort,
                timeDisplay = it.timeDisplay,
                data = createDepartureBoardData()
            )
        }
    }

    override fun onIntent(intent: Intent) = viewModelScope.launchAndReturnUnit {
        when (intent) {
            RetryClicked, UpdateNowClicked -> fetchingUseCase.fetchNow()
            EditClicked -> updateState { copy(isEditing = true) }
            StopEditingClicked -> updateState { copy(isEditing = false) }
            SettingsClicked -> sendEffect(NavigateToSettings)

            is StationSelectionDialogDismissed -> {
                updateState { copy(showStationSelectionDialog = false) }
                StationSelectionManager.updateSelection(intent.state)
            }

            is MoveStationDownClicked -> {
                StationSelectionManager.moveDown(
                    intent.id,
                    groupedByState = state.value.stationSort != Alphabetical
                )
            }

            is MoveStationUpClicked -> {
                StationSelectionManager.moveUp(
                    intent.id,
                    groupedByState = state.value.stationSort != Alphabetical
                )
            }

            is RemoveStationClicked -> {
                StationSelectionManager.remove(intent.id)
            }

            StationBottomSheetDismissed -> {
                updateState { copy(showAddStationBottomSheet = false) }
            }

            is StationBottomSheetSelection -> {
                updateState { copy(showAddStationBottomSheet = false) }
                StationSelectionManager.add(intent.station.pathApiName)
            }

            AddStationClicked -> {
                updateState { copy(showAddStationBottomSheet = true) }
            }

            is ConstraintsChanged -> {
                val (maxWidth, maxHeight) = intent
                updateState {
                    copy(
                        isTablet = maxWidth >= 480.dp && maxHeight >= 480.dp,
                        layoutOption = LayoutOptionManager.layoutOption ?: defaultLayout(maxWidth),
                    )
                }
            }

            is AlertUrlClicked -> {
                ExternalRoutingManager().openUrl(intent.url)
            }
        }
    }

    private suspend fun createFooterText(): String? = with(fetchData) {
        val formattedFetchTime =
            WidgetDataFormatter.formatTimeWithSeconds(lastFetchTime ?: return@with null)
        // could be localized better, but this works for en and es
        return getString(
            string.update_footer_text,
            formattedFetchTime,
            "${timeUntilNextFetch.inWholeSeconds}s"
        )
    }

    private suspend fun createDepartureBoardData(): DepartureBoardData? {
        return fetchData.data
            ?.toDepartureBoardData()
            ?.adjustedForLatestSettings()
    }

    private inline fun <T> updateStateOnEach(flow: Flow<T>, crossinline block: suspend State.(T) -> State) {
        flow.onEach { updateState { block(it) } }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    private suspend fun updateState(operation: suspend State.() -> State) {
        _state.value = operation(state.value)
    }

    private suspend fun DepartureBoardData.adjustedForLatestSettings(): DepartureBoardData {
        val stationToIndex =
            StationSelectionManager
                .selection
                .value
                .selectedStations
                .mapIndexed { index, station -> station.pathApiName to index }
                .toMap()
        val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
        val isMorning = hour in 3 until 12
        return copy(
            stations = stations
                .sortedBy { stationToIndex[it.station.pathApiName] }
                .sortedBy {
                    if (it.isClosest) return@sortedBy -1

                    val station = it.station
                    val isFirst = when (SettingsManager.stationSort.value) {
                        StationSort.NjAm -> isMorning == station.isInNewJersey
                        StationSort.NyAm -> isMorning == station.isInNewYork
                        else -> return@sortedBy 0
                    }
                    if (isFirst) 0 else 1
                }
                .map { data ->
                    data.copy(
                        trains = data.trains
                            .filterNot { it.shouldHideForPresumption() }
                            .filter { train ->
                                SettingsManager.lineFilter.value.any { it.matches(train) }
                            }
                            .filter(StationLimitFilter(SettingsManager.stationLimit.value))
                            .map { train ->
                                train.copy(
                                    displayText = trainDisplayTime(
                                        SettingsManager.timeDisplay.value,
                                        train.isDelayed,
                                        train.isBackfilled,
                                        train.projectedArrival
                                    )
                                )
                            }
                    )
                }
        )
    }

    private fun TrainData.shouldHideForPresumption(): Boolean {
        return isBackfilled && !SettingsManager.displayPresumedTrains.value
    }

    private fun defaultLayout(maxWidth: Dp) = when (maxWidth) {
        in 0.dp..600.dp -> OneColumn
        in 600.dp..1200.dp -> TwoColumns
        else -> ThreeColumns
    }

    private fun sendEffect(effect: Effect) {
        viewModelScope.launch {
            _effects.send(effect)
        }
    }

    companion object {
        suspend fun WidgetData.toDepartureBoardData(
            timeDisplay: TimeDisplay = SettingsManager.timeDisplay.value
        ): DepartureBoardData {
            val stations = stations.mapNotNull { data ->
                val station =
                    Stations.All.firstOrNull { it.pathApiName == data.id }
                        ?: return@mapNotNull null
                val alertToDisplay = data.alerts?.firstOrNull { it.isDisplayedNow() }
                val stationData = StationData(
                    station = station,
                    trains = data.trains
                        .filter { it.projectedArrival >= now() - 1.minutes }
                        .filter { matchesFilter(station, it, SettingsManager.trainFilter.value) }
                        .map { train ->
                            TrainData(
                                id = train.id,
                                title = train.title,
                                colors = train.colors,
                                displayText = trainDisplayTime(
                                    timeDisplay,
                                    isDelayed = train.isDelayed,
                                    isBackfilled = train.isBackfilled,
                                    train.projectedArrival
                                ),
                                projectedArrival = train.projectedArrival,
                                isDelayed = train.isDelayed,
                                backfill = train.backfillSource?.let {
                                    HomeBackfillSource(
                                        it,
                                        trainDisplayTime(
                                            timeDisplay,
                                            isDelayed = train.isDelayed,
                                            isBackfilled = false,
                                            it.projectedArrival
                                        )
                                    )
                                },
                            )
                        },
                    isClosest = data.id == closestStationId &&
                            SettingsManager.locationSetting.value == Enabled,
                    alertText = alertToDisplay?.message?.unpack(),
                    alertUrl = alertToDisplay?.url?.unpack(),
                )
                stationData
            }
            return DepartureBoardData(stations = stations)
        }

        private suspend fun trainDisplayTime(
            timeDisplay: TimeDisplay,
            isDelayed: Boolean,
            isBackfilled: Boolean,
            projectedArrival: Instant
        ): String {
            return with(StringBuilder()) {
                if (isBackfilled) append("~")

                if (isDelayed) append(getString(string.delay_time))

                val time = when (timeDisplay) {
                    TimeDisplay.Relative -> WidgetDataFormatter.formatRelativeTime(
                        Clock.System.now(),
                        projectedArrival
                    )

                    TimeDisplay.Clock -> WidgetDataFormatter.formatTime(projectedArrival)
                }
                append(time)

                toString()
            }
        }

        private suspend fun AlertText.unpack(): String? {
            val languageCode = getString(string.langauge_code)
            return localizations.find { it.locale == languageCode }?.text
                ?: localizations.firstOrNull()?.text
        }
    }
}