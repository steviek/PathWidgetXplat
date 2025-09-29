package com.desaiwang.transit.path.test

import com.desaiwang.transit.path.preferences.Preferences
import com.desaiwang.transit.path.preferences.PreferencesKey
import com.desaiwang.transit.path.preferences.testInstance

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
