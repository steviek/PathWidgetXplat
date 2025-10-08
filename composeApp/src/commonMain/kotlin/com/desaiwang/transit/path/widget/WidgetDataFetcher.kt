package com.desaiwang.transit.path.widget

import androidx.compose.ui.graphics.toArgb
import kotlin.native.ObjCName
import kotlin.experimental.ExperimentalObjCName
import com.desaiwang.transit.path.Logging
import com.desaiwang.transit.path.api.Line
import com.desaiwang.transit.path.api.PathApi
import com.desaiwang.transit.path.api.PathApiException
import com.desaiwang.transit.path.api.Station
import com.desaiwang.transit.path.api.StationSort
import com.desaiwang.transit.path.api.Stations
import com.desaiwang.transit.path.api.TrainFilter
import com.desaiwang.transit.path.api.UpcomingDepartures
import com.desaiwang.transit.path.api.alerts.Alert
import com.desaiwang.transit.path.api.alerts.AlertsRepository
import com.desaiwang.transit.path.api.alerts.affectsLines
import com.desaiwang.transit.path.api.alerts.hidesTrainAt
import com.desaiwang.transit.path.api.impl.SchedulePathApi
import com.desaiwang.transit.path.api.isEastOf
import com.desaiwang.transit.path.api.isInNewJersey
import com.desaiwang.transit.path.api.isInNewYork
import com.desaiwang.transit.path.api.isWestOf
import com.desaiwang.transit.path.api.State.NewJersey
import com.desaiwang.transit.path.api.State.NewYork
import com.desaiwang.transit.path.api.state
import com.desaiwang.transit.path.app.lifecycle.AppLifecycleObserver
import com.desaiwang.transit.path.app.settings.AvoidMissingTrains
import com.desaiwang.transit.path.app.settings.AvoidMissingTrains.Always
import com.desaiwang.transit.path.app.settings.AvoidMissingTrains.Disabled
import com.desaiwang.transit.path.app.settings.AvoidMissingTrains.OffPeak
import com.desaiwang.transit.path.app.settings.SettingsManager.currentAvoidMissingTrains
import com.desaiwang.transit.path.location.Location
import com.desaiwang.transit.path.location.LocationCheckResult.Failure
import com.desaiwang.transit.path.location.LocationCheckResult.JustChecked
import com.desaiwang.transit.path.location.LocationCheckResult.NoPermission
import com.desaiwang.transit.path.location.LocationCheckResult.NoProvider
import com.desaiwang.transit.path.location.LocationCheckResult.Success
import com.desaiwang.transit.path.location.LocationProvider
import com.desaiwang.transit.path.model.DepartureBoardData
import com.desaiwang.transit.path.time.NewYorkTimeZone
import com.desaiwang.transit.path.time.now
import com.desaiwang.transit.path.util.AgedValue
import com.desaiwang.transit.path.util.DataResult
import com.desaiwang.transit.path.util.FetchWithPrevious
import com.desaiwang.transit.path.util.Staleness
import com.desaiwang.transit.path.util.flatten
import com.desaiwang.transit.path.util.globalDataStore
import com.desaiwang.transit.path.util.isFailure
import com.desaiwang.transit.path.util.isSuccess
import com.desaiwang.transit.path.util.onFailure
import com.desaiwang.transit.path.util.onLoading
import com.desaiwang.transit.path.util.onSuccess
import com.desaiwang.transit.path.util.orElse
import com.desaiwang.transit.path.util.toDataResult
import com.desaiwang.transit.path.util.withTimeoutCatching
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
    data class LineDirection(
        val line: Line,
        val wantToNY: Boolean
    )

    @ObjCName(name = "getLinesForStationPair") // This exposes the function directly to Swift
    fun getLinesForStationPair(departure: String, destination: String): Set<LineDirection> {
        // Define the main PATH routes
        val nwkWtcRoute = listOf("NWK", "HAR", "JSQ", "GRV", "EXP", "WTC")
        val jsq33sRoute = listOf("JSQ", "GRV", "NEW", "CHR", "09S", "14S", "23S", "33S")
        val hobWtcRoute = listOf("HOB", "NEW", "EXP", "WTC")
        val hob33sRoute = listOf("HOB", "CHR", "09S", "14S", "23S", "33S")
        val jsqHob33sRoute = listOf("JSQ", "GRV", "NEW", "HOB", "CHR", "09S", "14S", "23S", "33S")

        val lineDirections = mutableSetOf<LineDirection>()

        // Check if current time is during late night/weekend hours (11pm-6am or weekends)
        val currentTime = now().toLocalDateTime(NewYorkTimeZone)
        val hour = currentTime.hour
        val dayOfWeek = currentTime.dayOfWeek
        val isLateNightOrWeekend = hour >= 23 || hour < 6 || dayOfWeek == SATURDAY || dayOfWeek == SUNDAY

        // Check NWK-WTC route (valid all times)
        val nwkWtcDep = nwkWtcRoute.indexOf(departure)
        val nwkWtcDest = nwkWtcRoute.indexOf(destination)
        if (nwkWtcDep != -1 && nwkWtcDest != -1) {
            lineDirections.add(LineDirection(
                line = Line.NewarkWtc,
                wantToNY = nwkWtcDep < nwkWtcDest
            ))
        }

        if (isLateNightOrWeekend) {
            // During late night/weekend hours, check JSQ-HOB-33S route
            val jsqHobDep = jsqHob33sRoute.indexOf(departure)
            val jsqHobDest = jsqHob33sRoute.indexOf(destination)
            if (jsqHobDep != -1 && jsqHobDest != -1) {
                lineDirections.add(LineDirection(line = Line.JournalSquare33rd, wantToNY = jsqHobDep < jsqHobDest))
                lineDirections.add(LineDirection(line = Line.Hoboken33rd, wantToNY = jsqHobDep < jsqHobDest))
            }
        } else {
            // During regular hours (6am-11pm, Monday-Friday), check regular routes
            // Check JSQ-33S route
            val jsq33sDep = jsq33sRoute.indexOf(departure)
            val jsq33sDest = jsq33sRoute.indexOf(destination)
            if (jsq33sDep != -1 && jsq33sDest != -1) {
                lineDirections.add(LineDirection(
                    line = Line.JournalSquare33rd,
                    wantToNY = jsq33sDep < jsq33sDest
                ))
            }

            // Check HOB-WTC route
            val hobWtcDep = hobWtcRoute.indexOf(departure)
            val hobWtcDest = hobWtcRoute.indexOf(destination)
            if (hobWtcDep != -1 && hobWtcDest != -1) {
                lineDirections.add(LineDirection(
                    line = Line.HobokenWtc,
                    wantToNY = hobWtcDep < hobWtcDest
                ))
            }

            // Check HOB-33S route
            val hob33sDep = hob33sRoute.indexOf(departure)
            val hob33sDest = hob33sRoute.indexOf(destination)
            if (hob33sDep != -1 && hob33sDest != -1) {
                lineDirections.add(LineDirection(
                    line = Line.Hoboken33rd,
                    wantToNY = hob33sDep < hob33sDest
                ))
            }
        }

        return lineDirections
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

                val alertsResult = withTimeoutCatching(5.seconds) {
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

        //disable sorting (TODO-Desai: delete this and remove sorting all together)
        //look into how NY/NJ evening/morning sort works
        //adjustedStations.sortWith(StationComparator(sort, closestStations))
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

            val filteredTrains = if (adjustedStations.size > 2) {
                // If we have 0 or 1 stations, don't filter by direction
                // but still filter by the provided lines
                apiTrains.filter { train ->
                    train.lines?.any { line -> line in lines } ?: false
                }
            } else {
                // Pre-compute line directions for this station pair
                val lineDirections = getLinesForStationPair(station.pathApiName, adjustedStations.last().pathApiName)
                
                // Filter trains by direction
                apiTrains.filter { train ->
                    val direction = train.directionState
                    if (direction == null) {
                        // No direction state, allow the train
                        true
                    } else {
                        // Check if this train is on any of our lines AND going in the right direction
                        train.lines?.any { line ->
                            lineDirections.any { lineDir ->
                                lineDir.line == line && (
                                    (lineDir.wantToNY && direction == NewYork) ||
                                    (!lineDir.wantToNY && direction == NewJersey)
                                )
                            }
                        } ?: false // No lines on train, filter it out
                    }
                }
            }

            // Process signs from filtered trains, this is grouped by headsign
            val signs = filteredTrains
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

            // Process trains from filtered trains, each train is a single headsign
            val trains = filteredTrains
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
                        directionState = it.directionState,
                    )
                }
                .filter { station == closestStationToUse || matchesFilter(station, it, filter) }
                .map { it.adjustedToAvoidMissingTrains(now, avoidMissingTrains) }
                .distinctBy { it.title to it.projectedArrival }
                .sortedBy { it.projectedArrival }
                .toList()

            Logging.d("For station ${station.displayName}:")
            Logging.d("  Signs count: ${signs.size}")
            Logging.d("  Trains count: ${trains.size}")
            
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
