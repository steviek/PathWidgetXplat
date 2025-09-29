package com.desaiwang.transit.path.widget

import android.annotation.SuppressLint
import android.content.Context
import com.desaiwang.transit.path.Logging
import com.desaiwang.transit.path.MobilePathApplication
import com.desaiwang.transit.path.api.Line
import com.desaiwang.transit.path.api.StationSort.Alphabetical
import com.desaiwang.transit.path.api.Stations
import com.desaiwang.transit.path.api.TrainFilter
import com.desaiwang.transit.path.app.lifecycle.AppLifecycleObserver
import com.desaiwang.transit.path.model.DepartureBoardData
import com.desaiwang.transit.path.util.DataResult
import com.desaiwang.transit.path.util.FetchWithPrevious
import com.desaiwang.transit.path.util.Staleness
import com.desaiwang.transit.path.util.isFailure
import com.desaiwang.transit.path.util.isSuccess
import com.desaiwang.transit.path.widget.configuration.WidgetConfigurationManager
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

    private var fetchId = 1

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
                    canRefreshLocation = false,
                    fetchId = 0,
                ).previous?.value
            )
        )

    }

    suspend fun getData(): StateFlow<DataResult<DepartureBoardData>> {
        return _data.await().asStateFlow()
    }

    init {
        GlobalScope.launch {
            if (!hasLoadedOnce) {
                refreshWidgetData(
                    force = false,
                    canRefreshLocation = false,
                    isBackgroundUpdate = !AppLifecycleObserver.isActive.value
                )
            }
        }
    }

    private fun getDataResult(departureBoardData: DepartureBoardData?): DataResult<DepartureBoardData> {
        return if (isLoading) {
            DataResult.loading(departureBoardData)
        } else if (hasError || departureBoardData == null) {
            DataResult.failure(
                Exception("Error loading widget data"),
                hadInternet = hadInternet,
                departureBoardData
            )
        } else {
            DataResult.success(departureBoardData)
        }
    }

    suspend fun refreshWidgetData(
        force: Boolean,
        canRefreshLocation: Boolean,
        isBackgroundUpdate: Boolean,
    ) = coroutineScope {
        if (isLoading && hasLoadedOnce) {
            Logging.d("WDR: join fetch $fetchId")
            return@coroutineScope
        }

        val fetchId = fetchId++
        Logging.d("WDR: start fetch $fetchId")

        val (fetch, previous) = startFetch(force, canRefreshLocation, isBackgroundUpdate, fetchId)

        hasLoadedOnce = true
        isLoading = true
        hasError = false
        hadInternet = true
        _data.await().value = getDataResult(previous?.value)
        DepartureBoardWidget.onDataChanged()

        val result = fetch.await()

        isLoading = false
        if (result.isFailure()) {
            Logging.w("WDR: fetch $fetchId error loading widget data", result.error)
            hasError = true
            hadInternet = result.hadInternet
        } else if (result.isSuccess()) {
            Logging.d("WDR: fetch $fetchId completed successfully")
        }

        _data.await().value = result
        DepartureBoardWidget.onDataChanged()
    }

    private suspend fun startFetch(
        force: Boolean,
        canRefreshLocation: Boolean,
        isBackgroundUpdate: Boolean = !AppLifecycleObserver.isActive.value,
        fetchId: Int,
    ): FetchWithPrevious<DepartureBoardData> {
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
            isBackgroundUpdate = isBackgroundUpdate,
            staleness = Staleness(
                staleAfter = if (force) 5.seconds else 30.seconds,
                invalidAfter = Duration.INFINITE, // Always show old data while loading widget.
            ),
            fetchId = fetchId,

        )
    }
}
