package com.desaiwang.transit.path.app.settings

import com.desaiwang.transit.path.preferences.IntPersistable

enum class StationLimit(override val number: Int) : IntPersistable {
    None(1), Four(2), Six(3), OnePerLine(4), TwoPerLine(5), ThreePerLine(6)
}
