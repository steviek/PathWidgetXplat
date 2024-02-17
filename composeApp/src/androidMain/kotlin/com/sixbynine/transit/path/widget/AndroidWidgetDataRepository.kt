package com.sixbynine.transit.path.widget

import android.annotation.SuppressLint
import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.PathApplication
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.StationSort.Alphabetical
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.TrainFilter
import com.sixbynine.transit.path.location.LocationCheckResult.Success
import com.sixbynine.transit.path.location.LocationProvider
import com.sixbynine.transit.path.util.DataResult
import com.sixbynine.transit.path.util.JsonFormat
import com.sixbynine.transit.path.util.isFailure
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlin.time.Duration.Companion.seconds

@SuppressLint("StaticFieldLeak") // application context
object AndroidWidgetDataRepository {

    private const val WidgetDataKey = "widget_data"
    private const val IsLoadingKey = "is_loading"
    private const val HasErrorKey = "has_error"
    private const val ClosestStationKey = "closest_station"

    private val context: Context = PathApplication.instance
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

    private var widgetData: WidgetData? = readWidgetData()
        private set(value) {
            field = value
            storeWidgetData(value)
        }

    private var closestStation: Station?
        get() {
            val id = prefs.getString(ClosestStationKey, null) ?: return null
            return Stations.All.find { it.pathApiName == id }
        }
        set(value) {
            if (value == null) {
                prefs.edit().remove(ClosestStationKey).apply()
            } else {
                prefs.edit().putString(ClosestStationKey, value.pathApiName).apply()
            }
        }

    private var hasLoadedOnce = false

    private var _data = MutableStateFlow(getData())
    val data = _data.asStateFlow()

    init {
        GlobalScope.launch {
            if (!hasLoadedOnce) {
                refreshWidgetData(force = false)
            }
        }
    }

    private fun getData(): DataResult<WidgetDataWithClosestStation> {
        val data = widgetData?.let { WidgetDataWithClosestStation(closestStation, it) }
        return if (isLoading) {
            DataResult.loading(data)
        } else if (hasError || data == null) {
            DataResult.failure(Exception("Error loading widget data"), data)
        } else {
            DataResult.success(data)
        }
    }

    suspend fun refreshWidgetData(force: Boolean) = coroutineScope {
        if (isLoading && hasLoadedOnce) {
            return@coroutineScope
        }

        hasLoadedOnce = true
        isLoading = true
        hasError = false
        _data.value = getData()
        DepartureBoardWidget.onDataChanged()

        val locationAsync = async { LocationProvider().tryToGetLocation(timeout = 3.seconds) }

        val result = WidgetDataFetcher.fetchWidgetDataSuspending(
            limit = Int.MAX_VALUE,
            stations = Stations.All,
            sort = Alphabetical,
            filter = TrainFilter.All,
            force = force,
        )

        val locationResult = locationAsync.await()
        if (locationResult is Success) {
            val (latitude, longitude) = locationResult.location
            closestStation =
                Stations.All
                    .minBy { station ->
                        val dLatitude = station.coordinates.latitude - latitude
                        val dLongitude = station.coordinates.longitude - longitude
                        dLatitude * dLatitude + dLongitude * dLongitude
                    }
        }

        isLoading = false
        hasError = result.isFailure()
        widgetData = result.data
        _data.value = getData()
        DepartureBoardWidget.onDataChanged()
        Logging.d("DataRepo: done refreshing widget data")
    }

    private fun readWidgetData(): WidgetData? {
        val json = prefs.getString(WidgetDataKey, null) ?: return null
        return try {
            JsonFormat.decodeFromString(json)
        } catch (e: Exception) {
            Firebase.crashlytics.recordException(e)
            Logging.e("Error decoding widget data", e)
            null
        }
    }

    private fun storeWidgetData(widgetData: WidgetData?) {
        if (widgetData == null) {
            prefs.edit().remove(WidgetDataKey).apply()
            return
        }

        val json = try {
            JsonFormat.encodeToString(widgetData)
        } catch (e: Exception) {
            Firebase.crashlytics.recordException(e)
            Logging.e("Error encoding widget data", e)
            return
        }
        prefs.edit().putString(WidgetDataKey, json).apply()
    }

    data class WidgetDataWithClosestStation(val closestStation: Station?, val widgetData: WidgetData)

}
