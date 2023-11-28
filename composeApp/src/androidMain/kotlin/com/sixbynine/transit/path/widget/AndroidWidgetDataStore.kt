package com.sixbynine.transit.path.widget

import android.content.Context
import androidx.core.content.edit
import com.sixbynine.transit.path.PathApplication

object AndroidWidgetDataStore : WidgetDataStore {
    private val prefs =
        PathApplication.instance.getSharedPreferences("widget_data", Context.MODE_PRIVATE)

    override fun set(key: String, value: String) = prefs.edit { putString(key, value) }

    override fun get(key: String): String? {
        return prefs.getString(key, null)
    }

    override fun set(key: String, value: Boolean) = prefs.edit { putBoolean(key, value) }

    override fun set(key: String, value: Long) = prefs.edit { putLong(key, value) }
}

actual fun widgetDataStore(): WidgetDataStore = AndroidWidgetDataStore
