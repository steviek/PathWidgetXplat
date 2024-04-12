package com.sixbynine.transit.path.widget

import platform.Foundation.NSUserDefaults

object NativeGlobalDataStore : GlobalDataStore {

    private val defaults: NSUserDefaults =
        NSUserDefaults(suiteName = "group.com.sixbynine.transit.path")

    override fun set(key: String, value: String?) {
        if (value == null) {
            remove(key)
            return
        }
        defaults.setObject(value, key)
    }

    override fun getString(key: String): String? {
        return defaults.stringForKey(key)
    }

    override fun set(key: String, value: Boolean?) {
        if (value == null) {
            remove(key)
            return
        }
        defaults.setBool(value, key)
    }

    override fun set(key: String, value: Long?) {
        if (value == null) {
            remove(key)
            return
        }
        defaults.setInteger(value, key)
    }

    override fun getLong(key: String): Long? {
        if (defaults.objectForKey(key) == null) {
            return null
        }
        return defaults.integerForKey(key)
    }

    private fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }
}

actual fun globalDataStore(): GlobalDataStore = NativeGlobalDataStore
