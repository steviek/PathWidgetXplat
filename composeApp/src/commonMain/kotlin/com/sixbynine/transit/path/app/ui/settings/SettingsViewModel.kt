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
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.BottomSheetType.StationSort
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.BottomSheetType.TrainFilter
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Effect
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Effect.GoBack
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Effect.GoToAdvancedSettings
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.AdvancedSettingsClicked
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
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.StationSortClicked
import com.sixbynine.transit.path.app.ui.settings.SettingsContract.Intent.StationSortSelected
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

class SettingsViewModel : BaseViewModel<State, Intent, Effect>(createInitialState()) {
    init {
        updateStateOnEach(SettingsManager.trainFilter) { copy(trainFilter = it) }
        updateStateOnEach(SettingsManager.lineFilter) { copy(lines = it) }
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

    override val rateLimitedIntents: Set<Intent> = setOf(
        StationSortClicked,
        TrainFilterClicked,
        LinesClicked,
        BackClicked,
        SendFeedbackClicked,
        RateAppClicked,
        ShareAppClicked,
        BuyMeACoffeeClicked,
        AdvancedSettingsClicked
    )

    private inline fun <T> updateStateOnEach(flow: Flow<T>, crossinline block: State.(T) -> State) {
        flow.onEach { updateState { block(it) } }.launchIn(lightweightScope)
    }

    override suspend fun performIntent(intent: Intent) {
        when (intent) {

            is TrainFilterChanged -> {
                SettingsManager.updateTrainFilter(intent.filter)
                updateState { copy(bottomSheet = null) }
            }

            is LineFilterToggled -> {
                val newLineFilters =
                    state.value.lines.withElementPresent(intent.filter, intent.isChecked)
                SettingsManager.updateLineFilters(newLineFilters)
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

            StationSortClicked -> {
                updateState { copy(bottomSheet = StationSort) }
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
                ExternalRoutingManager().launchAppRating()
            }

            SendFeedbackClicked -> {
                ExternalRoutingManager().openEmail()
            }

            ShareAppClicked -> {
                Analytics.shareAppClicked()
                ExternalRoutingManager().shareAppToSystem()
            }

            BuyMeACoffeeClicked -> {
                Analytics.buyMeACoffeeClicked()
                ExternalRoutingManager().openUrl("https://www.buymeacoffee.com/kideckel")
            }

            AdvancedSettingsClicked -> {
                sendEffect(GoToAdvancedSettings)
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
                trainFilter = SettingsManager.trainFilter.value,
                lines = SettingsManager.lineFilter.value,
                stationSort = SettingsManager.stationSort.value,
                showPresumedTrains = SettingsManager.displayPresumedTrains.value,
                hasLocationPermission = LocationProvider().hasLocationPermission(),
            )
        }
    }
}
