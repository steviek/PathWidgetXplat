package com.sixbynine.transit.path.app.ui.home

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.MR.strings
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.StationSort.Alphabetical
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.isInNewJersey
import com.sixbynine.transit.path.api.isInNewYork
import com.sixbynine.transit.path.app.lifecycle.AppLifecycleObserver
import com.sixbynine.transit.path.app.settings.SettingsManager
import com.sixbynine.transit.path.app.settings.TimeDisplay
import com.sixbynine.transit.path.app.station.StationSelectionManager
import com.sixbynine.transit.path.app.ui.PathViewModel
import com.sixbynine.transit.path.app.ui.home.ConfigurationItem.Settings
import com.sixbynine.transit.path.app.ui.home.ConfigurationItem.Station
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.DepartureBoardData
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Effect
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Effect.NavigateToSettings
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.HomeBackfillSource
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.AddStationClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.ConfigurationChipClicked
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
import com.sixbynine.transit.path.preferences.BooleanPreferencesKey
import com.sixbynine.transit.path.preferences.LongPreferencesKey
import com.sixbynine.transit.path.preferences.Preferences
import com.sixbynine.transit.path.preferences.StringPreferencesKey
import com.sixbynine.transit.path.preferences.persisting
import com.sixbynine.transit.path.resources.getString
import com.sixbynine.transit.path.widget.WidgetData
import com.sixbynine.transit.path.widget.WidgetDataFetcher
import com.sixbynine.transit.path.widget.WidgetDataFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class HomeScreenViewModel(maxWidth: Dp, maxHeight: Dp) : PathViewModel<State, Intent, Effect>() {

    private var hasLaunchedBefore by persisting(IsFirstLaunchKey)
    private var storedLastFetchTime by persisting(LastFetchKey)
    private var lastFetchTime = MutableStateFlow(
        storedLastFetchTime?.let { Instant.fromEpochMilliseconds(it) }
    )

    private val latestWidgetData = MutableStateFlow(getStoredWidgetData())

    private val _state = MutableStateFlow(
        run {
            val lastFetchTime = lastFetchTime.value
            val isLoading: Boolean
            val updateFooterText: String?
            if (lastFetchTime?.let { it + 1.minutes > Clock.System.now() } == true) {
                isLoading = false
                val nextFetchTime = lastFetchTime + 1.minutes

                val timeUntilNextFetch =
                    (nextFetchTime - Clock.System.now()).coerceAtLeast(Duration.ZERO)
                updateFooterText = createFooterText(lastFetchTime, timeUntilNextFetch)
            } else {
                isLoading = true
                updateFooterText = null
            }
            State(
                isLoading = isLoading,
                isTablet = maxWidth >= 480.dp && maxHeight >= 480.dp,
                selectedStations = StationSelectionManager.selection.value.selectedStations,
                unselectedStations = StationSelectionManager.selection.value.unselectedStations,
                layoutOption = LayoutOptionManager.layoutOption ?: defaultLayout(maxWidth),
                useColumnForFooter = maxWidth < 480.dp,
                isEditing = hasLaunchedBefore != true,
                timeDisplay = SettingsManager.timeDisplay.value,
                stationSort = SettingsManager.stationSort.value,
                updateFooterText = updateFooterText,
                data = latestWidgetData.value?.toDepartureBoardData()
                    ?.adjustedForLatestSettings()
            )
        }
    )
    override val state = _state.asStateFlow()

    private val _effects = Channel<Effect>()
    override val effects = _effects.receiveAsFlow()

    init {
        Logging.d("Create home screen")
        viewModelScope.launch(Dispatchers.Default) {
            lastFetchTime.collectLatest { fetchTime ->
                if (fetchTime == null) return@collectLatest
                while (true) {
                    AppLifecycleObserver.isActive.first { it } // Make sure the UI is visible

                    val nextFetchTime = fetchTime + 1.minutes

                    val timeUntilNextFetch =
                        (nextFetchTime - Clock.System.now()).coerceAtLeast(Duration.ZERO)

                    if (timeUntilNextFetch == Duration.ZERO &&
                        !state.value.isLoading &&
                        !state.value.hasError
                    ) {
                        fetchData(force = false)
                    }

                    if (!state.value.isLoading) {
                        updateState {
                            copy(
                                updateFooterText = createFooterText(fetchTime, timeUntilNextFetch),
                                data = data?.adjustedForLatestSettings()
                            )
                        }
                    }

                    delay(250)
                }
            }
        }

        viewModelScope.launch {
            delay(5.seconds)
            hasLaunchedBefore = true
        }

        updateStateOnEach(StationSelectionManager.selection.drop(1)) {
            copy(
                selectedStations = it.selectedStations,
                unselectedStations = it.unselectedStations,
                data = data?.adjustedForLatestSettings()
            ).also { fetchData(force = false) }
        }

        updateStateOnEach(SettingsManager.stationLimit) {
            copy(data = data?.adjustedForLatestSettings())
        }

        updateStateOnEach(SettingsManager.stationSort) {
            copy(
                stationSort = stationSort,
                data = data?.adjustedForLatestSettings()
            )
        }

        updateStateOnEach(SettingsManager.displayPresumedTrains) {
            copy(data = data?.adjustedForLatestSettings())
        }

        updateStateOnEach(SettingsManager.timeDisplay) {
            copy(
                timeDisplay = it,
                data = data?.adjustedForLatestSettings()
            )
        }

        updateStateOnEach(SettingsManager.trainFilter) {
            copy(data = data?.adjustedForLatestSettings())
        }

        updateStateOnEach(latestWidgetData) {
            copy(
                data = it?.toDepartureBoardData()
                    ?.adjustedForLatestSettings()
            )
        }

        latestWidgetData
            .onEach { storeWidgetData(it) }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: Intent) {
        when (intent) {
            RetryClicked -> fetchData(force = true)
            UpdateNowClicked -> fetchData(force = true)
            EditClicked -> updateState { copy(isEditing = true) }
            StopEditingClicked -> updateState { copy(isEditing = false) }
            SettingsClicked -> sendEffect(NavigateToSettings)
            is ConfigurationChipClicked -> {
                when (intent.item) {
                    Station -> {
                        updateState { copy(showStationSelectionDialog = true) }
                    }

                    Settings -> {

                    }
                }
            }

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
        }
    }

    private fun createFooterText(fetchTime: Instant, timeUntilNextFetch: Duration): String {
        val formattedFetchTime =
            WidgetDataFormatter.formatTimeWithSeconds(fetchTime)
        // could be localized better, but this works for en and es
        return getString(
            strings.update_footer_text,
            formattedFetchTime,
            "${timeUntilNextFetch.inWholeSeconds}s"
        )
    }

    private inline fun <T> updateStateOnEach(flow: Flow<T>, crossinline block: State.(T) -> State) {
        flow.onEach { updateState { block(it) } }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    private fun updateState(operation: State.() -> State) {
        _state.value = operation(state.value)
    }

    private fun fetchData(force: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            updateState {
                copy(
                    isLoading = true,
                    hasError = false,
                    updateFooterText = null,
                )
            }
            withTimeoutOrNull(5000) {
                suspendCancellableCoroutine<Boolean> { continuation ->
                    WidgetDataFetcher.fetchWidgetData(
                        limit = Int.MAX_VALUE,
                        stations = state.value.selectedStations,
                        sort = state.value.stationSort,
                        filter = SettingsManager.trainFilter.value,
                        force = force,
                        onSuccess = {
                            updateState {
                                copy(
                                    isLoading = false,
                                    hasError = false,
                                    data = it.toDepartureBoardData().adjustedForLatestSettings()
                                )
                            }
                            latestWidgetData.value = it
                            lastFetchTime.value = Clock.System.now()
                            storedLastFetchTime = lastFetchTime.value?.toEpochMilliseconds()
                            continuation.resume(true)
                        },
                        onFailure = {
                            updateState {
                                copy(
                                    isLoading = false,
                                    hasError = true,
                                    data = it?.toDepartureBoardData()?.adjustedForLatestSettings()
                                )
                            }
                            latestWidgetData.value = it
                            continuation.resume(false)
                        }
                    )
                }
            } ?: run {
                updateState {
                    copy(
                        isLoading = false,
                        hasError = true,
                    )
                }
            }
        }
    }

    private fun DepartureBoardData.adjustedForLatestSettings(): DepartureBoardData {
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
                            .filter(StationLimitFilter(SettingsManager.stationLimit.value))
                            .map { train ->
                                train.copy(
                                    displayText = trainDisplayTime(
                                        SettingsManager.timeDisplay.value,
                                        train.isDelayed,
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
        private val IsFirstLaunchKey = BooleanPreferencesKey("is_first_launch")
        private val LatestWidgetDataKey = StringPreferencesKey("latest_widget_data")
        private val LastFetchKey = LongPreferencesKey("last_fetch")

        fun WidgetData.toDepartureBoardData(timeDisplay: TimeDisplay = SettingsManager.timeDisplay.value): DepartureBoardData {
            val stations = stations.mapNotNull { data ->
                val station =
                    Stations.All.firstOrNull { it.pathApiName == data.id }
                        ?: return@mapNotNull null
                val stationData = StationData(
                    station = station,
                    trains = data.trains
                        .filter { it.projectedArrival >= Clock.System.now() - 1.minutes }
                        .map { train ->
                            TrainData(
                                id = train.id,
                                title = train.title,
                                colors = train.colors.map { it.color },
                                displayText = trainDisplayTime(
                                    timeDisplay,
                                    train.isDelayed,
                                    train.projectedArrival
                                ),
                                projectedArrival = train.projectedArrival,
                                isDelayed = train.isDelayed,
                                backfill = train.backfillSource?.let {
                                    HomeBackfillSource(
                                        it,
                                        trainDisplayTime(
                                            timeDisplay,
                                            train.isDelayed,
                                            it.projectedArrival
                                        )
                                    )
                                },
                            )
                        }
                )
                stationData
            }
            return DepartureBoardData(stations = stations)
        }

        private fun trainDisplayTime(
            timeDisplay: TimeDisplay,
            isDelayed: Boolean,
            projectedArrival: Instant
        ): String {
            return (if (isDelayed) "Delay - " else "") + when (timeDisplay) {
                TimeDisplay.Relative -> WidgetDataFormatter.formatRelativeTime(
                    Clock.System.now(),
                    projectedArrival
                )

                TimeDisplay.Clock -> WidgetDataFormatter.formatTime(projectedArrival)
            }
        }

        private fun getStoredWidgetData(): WidgetData? {
            val raw = Preferences()[LatestWidgetDataKey] ?: return null
            return try {
                Json.decodeFromString(raw)
            } catch (e: SerializationException) {
                Logging.e("Failed to deseralize widget data", e)
                null
            }
        }

        private fun storeWidgetData(data: WidgetData?) {
            val raw = try {
                Json.encodeToString(data)
            } catch (e: SerializationException) {
                Logging.e("Failed to serialize widget data", e)
                return
            }
            Preferences()[LatestWidgetDataKey] = raw
        }
    }
}