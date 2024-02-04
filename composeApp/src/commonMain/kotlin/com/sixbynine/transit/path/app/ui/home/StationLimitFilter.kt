package com.sixbynine.transit.path.app.ui.home

import com.sixbynine.transit.path.app.settings.StationLimit
import com.sixbynine.transit.path.app.settings.StationLimit.Four
import com.sixbynine.transit.path.app.settings.StationLimit.None
import com.sixbynine.transit.path.app.settings.StationLimit.OnePerLine
import com.sixbynine.transit.path.app.settings.StationLimit.Six
import com.sixbynine.transit.path.app.settings.StationLimit.ThreePerLine
import com.sixbynine.transit.path.app.settings.StationLimit.TwoPerLine
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.TrainData

class StationLimitFilter(private val stationLimit: StationLimit) {
    fun filter(trainData: List<TrainData>): List<TrainData> = when(stationLimit) {
        None -> trainData
        Four -> filterByTotalCount(trainData, 4)
        Six -> filterByTotalCount(trainData, 6)
        OnePerLine -> filterByLineCount(trainData, 1)
        TwoPerLine -> filterByLineCount(trainData, 2)
        ThreePerLine -> filterByLineCount(trainData, 3)
    }

    private fun filterByTotalCount(trainData: List<TrainData>, count: Int): List<TrainData> {
        return trainData.take(count)
    }

    private fun filterByLineCount(trainData: List<TrainData>, max: Int): List<TrainData> {
        val headSignToCount = mutableMapOf<String, Int>()
        return trainData.sortedBy { it.projectedArrival }.filter {
            val count = headSignToCount[it.title] ?: 0
            if (count < max) {
                headSignToCount[it.title] = count + 1
                true
            } else {
                false
            }
        }
    }
}

fun List<TrainData>.filter(filter: StationLimitFilter): List<TrainData> {
    return filter.filter(this)
}
