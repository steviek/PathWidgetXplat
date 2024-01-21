package com.sixbynine.transit.path.app.filter

import com.sixbynine.transit.path.api.StationFilter
import com.sixbynine.transit.path.api.StationFilter.All
import com.sixbynine.transit.path.preferences.IntPreferencesKey
import com.sixbynine.transit.path.preferences.persisting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object StationFilterManager {
    private val selectedFilterOptionKey = IntPreferencesKey("selected_filter_option")
    private var storedSelectedFilterOptionName by persisting(selectedFilterOptionKey)

    private var storedSelectedFilterOption: StationFilter
        get() {
            val storedValue = storedSelectedFilterOptionName ?: return All
            return StationFilter.entries.find { it.number == storedValue } ?: All
        }
        set(value) {
            storedSelectedFilterOptionName = value.number
        }

    private val _selection = MutableStateFlow(storedSelectedFilterOption)
    val filter = _selection.asStateFlow()

    fun update(selection: StationFilter) {
        storedSelectedFilterOption = selection
        _selection.value = selection
    }
}
