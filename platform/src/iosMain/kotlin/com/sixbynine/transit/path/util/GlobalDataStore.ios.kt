package com.desaiwang.transit.path.util

import platform.Foundation.NSUserDefaults

object NativeGlobalDataStore : GlobalDataStore {

    private val defaults: NSUserDefaults =
        NSUserDefaults(suiteName = "group.com.desaiwang.transit.path")

    override fun set(key: String, value: String?) {
        if (value == null) {
            remove(key)
            return
        }
        defaults.setObject(value, key)
        defaults.synchronize()
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
        defaults.synchronize()
    }

    override fun getBoolean(key: String): Boolean? {
        if (defaults.objectForKey(key) == null) {
            return null
        }
        return defaults.boolForKey(key)
    }

    override fun set(key: String, value: Long?) {
        if (value == null) {
            remove(key)
            return
        }
        defaults.setInteger(value, key)
        defaults.synchronize()
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
