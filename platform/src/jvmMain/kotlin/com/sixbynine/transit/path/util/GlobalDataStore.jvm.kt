package com.sixbynine.transit.path.util

import com.sixbynine.transit.path.preferences.BooleanPreferencesKey
import com.sixbynine.transit.path.preferences.LongPreferencesKey
import com.sixbynine.transit.path.preferences.StringPreferencesKey
import com.sixbynine.transit.path.preferences.createPreferences

actual fun globalDataStore(): GlobalDataStore {
    val preferences = createPreferences()
    return object : GlobalDataStore {
        override fun set(key: String, value: String?) {
            preferences[StringPreferencesKey(key)] = value
        }

        override fun set(key: String, value: Boolean?) {
            preferences[BooleanPreferencesKey(key)] = value
        }

        override fun set(key: String, value: Long?) {
            preferences[LongPreferencesKey(key)] = value
        }

        override fun getString(key: String): String? {
            return preferences[StringPreferencesKey(key)]
        }

        override fun getBoolean(key: String): Boolean? {
            return preferences[BooleanPreferencesKey(key)]
        }

        override fun getLong(key: String): Long? {
            return preferences[LongPreferencesKey(key)]
        }
    }
}
