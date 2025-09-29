package com.desaiwang.transit.path.app.ui.settings

import com.desaiwang.transit.path.Logging
import com.desaiwang.transit.path.analytics.Analytics
import com.desaiwang.transit.path.api.LocationSetting.Disabled
import com.desaiwang.transit.path.api.LocationSetting.Enabled
import com.desaiwang.transit.path.api.LocationSetting.EnabledPendingPermission
import com.desaiwang.transit.path.app.external.ExternalRoutingManager
import com.desaiwang.transit.path.app.external.shareAppToSystem
import com.desaiwang.transit.path.app.settings.SettingsManager
import com.desaiwang.transit.path.app.settings.exportDevLogs
import com.desaiwang.transit.path.app.ui.BaseViewModel
import com.desaiwang.transit.path.app.ui.settings.SettingsContract.BottomSheetType.Lines
import com.desaiwang.transit.path.app.ui.settings.SettingsContract.BottomSheetType.StationSort
import com.desaiwang.transit.path.app.ui.settings.SettingsContract.BottomSheetType.TrainFilter
import com.desaiwang.transit.path.app.ui.settings.SettingsContract.Effect
import com.desaiwang.transit.path.app.ui.settings.SettingsContract.Effect.GoBack
import com.desaiwang.transit.path.app.ui.settings.SettingsContract.Effect.GoToAdvancedSettings
import com.desaiwang.transit.path.app.ui.settings.SettingsContract.Intent
import com.desaiwang.transit.path.app.ui.settings.SettingsContract.Intent.*
import com.desaiwang.transit.path.app.ui.settings.SettingsContract.LocationSettingState
import com.desaiwang.transit.path.app.ui.settings.SettingsContract.State
import com.desaiwang.transit.path.location.LocationPermissionRequestResult
import com.desaiwang.transit.path.location.LocationProvider
import com.desaiwang.transit.path.location.isLocationSupportedByDevice
import com.desaiwang.transit.path.time.now
import com.desaiwang.transit.path.util.withElementPresent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.seconds

class SettingsViewModel : BaseViewModel<State, Intent, Effect>(createInitialState()) {

    private var headerTapInfo: HeaderTapInfo? = null

    init {
        updateStateOnEach(SettingsManager.trainFilter) { copy(trainFilter = it) }
        updateStateOnEach(SettingsManager.lineFilter) { copy(lines = it) }
        updateStateOnEach(SettingsManager.stationSort) { copy(stationSort = it) }
        updateStateOnEach(SettingsManager.displayPresumedTrains) { copy(showPresumedTrains = it) }
        updateStateOnEach(SettingsManager.locationSetting) {
            copy(
                locationSetting = when (it) {
                    Enabled, EnabledPendingPermission -> LocationSettingState.Enabled
                    Disabled -> LocationSettingState.Disabled
                }
            )
        }

        updateStateOnEach(LocationProvider().locationPermissionResults) {
            copy(hasLocationPermission = it is LocationPermissionRequestResult.Granted)
        }

        updateStateOnEach(SettingsManager.devOptionsEnabled) { copy(devOptionsEnabled = it) }
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

            HeaderTapped -> {
                val prevTapInfo = headerTapInfo
                if (prevTapInfo != null && prevTapInfo.lastTapTime > now() - 1.seconds) {
                    headerTapInfo = HeaderTapInfo(prevTapInfo.count + 1, now())
                    if (prevTapInfo.count >= 5) {
                        SettingsManager.updateDevOptionsEnabled(true)
                    }
                } else {
                    headerTapInfo = HeaderTapInfo()
                }
            }

            DevOptionsClicked -> {
                viewModelScope.launch {
                    val logs = Logging.getLogRecords()
                    exportDevLogs(logs)
                }
            }
        }
    }

    private data class HeaderTapInfo(val count: Int = 1, val lastTapTime: Instant = now())

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
                devOptionsEnabled = SettingsManager.devOptionsEnabled.value,
            )
        }
    }
}
