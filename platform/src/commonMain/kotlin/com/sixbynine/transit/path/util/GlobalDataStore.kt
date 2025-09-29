package com.desaiwang.transit.path.util

import com.desaiwang.transit.path.preferences.LongPreferencesKey
import com.desaiwang.transit.path.preferences.StringPreferencesKey
import kotlinx.datetime.Instant

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

fun persistingGlobally(key: StringPreferencesKey): StringGlobalDataStoreDelegate =
    StringGlobalDataStoreDelegate(key)

class StringGlobalDataStoreDelegate(private val key: StringPreferencesKey) {
    private val dataStore: GlobalDataStore = globalDataStore()

    operator fun getValue(thisRef: Any?, property: Any?): String? {
        return dataStore.getString(key.key)
    }

    operator fun setValue(thisRef: Any?, property: Any?, value: String?) {
        dataStore[key.key] = value
    }
}

class LongGlobalDataStoreDelegate(private val key: LongPreferencesKey) {
    private val dataStore: GlobalDataStore = globalDataStore()

    operator fun getValue(thisRef: Any?, property: Any?): Long? {
        return dataStore.getLong(key.key)
    }

    operator fun setValue(thisRef: Any?, property: Any?, value: Long?) {
        dataStore[key.key] = value
    }
}

fun persistingGlobally(key: LongPreferencesKey): LongGlobalDataStoreDelegate =
    LongGlobalDataStoreDelegate(key)

class InstantGlobalDataStoreDelegate(key: LongPreferencesKey) {
    private val longDelegate = LongGlobalDataStoreDelegate(key)

    operator fun getValue(thisRef: Any?, property: Any?): Instant? {
        return longDelegate.getValue(thisRef, property)?.let { Instant.fromEpochMilliseconds(it) }
    }

    operator fun setValue(thisRef: Any?, property: Any?, value: Instant?) {
        longDelegate.setValue(thisRef, property, value?.toEpochMilliseconds())
    }
}

fun persistingInstantGlobally(key: String): InstantGlobalDataStoreDelegate =
    InstantGlobalDataStoreDelegate(LongPreferencesKey(key))
