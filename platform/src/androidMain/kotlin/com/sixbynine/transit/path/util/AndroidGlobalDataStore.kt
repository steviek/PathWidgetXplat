package com.sixbynine.transit.path.util

import android.content.Context
import android.content.SharedPreferences
import com.sixbynine.transit.path.PathApplication

object AndroidGlobalDataStore : GlobalDataStore {
    private val prefs =
        PathApplication.instance.getSharedPreferences("widget_data", Context.MODE_PRIVATE)

    override fun set(key: String, value: String?) {
        prefs.edit {
            if (value == null) {
                remove(key)
            } else {
                putString(key, value)
            }
        }
    }

    override fun getString(key: String): String? {
        return prefs.getString(key, null)
    }

    override fun set(key: String, value: Boolean?) {
        prefs.edit {
            if (value == null) {
                remove(key)
            } else {
                putBoolean(key, value)
            }
        }
    }

    override fun set(key: String, value: Long?) {
        prefs.edit {
            if (value == null) {
                remove(key)
            } else {
                putLong(key, value)
            }
        }
    }

    override fun getLong(key: String): Long? {
        return if (prefs.contains(key)) {
            prefs.getLong(key, 0)
        } else {
            null
        }
    }

    private fun SharedPreferences.edit(block: SharedPreferences.Editor.() -> Unit) {
        edit().apply { block(this) }.apply()
    }
}

actual fun globalDataStore(): GlobalDataStore = AndroidGlobalDataStore
