package com.desaiwang.transit.path.widget

import com.desaiwang.transit.path.api.Station
import com.desaiwang.transit.path.api.StationSort
import com.desaiwang.transit.path.api.isInNewJersey
import com.desaiwang.transit.path.api.isInNewYork
import com.desaiwang.transit.path.app.settings.SettingsManager
import com.desaiwang.transit.path.app.settings.isActiveAt
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class StationComparator(
    private val sort: StationSort?,
    private val closestStations: List<Station>?,
    private val now: LocalDateTime =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
) : Comparator<Station> {

    private val commutingConfiguration = SettingsManager.commutingConfiguration.value
    private val isCommutingHome = commutingConfiguration.isActiveAt(now)

    override fun compare(a: Station, b: Station): Int {
        return when (sort) {
            StationSort.Alphabetical, null -> {
                StationByDisplayNameComparator.compare(a, b)
            }

            StationSort.NjAm -> {
                compareByState(a, b, isNjFirst = !isCommutingHome)
            }

            StationSort.NyAm -> {
                compareByState(a, b, isNjFirst = isCommutingHome)
            }

            StationSort.Proximity -> {
                val aIndex = closestStations?.indexOf(a) ?: -1
                val bIndex = closestStations?.indexOf(b) ?: -1
                when {
                    aIndex == -1 && bIndex == -1 -> StationByDisplayNameComparator.compare(a, b)
                    aIndex == -1 -> 1
                    bIndex == -1 -> -1
                    else -> aIndex.compareTo(bIndex)
                }

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
