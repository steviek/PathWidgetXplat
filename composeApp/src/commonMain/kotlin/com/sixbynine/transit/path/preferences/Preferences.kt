package com.sixbynine.transit.path.preferences

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface Preferences {
    operator fun <T> set(key: PreferencesKey<T>, value: T?)

    operator fun <T> get(key: PreferencesKey<T>): T?

    fun clear()
}

expect fun Preferences(): Preferences

sealed interface PreferencesKey<T> {
    val key: String
}

data class IntPreferencesKey(override val key: String) : PreferencesKey<Int>

data class LongPreferencesKey(override val key: String) : PreferencesKey<Long>

data class FloatPreferencesKey(override val key: String) : PreferencesKey<Float>

data class BooleanPreferencesKey(override val key: String) : PreferencesKey<Boolean>

data class StringPreferencesKey(override val key: String) : PreferencesKey<String>

class BooleanPreferenceDelegate(private val key: BooleanPreferencesKey) {
    private val preferences: Preferences = Preferences()

    operator fun getValue(thisRef: Any?, property: Any?): Boolean? {
        return preferences[key]
    }

    operator fun setValue(thisRef: Any?, property: Any?, value: Boolean?) {
        preferences[key] = value
    }
}

fun persisting(
    key: BooleanPreferencesKey
): BooleanPreferenceDelegate = BooleanPreferenceDelegate(key)

class IntPreferenceDelegate(private val key: IntPreferencesKey) {
    private val preferences: Preferences = Preferences()

    operator fun getValue(thisRef: Any?, property: Any?): Int? {
        return preferences[key]
    }

    operator fun setValue(thisRef: Any?, property: Any?, value: Int?) {
        preferences[key] = value
    }
}

fun persisting(key: IntPreferencesKey): IntPreferenceDelegate = IntPreferenceDelegate(key)

// stores a list of strings as a json array
class StringListPreferenceDelegate(private val key: StringPreferencesKey) {
    private val preferences: Preferences = Preferences()

    operator fun getValue(thisRef: Any?, property: Any?): List<String>? {
        return preferences[key]?.let { json -> Json.decodeFromString(json) }
    }

    operator fun setValue(thisRef: Any?, property: Any?, value: List<String>?) {
        if (value == null) {
            preferences[key] = null
            return
        }

        val json = Json.encodeToString(value)
        preferences[key] = json
    }
}

fun persistingList(key: StringPreferencesKey): StringListPreferenceDelegate =
    StringListPreferenceDelegate(key)
