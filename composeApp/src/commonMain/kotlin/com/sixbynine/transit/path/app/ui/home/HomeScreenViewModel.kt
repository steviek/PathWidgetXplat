package com.sixbynine.transit.path.app.ui.home

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.MR.strings
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.StationSort.Alphabetical
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.isInNewJersey
import com.sixbynine.transit.path.api.isInNewYork
import com.sixbynine.transit.path.app.filter.StationFilterManager
import com.sixbynine.transit.path.app.lifecycle.AppLifecycleObserver
import com.sixbynine.transit.path.app.station.StationSelectionManager
import com.sixbynine.transit.path.app.ui.home.ConfigurationItem.Settings
import com.sixbynine.transit.path.app.ui.home.ConfigurationItem.Station
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.AddStationClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.ConfigurationChipClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.EditClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.MoveStationDownClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.MoveStationUpClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.RemoveStationClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.RetryClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.SettingsBottomSheetDismissed
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.SettingsClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.SettingsFilterChanged
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.SettingsSortChanged
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.SettingsTimeDisplayChanged
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.StationBottomSheetDismissed
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.StationBottomSheetSelection
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.StationFilterDialogDismissed
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.StationSelectionDialogDismissed
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.StopEditingClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.UpdateNowClicked
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.State
import com.sixbynine.transit.path.app.ui.layout.LayoutOption.OneColumn
import com.sixbynine.transit.path.app.ui.layout.LayoutOption.ThreeColumns
import com.sixbynine.transit.path.app.ui.layout.LayoutOption.TwoColumns
import com.sixbynine.transit.path.app.ui.layout.LayoutOptionManager
import com.sixbynine.transit.path.preferences.BooleanPreferencesKey
import com.sixbynine.transit.path.preferences.IntPreferencesKey
import com.sixbynine.transit.path.preferences.persisting
import com.sixbynine.transit.path.resources.getString
import com.sixbynine.transit.path.widget.WidgetData
import com.sixbynine.transit.path.widget.WidgetDataFetcher
import com.sixbynine.transit.path.widget.WidgetDataFormatter
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class HomeScreenViewModel(maxWidth: Dp, maxHeight: Dp) : ViewModel() {

    private var hasLaunchedBefore by persisting(IsFirstLaunchKey)
    private var timeDisplayNumber by persisting(TimeDisplayOption)
    private var stationSortNumber by persisting(SortOptionKey)
    private var lastFetchTime = MutableStateFlow<Instant?>(null)

    private val _state = MutableStateFlow(
        State(
            isTablet = maxWidth >= 480.dp && maxHeight >= 480.dp,
            selectedStations = StationSelectionManager.selection.value.selectedStations,
            unselectedStations = StationSelectionManager.selection.value.unselectedStations,
            layoutOption = LayoutOptionManager.layoutOption ?: defaultLayout(maxWidth),
            useColumnForFooter = maxWidth < 480.dp,
            isEditing = hasLaunchedBefore != true,
            stationFilter = StationFilterManager.filter.value,
            timeDisplay = TimeDisplay.entries.firstOrNull { it.number == timeDisplayNumber }
                ?: TimeDisplay.Relative,
            stationSort = StationSort.entries.firstOrNull { it.number == stationSortNumber }
                ?: StationSort.Alphabetical,
        )
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(StationSelectionManager.selection, StationFilterManager.filter, ::Pair)
                .collectLatest { (selection, filter) ->
                    updateState {
                        copy(
                            selectedStations = selection.selectedStations,
                            unselectedStations = selection.unselectedStations,
                            stationFilter = filter,
                        )
                    }
                    updateState {
                        copy(data = data?.sorted())
                    }
                    fetchData(force = false)
                }
        }

        viewModelScope.launch {
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
                        val formattedFetchTime =
                            WidgetDataFormatter.formatTimeWithSeconds(fetchTime)
                        // could be localized better, but this works for en and es
                        val footerText =
                            getString(
                                strings.update_footer_text,
                                formattedFetchTime,
                                "${timeUntilNextFetch.inWholeSeconds}s"
                            )
                        updateState {
                            copy(
                                updateFooterText = footerText
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
    }

    fun onIntent(intent: Intent) {
        when (intent) {
            RetryClicked -> fetchData(force = true)
            UpdateNowClicked -> fetchData(force = true)
            EditClicked -> updateState { copy(isEditing = true) }
            StopEditingClicked -> updateState { copy(isEditing = false) }
            SettingsClicked -> updateState { copy(showSettingsBottomSheet = true) }
            is ConfigurationChipClicked -> {
                when (intent.item) {
                    Station -> {
                        updateState { copy(showStationSelectionDialog = true) }
                    }

                    Settings -> {
                        updateState { copy(showSettingsBottomSheet = true) }
                    }
                }
            }

            is StationSelectionDialogDismissed -> {
                updateState { copy(showStationSelectionDialog = false) }
                StationSelectionManager.updateSelection(intent.state)
            }

            StationFilterDialogDismissed -> {
                updateState { copy(showFilterDialog = false) }
            }

            SettingsBottomSheetDismissed -> {
                updateState { copy(showSettingsBottomSheet = false) }
            }

            is SettingsTimeDisplayChanged -> {
                timeDisplayNumber = intent.timeDisplay.number
                updateState { copy(timeDisplay = intent.timeDisplay) }
            }

            is SettingsFilterChanged -> {
                StationFilterManager.update(intent.filter)
            }

            is SettingsSortChanged -> {
                stationSortNumber = intent.sort.number
                updateState { copy(stationSort = intent.sort) }
                updateState { copy(data = data?.sorted()) }
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
        }
    }

    fun onConstraintsChanged(maxWidth: Dp, maxHeight: Dp) {
        updateState {
            copy(
                isTablet = maxWidth >= 480.dp && maxHeight >= 480.dp,
                layoutOption = LayoutOptionManager.layoutOption ?: defaultLayout(maxWidth),
            )
        }
    }

    private fun updateState(operation: State.() -> State) {
        _state.value = operation(state.value)
    }

    private fun fetchData(force: Boolean) {
        viewModelScope.launch {
            updateState {
                copy(
                    isLoading = true,
                    hasError = false,
                    updateFooterText = null,
                )
            }
            WidgetDataFetcher.fetchWidgetData(
                limit = Int.MAX_VALUE,
                stations = state.value.selectedStations,
                sort = state.value.stationSort,
                filter = StationFilterManager.filter.value,
                force = force,
                onSuccess = {
                    updateState {
                        copy(
                            isLoading = false,
                            hasError = false,
                            data = it.sorted()
                        )
                    }
                    lastFetchTime.value = Clock.System.now()
                },
                onFailure = {
                    updateState {
                        copy(
                            isLoading = false,
                            hasError = true,
                            data = it?.sorted()
                        )
                    }
                }
            )
        }
    }

    private fun WidgetData.sorted(): WidgetData {
        val stationToIndex =
            state
                .value
                .selectedStations
                .mapIndexed { index, station -> station.pathApiName to index }
                .toMap()
        val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
        val isMorning = hour in 3 until 12
        return copy(
            stations = stations.sortedBy { stationToIndex[it.id] }
                .sortedBy {
                    val station = Stations.All.first { s -> s.pathApiName == it.id }
                    val isFirst = when (state.value.stationSort) {
                        StationSort.NjAm -> isMorning == station.isInNewJersey
                        StationSort.NyAm -> isMorning == station.isInNewYork
                        else -> return@sortedBy 0
                    }
                    if (isFirst) 0 else 1
                }
        )
    }

    private fun defaultLayout(maxWidth: Dp) = when (maxWidth) {
        in 0.dp..600.dp -> OneColumn
        in 600.dp..1200.dp -> TwoColumns
        else -> ThreeColumns
    }

    private companion object {
        val IsFirstLaunchKey = BooleanPreferencesKey("is_first_launch")
        val TimeDisplayOption = IntPreferencesKey("time_display_option")
        val SortOptionKey = IntPreferencesKey("sort_option")
    }
}