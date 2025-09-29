package com.desaiwang.transit.path.app.settings

import com.desaiwang.transit.path.preferences.IntPersistable

enum class AvoidMissingTrains(override val number: Int) : IntPersistable {
    Disabled(1), OffPeak(2), Always(3)
}