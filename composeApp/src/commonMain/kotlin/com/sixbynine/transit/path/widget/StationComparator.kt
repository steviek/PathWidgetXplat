package com.sixbynine.transit.path.widget

import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.isInNewJersey
import com.sixbynine.transit.path.api.isInNewYork
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class StationComparator(
    private val sort: StationSort?,
    private val now: LocalTime =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time,
) : Comparator<Station> {
    override fun compare(a: Station, b: Station): Int {
        return when (sort) {
            StationSort.Alphabetical, null -> {
                StationByDisplayNameComparator.compare(a, b)
            }
            StationSort.NjAm -> {
                compareByState(a, b, isNjFirst = now.hour < 12)
            }
            StationSort.NyAm -> {
                compareByState(a, b, isNjFirst = now.hour >= 12)
            }
        }
    }

    private fun compareByState(
        first: Station,
        second: Station,
        isNjFirst: Boolean
    ): Int = when {
        first.isInNewJersey && second.isInNewYork -> if (isNjFirst) -1 else 1
        first.isInNewYork && second.isInNewJersey -> if (isNjFirst) 1 else -1
        else -> StationByDisplayNameComparator.compare(first, second)
    }
}
