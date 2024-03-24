package com.sixbynine.transit.path.app.ui.settings

import com.sixbynine.transit.path.analytics.Analytics
import com.sixbynine.transit.path.api.LocationSetting.Disabled
import com.sixbynine.transit.path.api.LocationSetting.Enabled
import com.sixbynine.transit.path.api.LocationSetting.EnabledPendingPermission
import com.sixbynine.transit.path.app.external.ExternalRoutingManager
import com.sixbynine.transit.path.app.external.shareAppToSystem
import com.sixbynine.transit.path.app.settings.SettingsManager
import com.sixbynine.transit.path.app.ui.BaseViewModel
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.BottomSheetType.Lines
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.BottomSheetType.StationLimit
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.BottomSheetType.StationSort
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.BottomSheetType.TimeDisplay
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.BottomSheetType.TrainFilter
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Effect
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Effect.GoBack
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.BackClicked
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.BottomSheetDismissed
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.BuyMeACoffeeClicked
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.LineFilterToggled
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.LinesClicked
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.LocationSettingChanged
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.RateAppClicked
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.SendFeedbackClicked
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.ShareAppClicked
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.ShowPresumedTrainsChanged
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.StationLimitClicked
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.StationLimitSelected
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.StationSortClicked
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.StationSortSelected
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.TimeDisplayChanged
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.TimeDisplayClicked
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.TrainFilterChanged
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.TrainFilterClicked
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.LocationSettingState
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.State
import com.sixbynine.transit.path.location.LocationPermissionRequestResult
import com.sixbynine.transit.path.location.LocationProvider
import com.sixbynine.transit.path.util.withElementPresent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SettingsViewModel : BaseViewModel<State, Intent, Effect>(createInitialState()) {
    init {
        updateStateOnEach(SettingsManager.timeDisplay) { copy(timeDisplay = it) }
        updateStateOnEach(SettingsManager.trainFilter) { copy(trainFilter = it) }
        updateStateOnEach(SettingsManager.lineFilter) { copy(lines = it) }
        updateStateOnEach(SettingsManager.stationLimit) { copy(stationLimit = it) }
        updateStateOnEach(SettingsManager.stationSort) { copy(stationSort = it) }
        updateStateOnEach(SettingsManager.displayPresumedTrains) { copy(showPresumedTrains = it) }
        if (LocationProvider().isLocationSupportedByDevice) {
            updateStateOnEach(SettingsManager.locationSetting) {
                copy(
                    locationSetting = when (it) {
                        Enabled, EnabledPendingPermission -> LocationSettingState.Enabled
                        Disabled -> LocationSettingState.Disabled
                    }
                )
            }
        }

        if (LocationProvider().isLocationSupportedByDevice) {
            updateStateOnEach(LocationProvider().locationPermissionResults) {
                copy(hasLocationPermission = it is LocationPermissionRequestResult.Granted)
            }
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

            is TrainFilterChanged -> {
                SettingsManager.updateTrainFilter(intent.filter)
                updateState { copy(bottomSheet = null) }
            }

            is LineFilterToggled -> {
                val newLineFilters = state.value.lines.withElementPresent(intent.filter, intent.isChecked)
                SettingsManager.updateLineFilters(newLineFilters)
            }

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
                updateState { copy(bottomSheet = null) }
            }

            is LocationSettingChanged -> {
                SettingsManager.updateLocationSetting(intent.use)
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

            LinesClicked -> {
                updateState { copy(bottomSheet = Lines) }
            }

            RateAppClicked -> {
                Analytics.rateAppClicked()
                viewModelScope.launch { ExternalRoutingManager().launchAppRating() }
            }

            SendFeedbackClicked -> {
                viewModelScope.launch {
                    ExternalRoutingManager().openEmail()
                }
            }

            ShareAppClicked -> {
                Analytics.shareAppClicked()
                ExternalRoutingManager().shareAppToSystem()
            }

            BuyMeACoffeeClicked -> {
                Analytics.buyMeACoffeeClicked()
                viewModelScope.launch {
                    ExternalRoutingManager().openUrl("https://www.buymeacoffee.com/kideckel")
                }
            }
        }
    }

    private companion object {
        fun createInitialState(): State {
            val locationSetting = when {
                LocationProvider().isLocationSupportedByDevice -> {
                    when (SettingsManager.locationSetting.value) {
                        Enabled, EnabledPendingPermission -> LocationSettingState.Enabled
                        Disabled -> LocationSettingState.Disabled
                    }
                }

                else -> LocationSettingState.NotAvailable
            }
            return State(
                locationSetting = locationSetting,
                timeDisplay = SettingsManager.timeDisplay.value,
                trainFilter = SettingsManager.trainFilter.value,
                lines = SettingsManager.lineFilter.value,
                stationLimit = SettingsManager.stationLimit.value,
                stationSort = SettingsManager.stationSort.value,
                showPresumedTrains = SettingsManager.displayPresumedTrains.value,
                hasLocationPermission = LocationProvider().hasLocationPermission(),
            )
        }
    }
}
