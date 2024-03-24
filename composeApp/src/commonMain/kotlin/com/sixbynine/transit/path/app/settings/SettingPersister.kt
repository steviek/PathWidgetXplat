package com.sixbynine.transit.path.app.settings

import com.sixbynine.transit.path.preferences.IntPersistable
import com.sixbynine.transit.path.preferences.IntPreferencesKey
import com.sixbynine.transit.path.preferences.Preferences
import com.sixbynine.transit.path.preferences.PreferencesKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingPersister<K, T>(
    key: PreferencesKey<K>,
    deserialize: (K) -> T?,
    serialize: (T) -> K,
    defaultValue: T
) {
    private val serializer = PreferencesSerializer(key, deserialize, serialize)

    private val _flow = MutableStateFlow(serializer.get() ?: defaultValue)
    val flow = _flow.asStateFlow()

    fun update(value: T) {
        serializer.set(value)
        _flow.value = value
    }
}

inline fun <reified E> SettingPersister(
    key: IntPreferencesKey,
    defaultValue: E
): SettingPersister<Int, E> where E : Enum<E>, E : IntPersistable {
    return SettingPersister(
        key = key,
        serialize = { it.number },
        deserialize = { IntPersistable.fromPersistence(it) },
        defaultValue = defaultValue
    )
}

inline fun <reified E> SettingPersister(
    key: String,
    defaultValue: E
): SettingPersister<Int, E> where E : Enum<E>, E : IntPersistable {
    return SettingPersister(IntPreferencesKey(key), defaultValue)
}

// This is very extra, but as long as all the numbers are in [0, 32), this works nicely.
inline fun <reified E> BitFlagSettingPersister(
    key: IntPreferencesKey,
    defaultValue: Collection<E>
): SettingPersister<Int, Set<E>> where E : Enum<E>, E : IntPersistable {
    return SettingPersister(
        key = key,
        serialize = { IntPersistable.createBitmask(it) },
        deserialize = { IntPersistable.fromBitmask(it) },
        defaultValue = defaultValue.toSet()
    )
}

inline fun <reified E> BitFlagSettingPersister(
    key: String,
    defaultValue: Collection<E>
): SettingPersister<Int, Set<E>> where E : Enum<E>, E : IntPersistable {
    return BitFlagSettingPersister(IntPreferencesKey(key), defaultValue)
}

inline fun SettingPersister(
    key: String,
    defaultValue: Boolean
): SettingPersister<Int, Boolean> {
    return SettingPersister(
        key = IntPreferencesKey(key),
        serialize = { if (it) 1 else 0 },
        deserialize = { it != 0 },
        defaultValue = defaultValue
    )
}

private class PreferencesSerializer<T, R>(
    private val key: PreferencesKey<T>,
    private val deserialize: (T) -> R?,
    private val serialize: (R) -> T
) {
    private val preferences = Preferences()

    fun set(value: R?) {
        preferences[key] = value?.let(serialize)
    }

    fun get(): R? {
        return preferences[key]?.let(deserialize)
    }
}
