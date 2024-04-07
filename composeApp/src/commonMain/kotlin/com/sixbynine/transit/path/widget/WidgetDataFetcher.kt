package com.sixbynine.transit.path.widget

import androidx.compose.ui.graphics.toArgb
import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.api.DepartureBoardTrain
import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.PathApi
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.TrainFilter
import com.sixbynine.transit.path.api.alerts.GithubAlerts
import com.sixbynine.transit.path.api.alerts.GithubAlertsRepository
import com.sixbynine.transit.path.api.alerts.hidesTrainNow
import com.sixbynine.transit.path.api.isEastOf
import com.sixbynine.transit.path.api.isInNewJersey
import com.sixbynine.transit.path.api.isInNewYork
import com.sixbynine.transit.path.api.isWestOf
import com.sixbynine.transit.path.api.state
import com.sixbynine.transit.path.location.LocationCheckResult.Failure
import com.sixbynine.transit.path.location.LocationCheckResult.NoPermission
import com.sixbynine.transit.path.location.LocationCheckResult.NoProvider
import com.sixbynine.transit.path.location.LocationCheckResult.Success
import com.sixbynine.transit.path.location.LocationProvider
import com.sixbynine.transit.path.network.NetworkManager
import com.sixbynine.transit.path.preferences.Preferences
import com.sixbynine.transit.path.preferences.StringPreferencesKey
import com.sixbynine.transit.path.util.DataResult
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object WidgetDataFetcher {

    private var lastFetchTime: Instant? = null
    private var lastFetch: Deferred<Result<Map<Station, List<DepartureBoardTrain>>>>? = null

    private val lastClosestStationKey = StringPreferencesKey("fetcher_lastClosestStation")
    private var lastClosestStation: Station?
        get() {
            val id = Preferences()[lastClosestStationKey] ?: return null
            return Stations.All.find { it.pathApiName == id }
        }
        set(value) {
            Preferences()[lastClosestStationKey] = value?.pathApiName
        }

    fun fetchWidgetData(
        limit: Int,
        stations: List<Station>,
        lines: Collection<Line>,
        sort: StationSort,
        filter: TrainFilter,
        force: Boolean,
        includeClosestStation: Boolean,
        onSuccess: (WidgetData) -> Unit,
        onFailure: (error: Throwable, hadInternet: Boolean, data: WidgetData?) -> Unit,
    ) {
        Logging.initialize()
        GlobalScope.launch {
            val result = async { PathApi.instance.fetchUpcomingDepartures(force) }
            val deferredGithubAlerts = async { GithubAlertsRepository.getAlerts() }

            val closestStationToUse =
                if (includeClosestStation) {
                    when (val locationResult = LocationProvider().tryToGetLocation(3.seconds)) {
                        NoPermission, NoProvider -> null
                        is Failure -> lastClosestStation
                        is Success -> {
                            Stations.closestTo(locationResult.location)
                                .also { lastClosestStation = it }
                        }
                    }
                } else {
                    null
                }

            var hadInternet = NetworkManager().isConnectedToInternet()
            val githubAlerts = deferredGithubAlerts.await().getOrNull()

            fun createWidgetData(data: Map<String, List<DepartureBoardTrain>>): WidgetData {
                return createWidgetData(
                    limit,
                    stations,
                    lines,
                    sort,
                    filter,
                    closestStationToUse,
                    githubAlerts,
                    data
                )
            }

            result
                .await()
                .onSuccess { onSuccess(createWidgetData(it)) }
                .onFailure {
                    Logging.e("Failed to fetch", it)

                    if ("Unable to resolve host" in it.message.toString()) {
                        hadInternet = false
                    }

                    val lastResults = PathApi.instance.getLastSuccessfulUpcomingDepartures()
                    onFailure(
                        it,
                        hadInternet,
                        lastResults?.let { createWidgetData(it) }
                    )
                }
        }
    }

    private fun createWidgetData(
        limit: Int,
        stations: List<Station>,
        lines: Collection<Line>,
        sort: StationSort,
        filter: TrainFilter,
        closestStationToUse: Station?,
        githubAlerts: GithubAlerts?,
        data: Map<String, List<DepartureBoardTrain>>
    ): WidgetData {
        Logging.d("createWidgetData, stations = ${stations.map { it.pathApiName }}, lines=$lines")
        val adjustedStations = stations.toMutableList()
        val stationDatas = arrayListOf<WidgetData.StationData>()

        adjustedStations.sortWith(StationComparator(sort))
        if (closestStationToUse != null) {
            adjustedStations.remove(closestStationToUse)
            adjustedStations.add(0, closestStationToUse)
        }

        for (station in adjustedStations) {
            val stationAlerts =
                githubAlerts?.alerts?.filter { station.pathApiName in it.stations }.orEmpty()
            val apiTrains =
                data[station.pathApiName]
                    ?.filterNot { train ->
                        stationAlerts.any { alert ->
                            alert.hidesTrainNow(
                                stationName = station.pathApiName,
                                headSign = train.headsign
                            )
                        }
                    }
                    ?: continue
            val signs =
                apiTrains
                    .groupBy { it.headsign }
                    .mapNotNull { (headSign, trains) ->
                        val colors =
                            trains.flatMap { it.lineColors }
                                .distinct()
                                .sortedBy { it.color.toArgb() }
                        val arrivals = trains.map { it.projectedArrival }.distinct().sorted()
                        if (arrivals.isEmpty()) return@mapNotNull null
                        WidgetData.SignData(
                            headSign,
                            colors,
                            arrivals,
                        )
                    }
                    .sortedBy { it.projectedArrivals.min() }

            val trains = apiTrains
                .map {
                    val colors = it.lineColors.distinct()
                    WidgetData.TrainData(
                        id = it.headsign + ":" + it.projectedArrival,
                        title = it.headsign,
                        colors = colors,
                        projectedArrival = it.projectedArrival,
                        isDelayed = it.isDelayed,
                        backfillSource = it.backfillSource,
                        lines = it.lines,
                    )
                }
                .filter { train ->
                    (train.lines == null || train.lines.any { line -> line in lines })
                        .also {
                            if (!it) {
                                Logging.d("Filtering out train ${train} because it's not in the right lines")
                            }
                        }
                }
                .filter { station == closestStationToUse || matchesFilter(station, it, filter) }
                .distinctBy { it.title to it.projectedArrival }
                .sortedBy { it.projectedArrival }

            stationDatas += WidgetData.StationData(
                id = station.pathApiName,
                displayName = station.displayName,
                signs = signs,
                trains = trains,
                state = station.state,
                alerts = stationAlerts,
            )
        }
        val nextFetchTime =
            stationDatas
                .take(limit)
                .flatMap { it.signs }
                .mapNotNull { it.projectedArrivals.maxOrNull() }
                .minOrNull()
                ?: (Clock.System.now() + 15.minutes)
        return WidgetData(
            fetchTime = Clock.System.now(),
            stations = stationDatas.take(limit),
            nextFetchTime = nextFetchTime,
            closestStationId = closestStationToUse?.pathApiName,
        )
    }

    private fun matchesFilter(
        station: Station,
        train: WidgetData.TrainData,
        filter: TrainFilter
    ): Boolean {
        if (filter == TrainFilter.All) return true

        val destination = Stations.fromHeadSign(train.title) ?: return true
        return when {
            // Newport -> Hoboken is the only time an NJ-terminating train travels east.
            destination == Stations.Hoboken -> station.isInNewYork
            station.isInNewYork -> destination.isInNewJersey || destination isWestOf station
            station.isInNewJersey -> destination.isInNewYork || destination isEastOf station
            else -> true // never happens, here for readability
        }
    }
}

suspend fun WidgetDataFetcher.fetchWidgetDataSuspending(
    limit: Int,
    stations: List<Station>,
    lines: Collection<Line>,
    sort: StationSort,
    filter: TrainFilter,
    force: Boolean,
    includeClosestStation: Boolean,
): DataResult<WidgetData> {
    return suspendCancellableCoroutine { continuation ->
        fetchWidgetData(
            limit,
            stations,
            lines,
            sort,
            filter,
            force,
            includeClosestStation,
            { continuation.resume(DataResult.success(it)) },
            { e, hadInternet, data ->
                continuation.resume(
                    DataResult.failure(
                        e,
                        hadInternet,
                        data
                    )
                )
            }
        )
    }
}

