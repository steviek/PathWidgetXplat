package com.sixbynine.transit.path.api.impl

import com.sixbynine.transit.path.api.Station
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/** Wrapper around an ordered list of station-checkpoint associations for a line. */
// Note: since there are so few stations in each line, it's not actually worth allocating extra
// maps for this, and linear lookup is fine.
data class CheckpointMap(
    private val checkpoints: List<Pair<Station, Duration>>
) {
    val keys: List<Station> = checkpoints.map { it.first }

    fun getPosition(station: Station): Int? {
        return checkpoints.indexOfFirst { it.first == station }.takeIf { it >= 0 }
    }

    fun without(vararg stations: Station): CheckpointMap = filterStations { it !in stations }

    fun only(vararg stations: Station): CheckpointMap = filterStations { it in stations }

    fun filterStations(predicate: (Station) -> Boolean): CheckpointMap {
        var subtractionDelta = 0.minutes
        val newCheckpoints = mutableListOf<Pair<Station, Duration>>()
        for ((station, time) in checkpoints) {
            if (!predicate(station)) continue
            if (newCheckpoints.isEmpty()) {
                subtractionDelta = time
            }
            newCheckpoints.add(station to (time - subtractionDelta))
        }
        return CheckpointMap(newCheckpoints)
    }

    operator fun get(station: Station) = checkpoints.firstOrNull { it.first == station }?.second

    operator fun contains(station: Station) = station in keys

    fun forEach(action: (Station, Duration) -> Unit) {
        checkpoints.forEach { (station, duration) -> action(station, duration) }
    }
}

fun checkpointMapOf(vararg pairs: Pair<Station, Duration>): CheckpointMap {
    return CheckpointMap(pairs.toList())
}