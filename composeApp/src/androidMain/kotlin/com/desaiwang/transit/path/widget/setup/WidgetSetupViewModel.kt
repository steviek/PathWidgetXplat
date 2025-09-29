package com.desaiwang.transit.path.widget.setup

import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desaiwang.transit.path.MobilePathApplication
import com.desaiwang.transit.path.api.State.NewJersey
import com.desaiwang.transit.path.api.State.NewYork
import com.desaiwang.transit.path.api.StationSort
import com.desaiwang.transit.path.api.Stations
import com.desaiwang.transit.path.api.TrainFilter
import com.desaiwang.transit.path.api.state
import com.desaiwang.transit.path.location.AndroidLocationProvider
import com.desaiwang.transit.path.location.LocationPermissionRequestResult.Denied
import com.desaiwang.transit.path.location.LocationPermissionRequestResult.Granted
import com.desaiwang.transit.path.util.suspendRunCatching
import com.desaiwang.transit.path.util.withElementPresent
import com.desaiwang.transit.path.widget.DepartureBoardWidget
import com.desaiwang.transit.path.widget.StationByDisplayNameComparator
import com.desaiwang.transit.path.widget.configuration.WidgetConfigurationManager
import com.desaiwang.transit.path.widget.setup.WidgetSetupScreenContract.Effect
import com.desaiwang.transit.path.widget.setup.WidgetSetupScreenContract.Effect.CompleteConfigurationIntent
import com.desaiwang.transit.path.widget.setup.WidgetSetupScreenContract.Intent
import com.desaiwang.transit.path.widget.setup.WidgetSetupScreenContract.Intent.ConfirmClicked
import com.desaiwang.transit.path.widget.setup.WidgetSetupScreenContract.Intent.LineToggled
import com.desaiwang.transit.path.widget.setup.WidgetSetupScreenContract.Intent.SortOrderSelected
import com.desaiwang.transit.path.widget.setup.WidgetSetupScreenContract.Intent.StationToggled
import com.desaiwang.transit.path.widget.setup.WidgetSetupScreenContract.Intent.TrainFilterSelected
import com.desaiwang.transit.path.widget.setup.WidgetSetupScreenContract.Intent.UseClosestStationToggled
import com.desaiwang.transit.path.widget.setup.WidgetSetupScreenContract.State
import com.desaiwang.transit.path.widget.setup.WidgetSetupScreenContract.StationRow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class WidgetSetupViewModel : ViewModel() {

    private val context = MobilePathApplication.instance
    private val appWidgetManager get() = GlanceAppWidgetManager(context)

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private val _effects = Channel<Effect>()
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            state
                .map { it.appWidgetId }
                .filterNot { it == INVALID_APPWIDGET_ID }
                .distinctUntilChanged()
                .collectLatest { appWidgetId ->
                    val glanceId = suspendRunCatching {
                        appWidgetManager.getGlanceIdBy(appWidgetId)
                    }.getOrElse { return@collectLatest }
                    val storedData = WidgetConfigurationManager.getWidgetConfiguration(glanceId)
                    if (storedData != null) {
                        val selectedStations = storedData.fixedStations.orEmpty()
                        setState {
                            copy(
                                useClosestStation = storedData.useClosestStation,
                                sortOrder = storedData.sortOrder ?: StationSort.Alphabetical,
                                filter = storedData.filter ?: TrainFilter.All,
                                lines = storedData.lines,
                                njStations = Stations.All
                                    .filter { it.state == NewJersey }
                                    .sortedWith(StationByDisplayNameComparator)
                                    .map { station ->
                                        StationRow(
                                            id = station.pathApiName,
                                            displayName = station.displayName,
                                            checked = station.pathApiName in selectedStations
                                        )
                                    },
                                nyStations = Stations.All
                                    .filter { it.state == NewYork }
                                    .sortedWith(StationByDisplayNameComparator)
                                    .map { station ->
                                        StationRow(
                                            id = station.pathApiName,
                                            displayName = station.displayName,
                                            checked = station.pathApiName in selectedStations
                                        )
                                    }
                            )
                        }
                    }
                }
        }

        viewModelScope.launch {
            AndroidLocationProvider.locationPermissionResults
                .collectLatest { result ->
                    when (result) {
                        Denied -> {
                            setState {
                                copy(useClosestStation = false)
                            }
                        }

                        Granted -> {}
                    }
                }
        }
    }

    fun setAppWidgetId(appWidgetId: Int) {
        setState {
            copy(appWidgetId = appWidgetId)
        }
    }

    fun onIntent(intent: Intent) {
        when (intent) {
            is UseClosestStationToggled -> {
                setState {
                    copy(useClosestStation = intent.checked)
                }

                if (intent.checked && !AndroidLocationProvider.hasLocationPermission()) {
                    AndroidLocationProvider.requestLocationPermission()
                }
            }

            is StationToggled -> {
                setState {
                    copy(
                        njStations = njStations.map {
                            if (it.id == intent.id) {
                                it.copy(checked = intent.checked)
                            } else {
                                it
                            }
                        },
                        nyStations = nyStations.map {
                            if (it.id == intent.id) {
                                it.copy(checked = intent.checked)
                            } else {
                                it
                            }
                        },
                    )
                }
            }

            is SortOrderSelected -> {
                setState { copy(sortOrder = intent.sortOrder) }
            }

            is TrainFilterSelected -> {
                setState { copy(filter = intent.filter) }
            }

            is LineToggled -> {
                setState { copy(lines = lines.withElementPresent(intent.line, intent.checked)) }
            }

            ConfirmClicked -> {
                viewModelScope.launch {
                    val currentState = state.value
                    val glanceId = appWidgetManager.getGlanceIdBy(currentState.appWidgetId)
                    val selectedStations =
                        currentState.njStations.filter { it.checked }.map { it.id } +
                                currentState.nyStations.filter { it.checked }.map { it.id }
                    WidgetConfigurationManager.setWidgetConfiguration(
                        glanceId,
                        selectedStations,
                        currentState.lines,
                        currentState.useClosestStation,
                        currentState.sortOrder,
                        currentState.filter,
                    )
                    DepartureBoardWidget().updateAll(context)
                    sendEffect(CompleteConfigurationIntent(currentState.appWidgetId))
                }
            }
        }

    }

    private fun setState(block: State.() -> State) {
        _state.value = block(_state.value)
    }

    private fun sendEffect(effect: Effect) {
        viewModelScope.launch {
            _effects.send(effect)
        }
    }
}
