package com.sixbynine.transit.path.app.settings

import com.sixbynine.transit.path.preferences.IntPersistable

enum class TimeDisplay(override val number: Int) : IntPersistable {
    Relative(1), Clock(2)
}
