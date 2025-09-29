package com.desaiwang.transit.path.app.settings

import com.desaiwang.transit.path.preferences.IntPersistable
import com.desaiwang.transit.path.preferences.IntPreferencesKey
import com.desaiwang.transit.path.preferences.Preferences
import com.desaiwang.transit.path.preferences.PreferencesKey
import com.desaiwang.transit.path.util.globalDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.enums.EnumEntries
import kotlin.enums.enumEntries

class SettingPersister<T>(
    defaultValue: T,
    private val serializer: StorageSerializer<T>
) {
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
): SettingPersister<E> where E : Enum<E>, E : IntPersistable {
    val serialize: (E) -> Int = { it.number }
    val deserialize: (Int) -> E? = { IntPersistable.fromPersistence(it) }
    return SettingPersister(
        defaultValue = defaultValue,
        serializer = PreferencesSerializer(key, deserialize, serialize)
    )
}

inline fun <reified E> SettingPersister(
    key: String,
    defaultValue: E
): SettingPersister<E> where E : Enum<E>, E : IntPersistable {
    return SettingPersister(IntPreferencesKey(key), defaultValue)
}

// This is very extra, but as long as all the numbers are in [0, 32), this works nicely.
inline fun <reified E> BitFlagSettingPersister(
    key: IntPreferencesKey,
    defaultValue: Collection<E>
): SettingPersister<Set<E>> where E : Enum<E>, E : IntPersistable {
    val serialize: (Set<E>) -> Int = { IntPersistable.createBitmask(it) }
    val deserialize: (Int) -> Set<E> = { IntPersistable.fromBitmask(it) }
    return SettingPersister(
        defaultValue = defaultValue.toSet(),
        serializer = PreferencesSerializer(key, deserialize, serialize)
    )
}

inline fun <reified E> BitFlagSettingPersister(
    key: String,
    defaultValue: Collection<E>
): SettingPersister<Set<E>> where E : Enum<E>, E : IntPersistable {
    return BitFlagSettingPersister(IntPreferencesKey(key), defaultValue)
}

inline fun GlobalSettingPersister(
    key: String,
    defaultValue: Boolean
): SettingPersister<Boolean> {
    return SettingPersister(
        defaultValue,
        serializer = BooleanGlobalDataStoreSerializer(key)
    )
}

inline fun <reified E> GlobalSettingPersister(
    key: String,
    defaultValue: E
): SettingPersister<E> where E : Enum<E>, E : IntPersistable {
    return SettingPersister(
        defaultValue,
        serializer = IntGlobalDataStoreSerializer(key, enumEntries())
    )
}

inline fun <reified T : Any> GlobalSettingPersister(
    key: String,
    defaultValue: T,
    crossinline toString: (T) -> String,
    crossinline fromString: (String) -> T,
): SettingPersister<T> {
    return SettingPersister(
        defaultValue,
        serializer = GlobalDataStoreSerializer(key, { toString(it) }, { fromString(it) })
    )
}

inline fun SettingPersister(
    key: String,
    defaultValue: Boolean
): SettingPersister<Boolean> {
    val serialize: (Boolean) -> Int = { if (it) 1 else 0 }
    val deserialize: (Int) -> Boolean = { it == 1 }
    val prefKey = IntPreferencesKey(key)
    return SettingPersister(
        defaultValue = defaultValue,
        serializer = PreferencesSerializer(prefKey, deserialize, serialize)
    )
}

interface StorageSerializer<R> {
    fun set(value: R?)
    fun get(): R?
}

class PreferencesSerializer<T, R>(
    private val key: PreferencesKey<T>,
    private val deserialize: (T) -> R?,
    private val serialize: (R) -> T
) : StorageSerializer<R> {
    private val preferences = Preferences()

    override fun set(value: R?) {
        preferences[key] = value?.let(serialize)
    }

    override fun get(): R? {
        return preferences[key]?.let(deserialize)
    }
}

class IntGlobalDataStoreSerializer<R>(
    private val key: String,
    private val enumEntries: EnumEntries<R>
) : StorageSerializer<R> where R : Enum<R>, R : IntPersistable {
    private val dataStore = globalDataStore()

    override fun set(value: R?) {
        dataStore[key] = value?.number?.toLong()
    }

    override fun get(): R? {
        val number = dataStore.getLong(key)?.toInt() ?: return null
        return IntPersistable.fromPersistence(number, enumEntries)
    }
}

class BooleanGlobalDataStoreSerializer(private val key: String) : StorageSerializer<Boolean> {
    private val dataStore = globalDataStore()

    override fun set(value: Boolean?) {
        dataStore[key] = value
    }

    override fun get(): Boolean? {
        return dataStore.getBoolean(key)
    }
}

class GlobalDataStoreSerializer<T : Any>(
    private val key: String,
    private val toString: (T) -> String,
    private val fromString: (String) -> T,
) : StorageSerializer<T> {
    private val dataStore = globalDataStore()

    override fun set(value: T?) {
        dataStore[key] = value?.let(toString)
    }

    override fun get(): T? {
        return dataStore.getString(key)?.let(fromString)
    }
}
