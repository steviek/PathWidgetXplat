package com.desaiwang.transit.path.app.ui.home

import com.desaiwang.transit.path.Logging
import com.desaiwang.transit.path.api.Line
import com.desaiwang.transit.path.api.LocationSetting.Disabled
import com.desaiwang.transit.path.api.LocationSetting.Enabled
import com.desaiwang.transit.path.api.LocationSetting.EnabledPendingPermission
import com.desaiwang.transit.path.api.PathApiException
import com.desaiwang.transit.path.api.Stations
import com.desaiwang.transit.path.api.TrainFilter
import com.desaiwang.transit.path.app.lifecycle.AppLifecycleObserver
import com.desaiwang.transit.path.app.settings.SettingsManager
import com.desaiwang.transit.path.app.station.StationSelectionManager
import com.desaiwang.transit.path.time.now
import com.desaiwang.transit.path.util.FetchWithPrevious
import com.desaiwang.transit.path.util.Staleness
import com.desaiwang.transit.path.util.awaitTrue
import com.desaiwang.transit.path.util.collect
import com.desaiwang.transit.path.util.collectIn
import com.desaiwang.transit.path.util.collectLatest
import com.desaiwang.transit.path.util.isFailure
import com.desaiwang.transit.path.model.DepartureBoardData
import com.desaiwang.transit.path.widget.WidgetDataFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
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
 * Encapsulates the logic for what the latest fetched [DepartureBoardData] is and when we should fetch
 * again.
 */
class WidgetDataFetchingUseCase private constructor() {

    private val subscribers = mutableSetOf<Any>()
    private val scope = CoroutineScope(Dispatchers.Default)

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

        WidgetDataFetcher.nonFetchLocationReceived.collectIn(scope) {
            fetchData(force = false)
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
            fetchData(force = true)
        }
    }

    fun fetchNow() {
        fetchData(force = true)
    }

    fun unsubscribe(subscriber: Any) {
        subscribers.remove(subscriber)
        if (subscribers.isEmpty()) {
            scope.cancel()
            instance = null
        }
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
            lastFetchTime = result.data?.fetchTime,
            nextFetchTime = now() + FetchInterval,
            data = result.data,
            hasError = result.isFailure(),
            hadInternet = !result.isFailure() || result.hadInternet,
            isPathApiBusted = (result.isFailure() && result.error is PathApiException) ||
                    result.data?.isPathApiBroken == true,
            isFetching = false,
            scheduleName = result.data?.scheduleName,
        )
    }

    private fun startFetch(staleness: Staleness): FetchWithPrevious<DepartureBoardData> {
        return WidgetDataFetcher.fetchWidgetDataWithPrevious(
            stationLimit = Int.MAX_VALUE,
            stations = Stations.All,
            sort = SettingsManager.stationSort.value,
            lines = Line.entries,
            filter = TrainFilter.All,
            includeClosestStation = SettingsManager.locationSetting.value == Enabled,
            staleness = staleness,
        )
    }

    data class FetchData(
        val lastFetchTime: Instant?,
        val nextFetchTime: Instant,
        val data: DepartureBoardData?,
        val hasError: Boolean,
        val hadInternet: Boolean,
        val isPathApiBusted: Boolean,
        val scheduleName: String?,
        val isFetching: Boolean
    ) {
        val timeUntilNextFetch: Duration
            get() = (nextFetchTime - now()).coerceAtLeast(Duration.ZERO)
    }

    companion object {
        private val FetchInvalidAfter = 6.hours
        private val ForcedStaleness = Staleness(staleAfter = 10.seconds, invalidAfter = FetchInvalidAfter)
        private val UnforcedStaleness = Staleness(staleAfter = 30.seconds, invalidAfter = FetchInvalidAfter)
        private val FetchInterval = 1.minutes

        private fun createInitialFetchData(data: FetchWithPrevious<DepartureBoardData>): FetchData {
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
                    hadInternet = true,
                    isPathApiBusted = false,
                    isFetching = true,
                    scheduleName = null,
                )
            }
            val nextFetchTime = now + FetchInterval - lastFetchAge
            val isFetching = nextFetchTime <= now
            return FetchData(
                lastFetchTime = now - lastFetchAge,
                nextFetchTime = nextFetchTime,
                data = lastFetchData,
                hasError = false,
                hadInternet = true,
                isPathApiBusted = false,
                isFetching = isFetching,
                scheduleName = null,
            )
        }

        private var instance: WidgetDataFetchingUseCase? = null

        fun get(subscriber: Any): WidgetDataFetchingUseCase {
            val useCase = instance ?: WidgetDataFetchingUseCase()
            useCase.subscribers += subscriber
            instance = useCase
            return useCase
        }
    }
}