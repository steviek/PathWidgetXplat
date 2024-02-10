package com.sixbynine.transit.path.widget

import platform.Foundation.NSUserDefaults

object NativeWidgetDataStore : WidgetDataStore {

    private val defaults: NSUserDefaults? =
        NSUserDefaults(suiteName = "group.com.sixbynine.transit.path")

    override fun set(key: String, value: String) {
        defaults?.setObject(value, key)
    }

    override fun get(key: String): String? {
        return defaults?.stringForKey(key)
    }

    override fun set(key: String, value: Boolean) {
        defaults?.setBool(value, key)
    }

    override fun set(key: String, value: Long) {
        defaults?.setInteger(value, key)
    }
}

actual fun widgetDataStore(): WidgetDataStore = NativeWidgetDataStore
