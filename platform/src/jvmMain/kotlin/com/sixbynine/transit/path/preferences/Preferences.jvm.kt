package com.sixbynine.transit.path.preferences

actual fun createPreferences(): Preferences {
    return InMemoryPreferences
}
