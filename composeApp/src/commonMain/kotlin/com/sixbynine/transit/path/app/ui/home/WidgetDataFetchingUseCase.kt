package com.sixbynine.transit.path.app.ui.home

import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.LocationSetting.Disabled
import com.sixbynine.transit.path.api.LocationSetting.Enabled
import com.sixbynine.transit.path.api.LocationSetting.EnabledPendingPermission
import com.sixbynine.transit.path.api.TrainFilter
import com.sixbynine.transit.path.app.lifecycle.AppLifecycleObserver
import com.sixbynine.transit.path.app.settings.SettingsManager
import com.sixbynine.transit.path.app.station.StationSelectionManager
import com.sixbynine.transit.path.preferences.BooleanPreferencesKey
import com.sixbynine.transit.path.preferences.LongPreferencesKey
import com.sixbynine.transit.path.preferences.Preferences
import com.sixbynine.transit.path.preferences.StringPreferencesKey
import com.sixbynine.transit.path.preferences.persisting
import com.sixbynine.transit.path.time.now
import com.sixbynine.transit.path.util.DataResult
import com.sixbynine.transit.path.util.JsonFormat
import com.sixbynine.transit.path.util.awaitTrue
import com.sixbynine.transit.path.util.collect
import com.sixbynine.transit.path.util.collectLatest
import com.sixbynine.transit.path.widget.WidgetData
import com.sixbynine.transit.path.widget.WidgetDataFetcher
import com.sixbynine.transit.path.widget.fetchWidgetDataSuspending
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
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Instant
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Encapsulates the logic for what the latest fetched [WidgetData] is and when we should fetch
 * again.
 */
class WidgetDataFetchingUseCase(private val scope: CoroutineScope) {

    private val _fetchData = MutableStateFlow(createInitialFetchData())
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
            .locationSetting
            .drop(1)
            .distinctUntilChanged()
            .filter { it == Enabled }
            .flowOn(Dispatchers.Default)
            .collect(scope) {
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
            fetchData(force = true)
        }
    }

    fun fetchNow() {
        fetchData(force = true)
    }

    private fun shouldFetchForLocationSettingChange(): Boolean {
        val closestStationId = fetchData.value.data?.closestStationId
        return when (SettingsManager.locationSetting.value) {
            Enabled -> closestStationId == null
            Disabled -> closestStationId != null
            EnabledPendingPermission -> false
        }
    }

    private fun fetchData(force: Boolean = false) {
        scope.launch(Dispatchers.Default) {
            _fetchData.value = _fetchData.value.copy(isFetching = true, hasError = false)

            fun completeFetch(data: WidgetData?, error: Boolean) {
                val now = now()
                storeWidgetData(data)
                hadError = false
                lastFetchTime = now

                _fetchData.value = FetchData(
                    lastFetchTime = now,
                    nextFetchTime = now + FetchInterval,
                    data = data,
                    hasError = error,
                    isFetching = false
                )
            }

            withTimeoutOrNull(5000) {
                val result = coroutineScope {
                    if (force) {
                        // This is a bit silly, but it feels really unsatisfying to click
                        // 'update now' and not see any sort of loading progress, so make this take
                        // at least half a second.
                        launch { delay(500) }
                    }

                    WidgetDataFetcher.fetchWidgetDataSuspending(
                        limit = Int.MAX_VALUE,
                        stations = StationSelectionManager.selection.value.selectedStations,
                        sort = SettingsManager.stationSort.value,
                        lines = Line.entries, // filtering happens downstream
                        filter = TrainFilter.All, // filtering happens downstream
                        force = force,
                        includeClosestStation = SettingsManager.locationSetting.value == Enabled
                    )
                }
                completeFetch(result.data, error = result is DataResult.Failure)
            } ?: run { completeFetch(_fetchData.value.data, error = true)}
        }
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
        val LastFetchKey = LongPreferencesKey("last_fetch")
        val LatestWidgetDataKey = StringPreferencesKey("latest_widget_data")
        val HadError = BooleanPreferencesKey("had_error")
        val FetchInterval = 1.minutes

        var storedLastFetchTime by persisting(LastFetchKey)
        var lastFetchTime: Instant?
            get() = storedLastFetchTime?.let { Instant.fromEpochMilliseconds(it) }
            set(value) {
                storedLastFetchTime = value?.toEpochMilliseconds()
            }

        var hadError by persisting(HadError)

        fun createInitialFetchData(): FetchData {
            val lastFetchTime = lastFetchTime
            val now = now()
            if (lastFetchTime == null || lastFetchTime < now - 10.minutes) {
                return FetchData(
                    lastFetchTime = null,
                    nextFetchTime = now,
                    data = null,
                    hasError = false,
                    isFetching = true,
                )
            }
            val nextFetchTime = lastFetchTime + FetchInterval
            val data = getStoredWidgetData()
            val isFetching = nextFetchTime <= now
            return FetchData(
                lastFetchTime,
                nextFetchTime,
                data,
                hasError = !isFetching && hadError == true,
                isFetching = isFetching
            )
        }

        fun getStoredWidgetData(): WidgetData? {
            val raw = Preferences()[LatestWidgetDataKey] ?: return null
            return try {
                JsonFormat.decodeFromString(raw)
            } catch (e: SerializationException) {
                Logging.e("Failed to deseralize widget data", e)
                null
            }
        }

        fun storeWidgetData(data: WidgetData?) {
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