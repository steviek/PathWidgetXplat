package com.sixbynine.transit.path.api

import com.sixbynine.transit.path.preferences.IntPersistable

enum class TrainFilter(override val number: Int) : IntPersistable {
    All(1), Interstate(2)
}
