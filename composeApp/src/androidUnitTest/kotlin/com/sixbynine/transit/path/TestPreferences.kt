package com.sixbynine.transit.path

import com.sixbynine.transit.path.preferences.Preferences
import com.sixbynine.transit.path.preferences.PreferencesKey
import com.sixbynine.transit.path.preferences.testInstance

object TestPreferences : Preferences {
    private val data = mutableMapOf<PreferencesKey<*>, Any?>()

    override fun <T> set(key: PreferencesKey<T>, value: T?) {
        data[key] = value
    }

    override fun <T> get(key: PreferencesKey<T>): T? {
        return data[key] as T?
    }

    override fun clear() {
        data.clear()
    }

    fun install() {
        testInstance = this
    }
}
