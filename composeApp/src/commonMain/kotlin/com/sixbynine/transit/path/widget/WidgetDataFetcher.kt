package com.sixbynine.transit.path.widget

import androidx.compose.ui.graphics.toArgb
import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.api.DepartureBoardTrainMap
import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.PathApi
import com.sixbynine.transit.path.api.PathApiException
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.TrainFilter
import com.sixbynine.transit.path.api.alerts.Alert
import com.sixbynine.transit.path.api.alerts.AlertsRepository
import com.sixbynine.transit.path.api.alerts.affectsLines
import com.sixbynine.transit.path.api.alerts.hidesTrainAt
import com.sixbynine.transit.path.api.impl.SchedulePathApi
import com.sixbynine.transit.path.api.isEastOf
import com.sixbynine.transit.path.api.isInNewJersey
import com.sixbynine.transit.path.api.isInNewYork
import com.sixbynine.transit.path.api.isWestOf
import com.sixbynine.transit.path.api.state
import com.sixbynine.transit.path.app.lifecycle.AppLifecycleObserver
import com.sixbynine.transit.path.app.settings.AvoidMissingTrains
import com.sixbynine.transit.path.app.settings.AvoidMissingTrains.*
import com.sixbynine.transit.path.app.settings.SettingsManager.currentAvoidMissingTrains
import com.sixbynine.transit.path.location.Location
import com.sixbynine.transit.path.location.LocationCheckResult.*
import com.sixbynine.transit.path.location.LocationProvider
import com.sixbynine.transit.path.time.NewYorkTimeZone
import com.sixbynine.transit.path.time.now
import com.sixbynine.transit.path.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.CoroutineStart.LAZY
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.datetime.DayOfWeek.SATURDAY
import kotlinx.datetime.DayOfWeek.SUNDAY
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource.Monotonic

object WidgetDataFetcher {

    private val lastClosestStationsKey = "fetcher_lastClosestStations"
    private var lastClosestStations: List<Station>?
        get() {
            val ids = globalDataStore().getString(lastClosestStationsKey)
            if (ids.isNullOrBlank()) return null
            return ids.split(",").mapNotNull { id -> Stations.byId(id) }
        }
        set(value) {
            globalDataStore()[lastClosestStationsKey] =
                value.orEmpty().joinToString(separator = ",") { it.pathApiName }
        }

    private val _nonFetchLocationReceived = MutableSharedFlow<Unit>()
    val nonFetchLocationReceived = _nonFetchLocationReceived.asSharedFlow()

    @Suppress("UNUSED") // Used by swift code
    fun widgetFetchStaleness(force: Boolean): Staleness {
        return Staleness(
            staleAfter = if (force) 5.seconds else 30.seconds,
            invalidAfter = Duration.INFINITE,
        )
    }

    @Suppress("UNUSED") // Used by swift code
    fun fetchWidgetData(
        stationLimit: Int,
        stations: List<Station>,
        lines: Collection<Line>,
        sort: StationSort,
        filter: TrainFilter,
        includeClosestStation: Boolean,
        staleness: Staleness,
        onSuccess: (WidgetData) -> Unit,
        onFailure: (
            error: Throwable,
            hadInternet: Boolean,
            isPathError: Boolean,
            data: WidgetData?,
        ) -> Unit,
    ): AgedValue<WidgetData>? {
        val (fetch, previous) = fetchWidgetDataWithPrevious(
            stationLimit = stationLimit,
            stations,
            lines,
            sort,
            filter,
            includeClosestStation,
            staleness,
        )
        GlobalScope.launch {
            fetch.await().onSuccess(onSuccess).onFailure { error, hadInternet, data ->
                onFailure(error, hadInternet, error is PathApiException, data)
            }
        }
        return previous
    }

