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
import com.sixbynine.transit.path.api.alerts.GithubAlerts
import com.sixbynine.transit.path.api.alerts.GithubAlertsRepository
import com.sixbynine.transit.path.api.alerts.hidesTrainAt
import com.sixbynine.transit.path.api.impl.SchedulePathApi
import com.sixbynine.transit.path.api.isEastOf
import com.sixbynine.transit.path.api.isInNewJersey
import com.sixbynine.transit.path.api.isInNewYork
import com.sixbynine.transit.path.api.isWestOf
import com.sixbynine.transit.path.api.state
import com.sixbynine.transit.path.app.lifecycle.AppLifecycleObserver
import com.sixbynine.transit.path.app.settings.AvoidMissingTrains
import com.sixbynine.transit.path.app.settings.AvoidMissingTrains.Always
import com.sixbynine.transit.path.app.settings.AvoidMissingTrains.Disabled
import com.sixbynine.transit.path.app.settings.AvoidMissingTrains.OffPeak
import com.sixbynine.transit.path.app.settings.SettingsManager.currentAvoidMissingTrains
import com.sixbynine.transit.path.location.LocationCheckResult.Failure
import com.sixbynine.transit.path.location.LocationCheckResult.NoPermission
import com.sixbynine.transit.path.location.LocationCheckResult.NoProvider
import com.sixbynine.transit.path.location.LocationCheckResult.Success
import com.sixbynine.transit.path.location.LocationProvider
import com.sixbynine.transit.path.preferences.Preferences
import com.sixbynine.transit.path.preferences.StringPreferencesKey
import com.sixbynine.transit.path.time.NewYorkTimeZone
import com.sixbynine.transit.path.time.now
import com.sixbynine.transit.path.util.AgedValue
import com.sixbynine.transit.path.util.DataResult
import com.sixbynine.transit.path.util.FetchWithPrevious
import com.sixbynine.transit.path.util.Staleness
import com.sixbynine.transit.path.util.isFailure
import com.sixbynine.transit.path.util.isSuccess
import com.sixbynine.transit.path.util.onFailure
import com.sixbynine.transit.path.util.onSuccess
import com.sixbynine.transit.path.util.suspendRunCatching
import kotlinx.coroutines.CoroutineStart.LAZY
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.DayOfWeek.SATURDAY
import kotlinx.datetime.DayOfWeek.SUNDAY
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource.Monotonic

object WidgetDataFetcher {

    private val lastClosestStationsKey = StringPreferencesKey("fetcher_lastClosestStations")
    private var lastClosestStations: List<Station>?
        get() {
            val ids = Preferences()[lastClosestStationsKey]
            if (ids.isNullOrBlank()) return null
            return ids.split(",").mapNotNull { id -> Stations.byId(id) }
        }
        set(value) {
            Preferences()[lastClosestStationsKey] =
                value.orEmpty().joinToString(separator = ",") { it.pathApiName }
        }

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
                onFailure(
                    error,
                    hadInternet,
                    error is PathApiException,
                    data,
                )
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
        now: Instant = now(),
    ): FetchWithPrevious<WidgetData> {
        Logging.d("Fetch widget data with previous, includeClosestStation = $includeClosestStation")
        val (liveDepartures, previousDepartures) =
            PathApi.instance.getUpcomingDepartures(now, staleness)
        val (githubAlerts, previousGithubAlerts) = GithubAlertsRepository.getAlerts(now)

        fun createWidgetData(
            data: DepartureBoardTrainMap,
            githubAlerts: GithubAlerts?,
            closestStations: List<Station>?,
            isPathApiBroken: Boolean,
        ): WidgetData {
            return createWidgetData(
                now,
                stationLimit,
                stations,
                lines,
                sort,
                filter,
                closestStations,
                githubAlerts,
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
                    data.value,
                    previousGithubAlerts?.value,
                    lastClosestStations,
                    isPathApiBroken = false,
                )
            )
        }

        val fetch: Deferred<DataResult<WidgetData>> = GlobalScope.async(start = LAZY) {
            coroutineScope {
                liveDepartures.start()
                githubAlerts.start()

                val stationsByProximity = when {
                    !includeClosestStation -> null
                    !canRefreshLocation && !AppLifecycleObserver.isActive.value -> {
                        lastClosestStations
                    }

                    else -> {
                        val mark = Monotonic.markNow()
                        val locationResult = try {
                            withTimeout(3.seconds) { LocationProvider().tryToGetLocation() }
                        } catch (e: TimeoutCancellationException) {
                            Logging.w("Timed out trying to get the user's location")
                            Failure(e)
                        }

                        when (locationResult) {
                            NoPermission, NoProvider -> null
                            is Failure -> lastClosestStations
                            is Success -> {
                                Stations.byProximityTo(locationResult.location)
                                    .also {
                                        Logging.d(
                                            "Received current location as " +
                                                    "${locationResult.location} in " +
                                                    "${mark.elapsedNow()}, closest stations are" +
                                                    " ${it.map { it.displayName }}"
                                        )
                                        lastClosestStations = it
                                    }
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

                val departureBoardTrainMap = live.data ?: scheduled?.data

                val widgetData = departureBoardTrainMap?.let { data ->
                    createWidgetData(
                        data,
                        githubAlerts.await().data,
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
                        DataResult.failure(
                            error = live.error,
                            hadInternet = live.hadInternet,
                            data = widgetData
                        )
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
        stationLimit: Int,
        stations: List<Station>,
        lines: Collection<Line>,
        sort: StationSort,
        filter: TrainFilter,
        closestStations: List<Station>?,
        githubAlerts: GithubAlerts?,
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
            val stationAlerts =
                githubAlerts?.alerts?.filter { station.pathApiName in it.stations }.orEmpty()
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
        val globalAlerts = githubAlerts?.alerts?.filter { it.isGlobal }.orEmpty()
        return WidgetData(
            fetchTime = now,
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
}
