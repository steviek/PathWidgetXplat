package com.sixbynine.transit.path.widget

import androidx.compose.ui.graphics.toArgb
import kotlin.native.ObjCName
import kotlin.experimental.ExperimentalObjCName
import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.PathApi
import com.sixbynine.transit.path.api.PathApiException
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.TrainFilter
import com.sixbynine.transit.path.api.UpcomingDepartures
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
import com.sixbynine.transit.path.app.settings.AvoidMissingTrains.Always
import com.sixbynine.transit.path.app.settings.AvoidMissingTrains.Disabled
import com.sixbynine.transit.path.app.settings.AvoidMissingTrains.OffPeak
import com.sixbynine.transit.path.app.settings.SettingsManager.currentAvoidMissingTrains
import com.sixbynine.transit.path.location.Location
import com.sixbynine.transit.path.location.LocationCheckResult.Failure
import com.sixbynine.transit.path.location.LocationCheckResult.JustChecked
import com.sixbynine.transit.path.location.LocationCheckResult.NoPermission
import com.sixbynine.transit.path.location.LocationCheckResult.NoProvider
import com.sixbynine.transit.path.location.LocationCheckResult.Success
import com.sixbynine.transit.path.location.LocationProvider
import com.sixbynine.transit.path.model.DepartureBoardData
import com.sixbynine.transit.path.time.NewYorkTimeZone
import com.sixbynine.transit.path.time.now
import com.sixbynine.transit.path.util.AgedValue
import com.sixbynine.transit.path.util.DataResult
import com.sixbynine.transit.path.util.FetchWithPrevious
import com.sixbynine.transit.path.util.Staleness
import com.sixbynine.transit.path.util.flatten
import com.sixbynine.transit.path.util.globalDataStore
import com.sixbynine.transit.path.util.isFailure
import com.sixbynine.transit.path.util.isSuccess
import com.sixbynine.transit.path.util.onFailure
import com.sixbynine.transit.path.util.onLoading
import com.sixbynine.transit.path.util.onSuccess
import com.sixbynine.transit.path.util.orElse
import com.sixbynine.transit.path.util.toDataResult
import com.sixbynine.transit.path.util.withTimeoutCatching
import kotlinx.coroutines.CoroutineStart.LAZY
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek.SATURDAY
import kotlinx.datetime.DayOfWeek.SUNDAY
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource.Monotonic

@OptIn(ExperimentalObjCName::class)
object WidgetDataFetcher {