    fun fetchWidgetDataWithPrevious(
        stationLimit: Int,
        stations: List<Station>,
        lines: Collection<Line>,
        sort: StationSort,
        filter: TrainFilter,
        includeClosestStation: Boolean,
        staleness: Staleness,
        canRefreshLocation: Boolean = true,
        isBackgroundUpdate: Boolean = false,
        now: Instant = now(),
    ): FetchWithPrevious<WidgetData> {
        Logging.d("Fetch widget data with previous, includeClosestStation = $includeClosestStation")
        val (liveDepartures, previousDepartures) =
            PathApi.instance.getUpcomingDepartures(now, staleness)
        val (alerts, previousAlerts) = AlertsRepository.getAlerts(now)

        fun createWidgetData(
            fetchTime: Instant,
            data: DepartureBoardTrainMap,
            alerts: List<Alert>?,
            closestStations: List<Station>?,
            isPathApiBroken: Boolean,
        ): WidgetData {
            return createWidgetData(
                now,
                fetchTime,
                stationLimit,
                stations,
                lines,
                sort,
                filter,
                closestStations,
                alerts,
                data,
                isPathApiBroken
            )
        }

        val previous = run {
            val data =
                previousDepartures ?: run {
                    SchedulePathApi().getUpcomingDepartures(now, staleness).previous
                } ?: return@run null
            AgedValue(
                data.age,
                createWidgetData(
                    now - data.age,
                    data.value,
                    previousAlerts?.value.orEmpty(),
                    lastClosestStations,
                    isPathApiBroken = false,
                )
            )
        }

        val fetch: Deferred<DataResult<WidgetData>> = GlobalScope.async(start = LAZY) {
            coroutineScope {
                liveDepartures.start()
                alerts.start()

                val stationsByProximity = when {
                    !includeClosestStation -> null
                    !canRefreshLocation && !AppLifecycleObserver.isActive.value -> {
                        lastClosestStations
                    }

                    else -> {
                        val mark = Monotonic.markNow()
                        val locationProvider = LocationProvider()
                        val locationResult =
                            withTimeoutCatching(locationProvider.defaultLocationCheckTimeout) {
                                locationProvider.tryToGetLocation()
                            }.getOrElse { Failure(it) }

                        when (locationResult) {
                            NoPermission, NoProvider -> null

                            JustChecked -> lastClosestStations

                            is Failure -> {
                                Logging.w("Failed to get location", locationResult.throwable)
                                lastClosestStations
                            }

                            is Success -> {
                                onLocationReceived(
                                    locationResult.location,
                                    mark.elapsedNow(),
                                    isDuringFetch = true,
                                )
                            }
                        }
                    }
                }

                val live = liveDepartures.await()
                val isPathApiBroken = live.isFailure() && live.error is PathApiException
                val scheduled = if (isPathApiBroken) {
                    SchedulePathApi().getUpcomingDepartures(now, staleness).fetch.await()
                } else {
                    null
                }

                val departureBoardTrainMap: DepartureBoardTrainMap?
                val lastFetchTime: Instant?

                when {
                    live.data != null -> {
                        departureBoardTrainMap = live.data
                        lastFetchTime = if (live.isSuccess()) {
                            now
                        } else {
                            now - previous?.age.orElse { Duration.ZERO }
                        }
                    }

                    scheduled?.data != null -> {
                        departureBoardTrainMap = scheduled.data
                        lastFetchTime = now
                    }

                    else -> {
                        departureBoardTrainMap = null
                        lastFetchTime = null
                    }
                }

                val widgetData = departureBoardTrainMap?.let { data ->
                    createWidgetData(
                        lastFetchTime ?: now,
                        data,
                        alerts.await().data,
                        stationsByProximity,
                        isPathApiBroken = isPathApiBroken,
                    )
                }

                when {
                    (live.isSuccess() || (scheduled?.data != null && isPathApiBroken)) &&
                            widgetData != null -> {
                        DataResult.success(widgetData)
                    }

                    live.isFailure() && live.data != null -> {
                        if (isBackgroundUpdate && widgetData != null && !live.hadInternet) {
                            // If this is a background update and we weren't given internet access,
                            // this is likely just the Android widget being throttled by the system.
                            // Let's treat it as success and just re-use the previous data.
                            Logging.d(
                                "Re-using previous data since the system denied us internet " +
                                        "during a background update",
                            )
                            DataResult.success(widgetData)
                        } else {
                            DataResult.failure(
                                error = live.error,
                                hadInternet = live.hadInternet,
                                data = widgetData
                            )
                        }
                    }

                    scheduled?.isFailure() == true -> {
                        DataResult.failure(
                            error = scheduled.error,
                            hadInternet = scheduled.hadInternet,
                            data = widgetData
                        )
                    }

                    else -> {
                        DataResult.failure(
                            error = IllegalStateException(),
                            hadInternet = true,
                            data = widgetData
                        )
                    }
                }
            }
        }
        val fetchWithTimeout = GlobalScope.async(start = LAZY) {
            suspendRunCatching {
                withTimeout(5.seconds) {
                    fetch.await()
                }
            }
                .fold(
                    onSuccess = { it },
                    onFailure = { cause ->
                        DataResult.failure(cause, hadInternet = true, data = previous?.value)
                    }
                )
        }
        return FetchWithPrevious(fetchWithTimeout, previous)
    }

