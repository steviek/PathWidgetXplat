@file:Suppress("UNCHECKED_CAST")

package com.desaiwang.transit.path.preferences

import platform.Foundation.NSUserDefaults

object IosPreferences : Preferences {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    override fun <T> set(key: PreferencesKey<T>, value: T?) {
        if (value == null) {
            userDefaults.removeObjectForKey(key.key)
            return
        }

        when (key) {
            is IntPreferencesKey -> userDefaults.setInteger((value as Int).toLong(), key.key)
            is LongPreferencesKey -> userDefaults.setInteger(value as Long, key.key)
            is FloatPreferencesKey -> userDefaults.setFloat(value as Float, key.key)
            is BooleanPreferencesKey -> userDefaults.setBool(value as Boolean, key.key)
            is StringPreferencesKey -> userDefaults.setObject(value as String, key.key)
        }
    }

    override fun <T> get(key: PreferencesKey<T>): T? {
        if (userDefaults.objectForKey(key.key) == null) return null
        return when (key) {
            is IntPreferencesKey -> userDefaults.integerForKey(key.key).toInt() as T
            is FloatPreferencesKey -> userDefaults.floatForKey(key.key) as T
            is LongPreferencesKey -> userDefaults.integerForKey(key.key) as T
            is BooleanPreferencesKey -> userDefaults.boolForKey(key.key) as T
            is StringPreferencesKey -> userDefaults.stringForKey(key.key) as T
        }
    }

    override fun clear() {
        userDefaults.dictionaryRepresentation().keys.forEach {
            if (it !is String) return@forEach
            userDefaults.removeObjectForKey(it)
        }
    }
}

actual fun createPreferences(): Preferences = IosPreferences