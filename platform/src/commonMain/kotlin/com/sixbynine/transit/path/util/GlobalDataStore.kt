package com.sixbynine.transit.path.util

/** Like preferences, but shared between the main app and widgets. */
// TODO: Unify with preferences and just have a boolean setting.
interface GlobalDataStore {
    operator fun set(key: String, value: String?)

    fun getString(key: String): String?

    operator fun set(key: String, value: Boolean?)

    fun getBoolean(key: String): Boolean?

    operator fun set(key: String, value: Long?)

    fun getLong(key: String): Long?
}

expect fun globalDataStore(): GlobalDataStore
