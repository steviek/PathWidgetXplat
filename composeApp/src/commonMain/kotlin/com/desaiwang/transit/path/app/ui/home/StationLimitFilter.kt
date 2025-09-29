package com.desaiwang.transit.path.app.ui.home

import com.desaiwang.transit.path.app.settings.StationLimit
import com.desaiwang.transit.path.app.settings.StationLimit.Four
import com.desaiwang.transit.path.app.settings.StationLimit.None
import com.desaiwang.transit.path.app.settings.StationLimit.OnePerLine
import com.desaiwang.transit.path.app.settings.StationLimit.Six
import com.desaiwang.transit.path.app.settings.StationLimit.ThreePerLine
import com.desaiwang.transit.path.app.settings.StationLimit.TwoPerLine
import com.desaiwang.transit.path.app.ui.common.AppUiTrainData

class StationLimitFilter(private val stationLimit: StationLimit) {
    fun filter(trainData: List<AppUiTrainData>): List<AppUiTrainData> = when(stationLimit) {
        None -> trainData
        Four -> filterByTotalCount(trainData, 4)
        Six -> filterByTotalCount(trainData, 6)
        OnePerLine -> filterByLineCount(trainData, 1)
        TwoPerLine -> filterByLineCount(trainData, 2)
        ThreePerLine -> filterByLineCount(trainData, 3)
    }

    private fun filterByTotalCount(trainData: List<AppUiTrainData>, count: Int): List<AppUiTrainData> {
        return trainData.take(count)
    }

    private fun filterByLineCount(trainData: List<AppUiTrainData>, max: Int): List<AppUiTrainData> {
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

fun List<AppUiTrainData>.filter(filter: StationLimitFilter): List<AppUiTrainData> {
    return filter.filter(this)
}