    /**
     * Determines which PATH lines run between a departure station and destination station.
     * 
     * This function analyzes the PATH system routes to find all possible lines that could
     * take a passenger from their departure station to their destination station.
     *
     * For example:
     * - Newark (NWK) to World Trade Center (WTC): [NewarkWtc]
     * - Journal Square (JSQ) to 33rd St (33S): [JournalSquare33rd, Hoboken33rd]
     * - Newport (NEW) to Hoboken (HOB): [HobokenWtc]
     *
     * @param departure pathAPIName of departure station (e.g. "NWK", "JSQ", "HOB").
     * @param destination pathAPIName of destination station (e.g. "WTC", "33S", "HOB")
     * @return Set of PATH lines that serve this station pair
     */
    /**
     * Determines which PATH lines run between a departure station and destination station.
     * This function is exposed to iOS through Kotlin Multiplatform.
     */
    @ObjCName(name = "getLinesForStationPair") // This exposes the function directly to Swift
    fun getLinesForStationPair(departure: String, destination: String): Set<Line> {
        // Helper function to check if a station is between two other stations on a route
        fun isStationBetween(station: String, start: String, end: String, route: List<String>): Boolean {
            val startIdx = route.indexOf(start)
            val endIdx = route.indexOf(end)
            val stationIdx = route.indexOf(station)
            return when {
                startIdx == -1 || endIdx == -1 || stationIdx == -1 -> false
                startIdx < endIdx -> stationIdx in startIdx..endIdx
                else -> stationIdx in endIdx..startIdx
            }
        }

        // Define the main PATH routes
        val nwkWtcRoute = listOf("NWK", "HAR", "JSQ", "GRV", "EXP", "WTC")
        val jsq33sRoute = listOf("JSQ", "GRV", "NEW", "CHR", "09S", "14S", "23S", "33S")
        val hobWtcRoute = listOf("HOB", "NEW", "EXP", "WTC")
        val hob33sRoute = listOf("HOB", "CHR", "09S", "14S", "23S", "33S")
        val jsqHob33sRoute = listOf("JSQ", "GRV", "NEW", "HOB", "CHR", "09S", "14S", "23S", "33S")

        val lines = mutableSetOf<Line>()

        // Check each route to see if it connects our station pair
        if (isStationBetween(departure, "NWK", "WTC", nwkWtcRoute) && 
            isStationBetween(destination, "NWK", "WTC", nwkWtcRoute)) {
            lines.add(Line.NewarkWtc)
        }

        if (isStationBetween(departure, "JSQ", "33S", jsq33sRoute) && 
            isStationBetween(destination, "JSQ", "33S", jsq33sRoute)) {
            lines.add(Line.JournalSquare33rd)
        }

        if (isStationBetween(departure, "HOB", "WTC", hobWtcRoute) && 
            isStationBetween(destination, "HOB", "WTC", hobWtcRoute)) {
            lines.add(Line.HobokenWtc)
        }

        if (isStationBetween(departure, "HOB", "33S", hob33sRoute) && 
            isStationBetween(destination, "HOB", "33S", hob33sRoute)) {
            lines.add(Line.Hoboken33rd)
        }

        // Special case for JSQ-HOB-33S service
        if (isStationBetween(departure, "JSQ", "33S", jsqHob33sRoute) && 
            isStationBetween(destination, "JSQ", "33S", jsqHob33sRoute)) {
            // This route runs during off-peak hours
            if (isStationBetween("HOB", departure, destination, jsqHob33sRoute) || 
                isStationBetween("HOB", destination, departure, jsqHob33sRoute)) {
                lines.add(Line.Hoboken33rd)
                lines.add(Line.JournalSquare33rd)
            }
        }

        return lines
    }

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
        onSuccess: (DepartureBoardData) -> Unit,
        onFailure: (
            error: Throwable,
            hadInternet: Boolean,
            isPathError: Boolean,
            data: DepartureBoardData?,
        ) -> Unit,
    ): AgedValue<DepartureBoardData>? {
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
        fetchId: Int? = null,
    ): FetchWithPrevious<DepartureBoardData> {
        val fetchIdLabel = fetchId?.let { " [$fetchId]" }.orEmpty()
        Logging.d(
            "Fetch$fetchIdLabel widget data with previous, " +
                    "includeClosestStation = $includeClosestStation"
        )
        val (liveDepartures, previousDepartures) =
            PathApi.instance.getUpcomingDepartures(now, staleness)
        val (alerts, previousAlerts) = AlertsRepository.getAlerts(now)

        fun createWidgetData(
            fetchTime: Instant,
            data: UpcomingDepartures,
            alerts: List<Alert>?,
            closestStations: List<Station>?,
            isPathApiBroken: Boolean,
        ): DepartureBoardData {
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

        val fetch: Deferred<DataResult<DepartureBoardData>> = GlobalScope.async(start = LAZY) {
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
                                Logging.w(
                                    "Fetch$fetchIdLabel: failed to get location",
                                    locationResult.throwable
                                )
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

                val live =
                    withTimeoutCatching(5.seconds) {
                        liveDepartures.await()
                    }
                        .toDataResult()
                        .flatten()
                        .onSuccess {
                            Logging.d("Fetch$fetchIdLabel: received live departures")
                        }
                        .onLoading {
                            Logging.w(
                                "Fetch$fetchIdLabel: received loading live departures"
                            )
                        }
                        .onFailure { error, hadInternet, _ ->
                            Logging.w(
                                "Fetch$fetchIdLabel: failed fetching live departures, " +
                                        "hadInternet = $hadInternet",
                                error
                            )
                        }
                val isPathApiBroken = live.isFailure() && live.error is PathApiException
                val scheduled = if (isPathApiBroken) {
                    SchedulePathApi().getUpcomingDepartures(now, staleness).fetch.await()
                } else {
                    null
                }

                val upcomingDepartures: UpcomingDepartures?
                val lastFetchTime: Instant?

                when {
                    live.data != null -> {
                        upcomingDepartures = live.data
                        lastFetchTime = if (live.isSuccess()) {
                            now
                        } else {
                            now - previous?.age.orElse { Duration.ZERO }
                        }
                    }

                    scheduled?.data != null -> {
                        upcomingDepartures = scheduled.data
                        lastFetchTime = now
                    }

                    else -> {
                        upcomingDepartures = null
                        lastFetchTime = null
                    }
                }

                val alertsResult = withTimeoutCatching(2.seconds) {
                    alerts.await()
                }.toDataResult().flatten()
                    .onSuccess {
                        Logging.d("Fetch$fetchIdLabel: Received alerts ")
                    }
                    .onFailure { error, hadInternet, data ->
                        Logging.w("Fetch$fetchIdLabel: Failed to get alerts", error)
                    }
                    .onLoading {
                        Logging.w("Fetch$fetchIdLabel: Received loading for alerts!")
                    }

                val widgetData = upcomingDepartures?.let { data ->
                    createWidgetData(
                        lastFetchTime ?: now,
                        data,
                        alertsResult.data,
                        stationsByProximity,
                        isPathApiBroken = isPathApiBroken,
                    )
                }

                (this as? Job)?.children?.let {
                    if (it.toList().isNotEmpty()) {
                        Logging.w("Fetch$fetchIdLabel: Ready to return data, but there are " +
                                "ongoing jobs to cancel")
                        it.forEach { it.cancel() }
                    }
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
                                "Fetch$fetchIdLabel: " +
                                        "Re-using previous data since the system denied us " +
                                        "internet " +
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
            withTimeoutCatching(5.seconds) {
                fetch.await()
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

    /**
     * Creates widget display data by processing raw train departures and applying various filters and transformations.
     *
     * This function:
     * 1. Sorts and filters stations based on user preferences and location
     * 2. Processes alerts that may affect train service
     * 3. Groups trains by destination and applies line/interstate filters
     * 4. Adjusts arrival times to help users avoid missing trains
     * 5. Formats data for widget display
     *
     * @param now Current time used for calculations
     * @param fetchTime When the train data was fetched from PATH API
     * @param stationLimit Maximum number of stations to show in widget
     * @param stations List of stations user wants to see
     * @param lines Which PATH train lines to include
     * @param sort How to sort the stations (alphabetical, NJ morning commute, NY morning commute)
     * @param filter Whether to show all trains or only interstate trains
     * @param closestStations Ordered list of stations near user's location (if available)
     * @param alerts Current service alerts that may affect trains
     * @param data Raw train departure data from PATH API
     * @param isPathApiBroken Whether PATH API is currently having issues
     * @return Processed and formatted data ready for widget display
     */
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
        data: UpcomingDepartures,
        isPathApiBroken: Boolean,
    ): DepartureBoardData {
        val adjustedStations = stations.toMutableList()
        val stationDatas = arrayListOf<DepartureBoardData.StationData>()
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
                        DepartureBoardData.SignData(headSign, colors, arrivals)
                    }
                    .sortedBy { it.projectedArrivals.min() }

            val trains = apiTrains
                .asSequence()
                .map {
                    val colors = it.lineColors.distinct()
                    DepartureBoardData.TrainData(
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
                    (train.lines == null || train.lines.orEmpty().any { line -> line in lines })
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

            stationDatas += DepartureBoardData.StationData(
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
        return DepartureBoardData(
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
        train: DepartureBoardData.TrainData,
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
            station == Stations.WorldTradeCenter || destination == Stations.WorldTradeCenter -> true
            // Newport -> Hoboken is the only time an NJ-terminating train travels east.
            destination == Stations.Hoboken -> station.isInNewYork
            station.isInNewYork -> destination.isInNewJersey || destination isWestOf station
            station.isInNewJersey -> destination.isInNewYork || destination isEastOf station
            else -> true // never happens, here for readability
        }
    }

    private fun DepartureBoardData.TrainData.adjustedToAvoidMissingTrains(
        now: Instant,
        avoidMissingTrains: AvoidMissingTrains
    ): DepartureBoardData.TrainData {
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
    fun prunePassedDepartures(data: DepartureBoardData?, time: Instant): DepartureBoardData? {
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
                "Received current location as (${location.latitude},${location.longitude}) in " +
                        "${duration}, closest stations are " +
                        stations.map { it.displayName }
            )
            lastClosestStations = stations
            if (!isDuringFetch) {
                _nonFetchLocationReceived.tryEmit(Unit)
            }
        }
    }
}
