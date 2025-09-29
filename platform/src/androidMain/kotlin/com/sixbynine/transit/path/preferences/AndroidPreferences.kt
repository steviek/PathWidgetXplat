@file:Suppress("UNCHECKED_CAST")

package com.desaiwang.transit.path.preferences

import android.content.Context
import android.content.SharedPreferences
import com.desaiwang.transit.path.PathApplication

object AndroidPreferences : Preferences {
    private val preferences: SharedPreferences
        get() = PathApplication.instance.getSharedPreferences("path", Context.MODE_PRIVATE)

    override fun <T> set(key: PreferencesKey<T>, value: T?) {
        preferences.edit()
            .apply {
                if (value == null) {
                    remove(key.key)
                    return@apply
                }

                when (key) {
                    is IntPreferencesKey -> putInt(key.key, value as Int)
                    is LongPreferencesKey -> putLong(key.key, value as Long)
                    is FloatPreferencesKey -> putFloat(key.key, value as Float)
                    is BooleanPreferencesKey -> putBoolean(key.key, value as Boolean)
                    is StringPreferencesKey -> putString(key.key, value as String)
                }
            }
            .apply()
    }

    override fun <T> get(key: PreferencesKey<T>): T? {
        if (!preferences.contains(key.key)) return null
        return when (key) {
            is IntPreferencesKey -> preferences.getInt(key.key, Int.MIN_VALUE)
            is FloatPreferencesKey -> preferences.getFloat(key.key, Float.MIN_VALUE)
            is LongPreferencesKey -> preferences.getLong(key.key, Long.MIN_VALUE)
            is BooleanPreferencesKey -> preferences.getBoolean(key.key, false)
            is StringPreferencesKey -> preferences.getString(key.key, null)
        } as T
    }

    override fun clear() {
        preferences.edit().clear().apply()
    }
}

actual fun createPreferences(): Preferences {
    return testInstance ?: AndroidPreferences
}
