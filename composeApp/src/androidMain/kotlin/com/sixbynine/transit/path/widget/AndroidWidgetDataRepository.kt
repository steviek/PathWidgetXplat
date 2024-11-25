package com.sixbynine.transit.path.widget

import android.annotation.SuppressLint
import android.content.Context
import com.sixbynine.transit.path.MobilePathApplication
import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.StationSort.Alphabetical
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.TrainFilter
import com.sixbynine.transit.path.util.DataResult
import com.sixbynine.transit.path.util.FetchWithPrevious
import com.sixbynine.transit.path.util.Staleness
import com.sixbynine.transit.path.util.isFailure
import com.sixbynine.transit.path.widget.configuration.WidgetConfigurationManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@SuppressLint("StaticFieldLeak") // application context
object AndroidWidgetDataRepository {

    private const val IsLoadingKey = "is_loading"
    private const val HasErrorKey = "has_error"
    private const val HadInternetKey = "had_internet"

    private val context: Context = MobilePathApplication.instance
    private val prefs = context.getSharedPreferences("widget_data_store", Context.MODE_PRIVATE)

    private var isLoading: Boolean
        get() = prefs.getBoolean(IsLoadingKey, true)
        set(value) {
            prefs.edit().putBoolean(IsLoadingKey, value).apply()
        }

    private var hasError: Boolean
        get() = prefs.getBoolean(HasErrorKey, false)
        set(value) {
            prefs.edit().putBoolean(HasErrorKey, value).apply()
        }

    private var hadInternet: Boolean
        get() = prefs.getBoolean(HadInternetKey, true)
        set(value) {
            prefs.edit().putBoolean(HadInternetKey, value).apply()
        }

    private var hasLoadedOnce = false

    private val _data = GlobalScope.async {
        MutableStateFlow(
            getDataResult(
                startFetch(
                    force = false,
                    canRefreshLocation = false
                ).previous?.value
            )
        )

    }

    suspend fun getData(): StateFlow<DataResult<WidgetData>> {
        return _data.await().asStateFlow()
    }

    init {
        GlobalScope.launch {
            if (!hasLoadedOnce) {
                refreshWidgetData(force = false, canRefreshLocation = false)
            }
        }
    }

    private fun getDataResult(widgetData: WidgetData?): DataResult<WidgetData> {
        return if (isLoading) {
            DataResult.loading(widgetData)
        } else if (hasError || widgetData == null) {
            DataResult.failure(
                Exception("Error loading widget data"),
                hadInternet = hadInternet,
                widgetData
            )
        } else {
            DataResult.success(widgetData)
        }
    }

    suspend fun refreshWidgetData(force: Boolean, canRefreshLocation: Boolean) = coroutineScope {
        if (isLoading && hasLoadedOnce) {
            return@coroutineScope
        }

        val (fetch, previous) = startFetch(force, canRefreshLocation)

        hasLoadedOnce = true
        isLoading = true
        hasError = false
        hadInternet = true
        _data.await().value = getDataResult(previous?.value)
        DepartureBoardWidget.onDataChanged()

        val result = fetch.await()

        isLoading = false
        if (result.isFailure()) {
            hasError = true
            hadInternet = result.hadInternet
        }
        _data.await().value = result
        DepartureBoardWidget.onDataChanged()
    }

    private suspend fun startFetch(
        force: Boolean,
        canRefreshLocation: Boolean
    ): FetchWithPrevious<WidgetData> {
        val anyWidgetsUseLocation =
            WidgetConfigurationManager.getWidgetConfigurations().values.any { it.useClosestStation }

        return WidgetDataFetcher.fetchWidgetDataWithPrevious(
            stationLimit = Int.MAX_VALUE,
            stations = Stations.All,
            lines = Line.entries,
            sort = Alphabetical,
            filter = TrainFilter.All,
            includeClosestStation = anyWidgetsUseLocation,
            canRefreshLocation = canRefreshLocation,
            staleness = Staleness(
                staleAfter = if (force) 5.seconds else 30.seconds,
                invalidAfter = Duration.INFINITE, // Always show old data while loading widget.
            )
        )
    }
}
