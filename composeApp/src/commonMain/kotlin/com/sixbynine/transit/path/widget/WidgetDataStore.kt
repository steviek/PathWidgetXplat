package com.sixbynine.transit.path.widget

interface WidgetDataStore {
    operator fun set(key: String, value: String)

    operator fun get(key: String): String?

    operator fun set(key: String, value: Boolean)

    operator fun set(key: String, value: Long)
}

expect fun widgetDataStore(): WidgetDataStore