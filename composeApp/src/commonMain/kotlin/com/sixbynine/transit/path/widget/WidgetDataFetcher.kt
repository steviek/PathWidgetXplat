package com.sixbynine.transit.path.widget

import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.api.DepartureBoardTrain
import com.sixbynine.transit.path.api.PathApi
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.StationFilter
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.isEastOf
import com.sixbynine.transit.path.api.isInNewJersey
import com.sixbynine.transit.path.api.isInNewYork
import com.sixbynine.transit.path.api.isWestOf
import com.sixbynine.transit.path.api.state
import com.sixbynine.transit.path.app.ui.ColorWrapper
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object WidgetDataFetcher {

    private var lastFetchTime: Instant? = null
    private var lastFetch: Deferred<Result<Map<Station, List<DepartureBoardTrain>>>>? = null

    fun fetchWidgetData(
        limit: Int,
        stations: List<Station>,
        sort: StationSort,
        filter: StationFilter,
        force: Boolean,
        onSuccess: (WidgetData) -> Unit,
        onFailure: (WidgetData?) -> Unit,
    ) {
        Logging.initialize()
        GlobalScope.launch {
            val lastFetch = lastFetch
            val lastFetchTime = lastFetchTime
            val now = Clock.System.now()
            val result =
                if (lastFetch != null &&
                    !(lastFetch.isCompleted && lastFetch.getCompleted().isFailure) &&
                    lastFetchTime != null &&
                    lastFetchTime in (now - 30.seconds)..now &&
                    !force
                ) {
                    Napier.d("Reuse existing fetch")
                    lastFetch
                } else {
                    Napier.d("New fetch")
                    GlobalScope.async { PathApi.instance.fetchUpcomingDepartures() }
                        .also {
                            WidgetDataFetcher.lastFetch = it
                            WidgetDataFetcher.lastFetchTime = now
                        }
                }
            result
                .await()
                .onSuccess { onSuccess(createWidgetData(limit, stations, sort, filter, it)) }
                .onFailure {
                    Napier.e("Failed to fetch", it)
                    val lastResults = PathApi.instance.getLastSuccessfulUpcomingDepartures()
                    onFailure(
                        lastResults?.let { createWidgetData(limit, stations, sort, filter, it) }
                    )
                }
        }
    }

    private fun createWidgetData(
        limit: Int,
        stations: List<Station>,
        sort: StationSort,
        filter: StationFilter,
        data: Map<Station, List<DepartureBoardTrain>>
    ): WidgetData {
        val stationDatas = arrayListOf<WidgetData.StationData>()
        val comparator = StationComparator(sort)
        for (station in stations.sortedWith(comparator)) {
            val apiTrains = data[station] ?: continue
            val signs =
                apiTrains
                    .groupBy { it.headsign }
                    .mapNotNull { (headSign, trains) ->
                        val colors =
                            trains.flatMap { it.lineColors }.distinct().map { ColorWrapper(it) }
                        val arrivals = trains.map { it.projectedArrival }.distinct().sorted()
                        if (arrivals.isEmpty()) return@mapNotNull null
                        WidgetData.SignData(
                            headSign,
                            colors,
                            arrivals
                        )
                    }
                    .sortedBy { it.projectedArrivals.min() }

            val trains = apiTrains
                .map {
                    val colors = it.lineColors.distinct().map(::ColorWrapper)
                    WidgetData.TrainData(
                        id = it.headsign + ":" + it.projectedArrival,
                        title = it.headsign,
                        colors = colors,
                        projectedArrival = it.projectedArrival,
                        isDelayed = it.isDelayed,
                        backfillSource = it.backfillSource,
                    )
                }
                .filter { matchesFilter(station, it, filter) }
                .distinctBy { it.title to it.projectedArrival }
                .sortedBy { it.projectedArrival }

            if (signs.isNotEmpty()) {
                stationDatas += WidgetData.StationData(
                    id = station.pathApiName,
                    displayName = station.displayName,
                    signs = signs,
                    trains = trains,
                    state = station.state,
                )
            }
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
            nextFetchTime = nextFetchTime
        )
    }

    private fun matchesFilter(
        station: Station,
        train: WidgetData.TrainData,
        filter: StationFilter
    ): Boolean {
        if (filter == StationFilter.All) return true

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

