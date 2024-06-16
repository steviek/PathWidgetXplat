package com.sixbynine.transit.path.app.ui.home

import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.api.LocationSetting.Disabled
import com.sixbynine.transit.path.api.LocationSetting.Enabled
import com.sixbynine.transit.path.api.LocationSetting.EnabledPendingPermission
import com.sixbynine.transit.path.app.lifecycle.AppLifecycleObserver
import com.sixbynine.transit.path.app.settings.SettingsManager
import com.sixbynine.transit.path.app.station.StationSelectionManager
import com.sixbynine.transit.path.time.now
import com.sixbynine.transit.path.util.FetchWithPrevious
import com.sixbynine.transit.path.util.Staleness
import com.sixbynine.transit.path.util.awaitTrue
import com.sixbynine.transit.path.util.collect
import com.sixbynine.transit.path.util.collectLatest
import com.sixbynine.transit.path.util.isFailure
import com.sixbynine.transit.path.widget.WidgetData
import com.sixbynine.transit.path.widget.WidgetDataFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Encapsulates the logic for what the latest fetched [WidgetData] is and when we should fetch
 * again.
 */
class WidgetDataFetchingUseCase(private val scope: CoroutineScope) {

    private val initialFetch = startFetch(staleness = UnforcedStaleness)

    private val _fetchData = MutableStateFlow(createInitialFetchData(initialFetch))
    val fetchData = _fetchData.asStateFlow()

    init {
        // Observe updates to data that we should refresh data for.
        StationSelectionManager
            .selection
            .drop(1)
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .collect(scope) {
                fetchData(force = false)
            }

        SettingsManager
            .avoidMissingTrains
            .drop(1)
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .collect(scope) {
                fetchData(force = false)
            }

        SettingsManager
            .locationSetting
            .drop(1)
            .distinctUntilChanged()
            .filter { it == Enabled }
            .flowOn(Dispatchers.Default)
            .collect(scope) {
                Logging.d("Fetching widget data, force for location")
                // Force a refresh whenever location is enabled (with permission).
                fetchData(force = true)
            }

        fetchData.collectLatest(scope) {
            if (it.isFetching || it.hasError) return@collectLatest

            // Make sure the UI is visible before automatically fetching more data.
            AppLifecycleObserver.isActive.awaitTrue()

            // Wait until the next fetch time.
            delay(it.nextFetchTime - now())

            // Fetch again.
            fetchData()
        }

        // Handle initial fetch when data is not already present.
        if (fetchData.value.isFetching) {
            fetchData()
        } else if (shouldFetchForLocationSettingChange()) {
            Logging.d("Fetching widget data, force for location2")
            fetchData(force = true)
        }
    }

    fun fetchNow() {
        Logging.d("Fetching widget data, force for fetchNow")
        fetchData(force = true)
    }

    private fun shouldFetchForLocationSettingChange(): Boolean {
        val closestStationId = fetchData.value.data?.closestStationId
        return when (SettingsManager.locationSetting.value) {
            Enabled -> closestStationId == null
            Disabled -> false
            EnabledPendingPermission -> false
        }
    }

    private fun fetchData(force: Boolean = false) = scope.launch(Dispatchers.Default) {
        _fetchData.value = _fetchData.value.copy(isFetching = true, hasError = false)

        val result = coroutineScope {
            if (force) {
                // This is a bit silly, but it feels really unsatisfying to click
                // 'update now' and not see any sort of loading progress, so make this take
                // at least half a second.
                delay(500)
            }

            startFetch(if (force) ForcedStaleness else UnforcedStaleness).fetch.await()
        }
        _fetchData.value = FetchData(
            lastFetchTime = now(),
            nextFetchTime = now() + FetchInterval,
            data = result.data,
            hasError = result.isFailure(),
            isFetching = false
        )
    }

    private fun startFetch(staleness: Staleness): FetchWithPrevious<WidgetData> {
        return WidgetDataFetcher.fetchWidgetDataWithPrevious(
            limit = Int.MAX_VALUE,
            stations = StationSelectionManager.selection.value.selectedStations,
            sort = SettingsManager.stationSort.value,
            lines = SettingsManager.lineFilter.value,
            filter = SettingsManager.trainFilter.value,
            includeClosestStation = SettingsManager.locationSetting.value == Enabled,
            staleness = staleness,
        )
    }

    data class FetchData(
        val lastFetchTime: Instant?,
        val nextFetchTime: Instant,
        val data: WidgetData?,
        val hasError: Boolean,
        val isFetching: Boolean
    ) {
        val timeUntilNextFetch: Duration
            get() = (nextFetchTime - now()).coerceAtLeast(Duration.ZERO)
    }

    private companion object {
        private val FetchInvalidAfter = 6.hours
        val ForcedStaleness = Staleness(staleAfter = 10.seconds, invalidAfter = FetchInvalidAfter)
        val UnforcedStaleness = Staleness(staleAfter = 30.seconds, invalidAfter = FetchInvalidAfter)
        val FetchInterval = 1.minutes

        fun createInitialFetchData(data: FetchWithPrevious<WidgetData>): FetchData {
            val lastFetch = data.previous
            val lastFetchAge = lastFetch?.age
            val lastFetchData = lastFetch?.value
            val now = now()
            if (lastFetchAge == null || lastFetchAge >= FetchInvalidAfter) {
                return FetchData(
                    lastFetchTime = null,
                    nextFetchTime = now,
                    data = null,
                    hasError = false,
                    isFetching = true,
                )
            }
            val nextFetchTime = now + FetchInterval - lastFetchAge
            val isFetching = nextFetchTime <= now
            return FetchData(
                lastFetchTime = now - lastFetchAge,
                nextFetchTime = nextFetchTime,
                data = lastFetchData,
                hasError = false,
                isFetching = isFetching
            )
        }
    }
}