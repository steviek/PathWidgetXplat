package com.desaiwang.transit.path.app.settings

import com.desaiwang.transit.path.preferences.IntPersistable

enum class TimeDisplay(override val number: Int) : IntPersistable {
    Relative(1), Clock(2)
}
