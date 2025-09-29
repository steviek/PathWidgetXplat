package com.desaiwang.transit.path.api

import com.desaiwang.transit.path.preferences.IntPersistable

enum class StationSort(override val number: Int) : IntPersistable {
    Alphabetical(1), NjAm(2), NyAm(3), Proximity(4);

    companion object {
        fun isProximityEnabled(): Boolean {
            return false
        }
    }
}
