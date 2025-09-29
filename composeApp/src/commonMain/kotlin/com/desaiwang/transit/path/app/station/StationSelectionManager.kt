package com.desaiwang.transit.path.app.station

import com.desaiwang.transit.path.analytics.Analytics
import com.desaiwang.transit.path.api.Station
import com.desaiwang.transit.path.api.Stations
import com.desaiwang.transit.path.api.state
import com.desaiwang.transit.path.preferences.StringPreferencesKey
import com.desaiwang.transit.path.preferences.persistingList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object StationSelectionManager {
    private val DefaultStations = listOf(
        Stations.WorldTradeCenter,
        Stations.ThirtyThirdStreet,
        Stations.Hoboken,
        Stations.JournalSquare,
        Stations.Newark,
    )

    private val selectedStationsKey = StringPreferencesKey("selected_stations")
    private var storedSelectedStationsNames by persistingList(selectedStationsKey)
    private var storedSelectedStations: List<Station>
        get() {
            val storedNames = storedSelectedStationsNames ?: return DefaultStations
            return storedNames.mapNotNull(this::stationFromString)
        }
        set(value) {
            storedSelectedStationsNames = value.map(this::stationToString)
        }

    private val unselectedStationsKey = StringPreferencesKey("unselected_stations")
    private var storedUnselectedStationsNames by persistingList(unselectedStationsKey)
    private var storedUnselectedStations: List<Station>
        get() {
            val storedNames =
                storedUnselectedStationsNames ?: return Stations.All - storedSelectedStations.toSet()
            return storedNames.mapNotNull(this::stationFromString)
        }
        set(value) {
            storedUnselectedStationsNames = value.map(this::stationToString)
        }

    private val _selection =
        MutableStateFlow(StationSelection(storedSelectedStations, storedUnselectedStations))
    val selection = _selection.asStateFlow()

    fun updateSelection(selection: StationSelection) {
        _selection.value = selection
        storedSelectedStations = selection.selectedStations
        storedUnselectedStations = selection.unselectedStations
    }

    private fun updateSelection(block: StationSelection.() -> StationSelection) {
        updateSelection(block(_selection.value))
    }

    fun moveDown(stationId: String, groupedByState: Boolean) {
        updateSelection {
            var origIndex: Int? = null
            var newIndex: Int? = null
            for (i in selectedStations.indices) {
                if (selectedStations[i].pathApiName == stationId) {
                    origIndex = i
                    continue
                }
                if (origIndex == null) continue

                val station = selectedStations[origIndex]

                if (!groupedByState || selectedStations[i].state == station.state) {
                    newIndex = i
                    break
                }
            }

            if (origIndex == null || newIndex == null) return@updateSelection this

            val newSelectedStations = selectedStations.toMutableList()
            val station = newSelectedStations.removeAt(origIndex)
            newSelectedStations.add(newIndex, station)


            copy(selectedStations = newSelectedStations)
        }
    }

    fun moveUp(stationId: String, groupedByState: Boolean) {
        updateSelection {
            var origIndex: Int? = null
            var newIndex: Int? = null
            for (i in selectedStations.indices.reversed()) {
                if (selectedStations[i].pathApiName == stationId) {
                    origIndex = i
                    continue
                }
                if (origIndex == null) continue

                val station = selectedStations[origIndex]

                if (!groupedByState || selectedStations[i].state == station.state) {
                    newIndex = i
                    break
                }
            }

            if (origIndex == null || newIndex == null) return@updateSelection this

            val newSelectedStations = selectedStations.toMutableList()
            val station = newSelectedStations.removeAt(origIndex)
            newSelectedStations.add(newIndex, station)

            copy(selectedStations = newSelectedStations)
        }
    }

    fun remove(stationId: String) {
        updateSelection {
            val newSelectedStations = selectedStations.toMutableList()
            val index = newSelectedStations.indexOfFirst { it.pathApiName == stationId }
            if (index < 0) return@updateSelection this
            val station = newSelectedStations.removeAt(index)
            Analytics.stationRemoved(station)
            copy(
                selectedStations = newSelectedStations,
                unselectedStations = unselectedStations + station
            )
        }
    }

    fun add(stationId: String) {
        updateSelection {
            val newUnselectedStations = unselectedStations.toMutableList()
            val index = newUnselectedStations.indexOfFirst { it.pathApiName == stationId }
            if (index < 0) return@updateSelection this

            val station = newUnselectedStations.removeAt(index)
            Analytics.stationAdded(station)
            copy(
                selectedStations = selectedStations + station,
                unselectedStations = newUnselectedStations
            )
        }
    }

    private fun stationToString(station: Station): String = station.pathApiName
    private fun stationFromString(string: String) = Stations.All.find { it.pathApiName == string }
}