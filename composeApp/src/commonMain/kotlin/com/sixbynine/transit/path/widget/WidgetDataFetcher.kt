package com.sixbynine.transit.path.widget

import androidx.compose.ui.graphics.toArgb
import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.api.DepartureBoardTrainMap
import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.PathApi
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.TrainFilter
import com.sixbynine.transit.path.api.alerts.GithubAlerts
import com.sixbynine.transit.path.api.alerts.GithubAlertsRepository
import com.sixbynine.transit.path.api.alerts.hidesTrainAt
import com.sixbynine.transit.path.api.isEastOf
import com.sixbynine.transit.path.api.isInNewJersey
import com.sixbynine.transit.path.api.isInNewYork
import com.sixbynine.transit.path.api.isWestOf
import com.sixbynine.transit.path.api.state
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
import com.sixbynine.transit.path.util.map
import com.sixbynine.transit.path.util.onFailure
import com.sixbynine.transit.path.util.onSuccess
import com.sixbynine.transit.path.util.suspendRunCatching
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.DayOfWeek.SATURDAY
import kotlinx.datetime.DayOfWeek.SUNDAY
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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

    fun fetchWidgetData(
        limit: Int,
        stations: List<Station>,
        lines: Collection<Line>,
        sort: StationSort,
        filter: TrainFilter,
        includeClosestStation: Boolean,
        staleness: Staleness,
        onSuccess: (WidgetData) -> Unit,
        onFailure: (error: Throwable, hadInternet: Boolean, data: WidgetData?) -> Unit,
    ): AgedValue<WidgetData>? {
        val (fetch, previous) = fetchWidgetDataWithPrevious(
            limit,
            stations,
            lines,
            sort,
            filter,
            includeClosestStation,
            staleness,
        )
        GlobalScope.launch {
            fetch.await().onSuccess(onSuccess).onFailure(onFailure)
        }
        return previous
    }

    fun fetchWidgetDataWithPrevious(
        limit: Int,
        stations: List<Station>,
        lines: Collection<Line>,
        sort: StationSort,
        filter: TrainFilter,
        includeClosestStation: Boolean,
        staleness: Staleness,
    ): FetchWithPrevious<WidgetData> {
        Logging.initialize()
        val now = now()
        val (departures, previousDepartures) =
            PathApi.instance.getUpcomingDepartures(now, staleness)
        val (githubAlerts, previousGithubAlerts) = GithubAlertsRepository.getAlerts(now)

        fun createWidgetData(
            data: DepartureBoardTrainMap,
            githubAlerts: GithubAlerts?,
            closestStations: List<Station>?,
        ): WidgetData {
            return createWidgetData(
                now,
                limit,
                stations,
                lines,
                sort,
                filter,
                closestStations,
                githubAlerts,
                data
            )
        }

        val previous = previousDepartures?.value?.let {
            AgedValue(
                previousDepartures.age,
                createWidgetData(
                    it,
                    previousGithubAlerts?.value,
                    lastClosestStations
                )
            )
        }

        val fetch = GlobalScope.async {
            coroutineScope {
                val stationsByProximity =
                    if (includeClosestStation) {
                        when (val locationResult =
                            LocationProvider().tryToGetLocation(3.seconds)) {
                            NoPermission, NoProvider -> null
                            is Failure -> lastClosestStations
                            is Success -> {
                                Stations.byProximityTo(locationResult.location)
                                    .also { lastClosestStations = it }
                            }
                        }
                    } else {
                        null
                    }

                departures.await()
                    .map {
                        createWidgetData(
                            it,
                            githubAlerts.await().data,
                            stationsByProximity
                        )
                    }
            }
        }
        val fetchWithTimeout = GlobalScope.async {
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
        limit: Int,
        stations: List<Station>,
        lines: Collection<Line>,
        sort: StationSort,
        filter: TrainFilter,
        closestStations: List<Station>?,
        githubAlerts: GithubAlerts?,
        data: DepartureBoardTrainMap
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
                        WidgetData.SignData(
                            headSign,
                            colors,
                            arrivals,
                        )
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
                                Logging.d("Filtering out train ${train} because it's not in the right lines")
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
                .take(limit)
                .flatMap { it.signs }
                .mapNotNull { it.projectedArrivals.maxOrNull() }
                .minOrNull()
                ?: (now + 15.minutes)
        return WidgetData(
            fetchTime = now,
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
}