    private fun createWidgetData(
        now: Instant,
        fetchTime: Instant,
        stationLimit: Int,
        stations: List<Station>,
        lines: Collection<Line>,
        sort: StationSort,
        filter: TrainFilter,
        closestStations: List<Station>?,
        alerts: List<Alert>?,
        data: DepartureBoardTrainMap,
        isPathApiBroken: Boolean,
    ): WidgetData {
        Logging.d("createWidgetData, stations = ${stations.map { it.pathApiName }}, lines=$lines")
        val adjustedStations = stations.toMutableList()
        val stationDatas = arrayListOf<WidgetData.StationData>()
        val avoidMissingTrains = currentAvoidMissingTrains()

        adjustedStations.sortWith(StationComparator(sort, closestStations))
        val closestStationToUse = closestStations?.firstOrNull()
        if (closestStationToUse != null) {
            adjustedStations.remove(closestStationToUse)
            adjustedStations.add(0, closestStationToUse)
        }

        for (station in adjustedStations) {
            val stationAlerts = alerts?.filter { station.pathApiName in it.stations }.orEmpty()

            val apiTrains =
                data.getTrainsAt(station)
                    ?.filterNot { train ->
                        stationAlerts.any { alert ->
                            alert.hidesTrainAt(
                                stationName = station.pathApiName,
                                headSign = train.headsign,
                                time = train.projectedArrival,
                            )
                        }
                    }
                    ?: continue
            val signs =
                apiTrains
                    .groupBy { it.headsign }
                    .mapNotNull { (headSign, trains) ->
                        if (!matchesFilter(station, headSign, filter)) {
                            return@mapNotNull null
                        }

                        val headSignLines = trains.flatMap { it.lines }.toSet()
                        if (headSignLines.isNotEmpty() && headSignLines.none { it in lines }) {
                            return@mapNotNull null
                        }

                        val colors =
                            trains.flatMap { it.lineColors }
                                .distinct()
                                .sortedBy { it.color.toArgb() }
                        val arrivals =
                            trains.map {
                                adjustToAvoidMissingTrains(
                                    now,
                                    it.projectedArrival,
                                    avoidMissingTrains
                                )
                            }
                                .distinct()
                                .sorted()
                        if (arrivals.isEmpty()) return@mapNotNull null
                        WidgetData.SignData(headSign, colors, arrivals)
                    }
                    .sortedBy { it.projectedArrivals.min() }

            val trains = apiTrains
                .asSequence()
                .map { it.toCommonUiTrainData() }
                .filter { train ->
                    (train.lines == null || train.lines.any { line -> line in lines })
                        .also {
                            if (!it) {
                                Logging.d("Filtering out train $train because it's not in the right lines")
                            }
                        }
                }
                .filter { station == closestStationToUse || matchesFilter(station, it, filter) }
                .map { it.adjustedToAvoidMissingTrains(now, avoidMissingTrains) }
                .distinctBy { it.title to it.projectedArrival }
                .sortedBy { it.projectedArrival }
                .toList()

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
                .take(stationLimit)
                .flatMap { it.signs }
                .mapNotNull { it.projectedArrivals.maxOrNull() }
                .minOrNull()
                ?: (now + 15.minutes)
        val globalAlerts =
            alerts.orEmpty().filter { alert -> alert.isGlobal && alert.affectsLines(lines) }
        return WidgetData(
            fetchTime = fetchTime,
            stations = stationDatas.take(stationLimit),
            nextFetchTime = nextFetchTime,
            closestStationId = closestStationToUse?.pathApiName,
            isPathApiBroken = isPathApiBroken,
            scheduleName = data.scheduleName,
            globalAlerts = globalAlerts,
        )
    }

    private fun matchesFilter(
        station: Station,
        train: WidgetData.TrainData,
        filter: TrainFilter
    ): Boolean {
        return matchesFilter(station, train.title, filter)
    }

    private fun matchesFilter(
        station: Station,
        headSign: String,
        filter: TrainFilter
    ): Boolean {
        if (filter == TrainFilter.All) return true

        val destination = Stations.fromHeadSign(headSign) ?: return true
        return when {
            // Newport -> Hoboken is the only time an NJ-terminating train travels east.
            destination == Stations.Hoboken -> station.isInNewYork
            station.isInNewYork -> destination.isInNewJersey || destination isWestOf station
            station.isInNewJersey -> destination.isInNewYork || destination isEastOf station
            else -> true // never happens, here for readability
        }
    }

    private fun WidgetData.TrainData.adjustedToAvoidMissingTrains(
        now: Instant,
        avoidMissingTrains: AvoidMissingTrains
    ): WidgetData.TrainData {
        val adjustedArrival =
            adjustToAvoidMissingTrains(now, projectedArrival, avoidMissingTrains)
        val delta = projectedArrival - adjustedArrival
        return copy(
            projectedArrival = adjustedArrival,
            backfillSource = backfillSource?.let { source ->
                source.copy(projectedArrival = source.projectedArrival - delta)
            }
        )
    }

    private fun adjustToAvoidMissingTrains(
        now: Instant,
        time: Instant,
        avoidMissingTrains: AvoidMissingTrains
    ): Instant {
        val subtractTime = when (avoidMissingTrains) {
            Disabled -> false
            OffPeak -> isOffPeak(time)
            Always -> true
        }
        return if (subtractTime) {
            (time - 3.minutes).coerceAtLeast(now)
        } else {
            time
        }
    }

    private fun isOffPeak(time: Instant): Boolean {
        val projectedDateTime = time.toLocalDateTime(NewYorkTimeZone)
        if (projectedDateTime.dayOfWeek == SATURDAY || projectedDateTime.dayOfWeek == SUNDAY) {
            return true
        }

        return projectedDateTime.hour !in 6..9 && projectedDateTime.hour !in 16..19
    }

    @Suppress("UNUSED") // Used by swift code
    fun prunePassedDepartures(data: WidgetData?, time: Instant): WidgetData? {
        data ?: return null
        return data.copy(
            stations = data.stations.map { station ->
                station.copy(
                    trains = station.trains.filter { it.projectedArrival >= time },
                    signs = station.signs.mapNotNull { sign ->
                        sign.copy(projectedArrivals = sign.projectedArrivals.filter { it >= time })
                            .takeUnless { it.projectedArrivals.isEmpty() }
                    }
                )
            }
        )
    }

    internal fun onLocationReceived(
        location: Location,
        duration: Duration,
        isDuringFetch: Boolean
    ): List<Station> {
        return Stations.byProximityTo(location).also { stations ->
            Logging.d(
                "Received current location as $location in ${duration}, closest stations are " +
                        stations.map { it.displayName }
            )
            lastClosestStations = stations
            if (!isDuringFetch) {
                _nonFetchLocationReceived.tryEmit(Unit)
            }
        }
    }
}
