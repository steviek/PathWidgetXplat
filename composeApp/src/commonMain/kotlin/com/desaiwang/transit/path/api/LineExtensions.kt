package com.desaiwang.transit.path.api

import com.desaiwang.transit.path.api.Line.Hoboken33rd
import com.desaiwang.transit.path.api.Line.HobokenWtc
import com.desaiwang.transit.path.api.Line.JournalSquare33rd
import com.desaiwang.transit.path.api.Line.NewarkWtc
import com.desaiwang.transit.path.api.impl.LineComputer
import com.desaiwang.transit.path.app.ui.common.AppUiTrainData
import com.desaiwang.transit.path.model.ColorWrapper
import com.desaiwang.transit.path.model.Colors
import com.desaiwang.transit.path.model.DepartureBoardData.SignData
import com.desaiwang.transit.path.model.DepartureBoardData.TrainData

val Line.colors: List<ColorWrapper>
    get() = when (this) {
        NewarkWtc -> Colors.NwkWtc
        HobokenWtc -> Colors.HobWtc
        JournalSquare33rd -> Colors.Jsq33s
        Hoboken33rd -> Colors.Hob33s
    }

private fun Line.matches(
    colors: Collection<ColorWrapper>,
    target: String,
    stationId: String
): Boolean {
    return this in LineComputer.computeLines(station = stationId, target = target, colors = colors)
}

fun Line.matches(data: TrainData, stationId: String): Boolean {
    return matches(data.colors, data.title, stationId)
}

fun Collection<Line>.anyMatch(data: TrainData, stationId: String): Boolean {
    if (containsAll(Line.permanentLines)) return true
    return any { it.matches(data, stationId) }
}

fun Collection<Line>.anyMatch(data: SignData, stationId: String): Boolean {
    if (containsAll(Line.permanentLines)) return true
    return any { it.matches(data.colors, data.title, stationId) }
}

fun Line.matches(data: AppUiTrainData, stationId: String): Boolean {
    return matches(data.colors, data.title, stationId)
}
