package com.sixbynine.transit.path.api

import com.sixbynine.transit.path.preferences.IntPersistable

enum class LocationSetting(override val number: Int) : IntPersistable {
    Enabled(1), Disabled(2);
}

val LocationSetting.isEnabled get() = this == LocationSetting.Enabled
