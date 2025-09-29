package com.desaiwang.transit.path.api

import com.desaiwang.transit.path.preferences.IntPersistable

enum class LocationSetting(override val number: Int) : IntPersistable {
    Enabled(1), Disabled(2), EnabledPendingPermission(3);
}
