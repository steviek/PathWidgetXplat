package com.sixbynine.transit.path.api

import com.sixbynine.transit.path.api.Line.Hoboken33rd
import com.sixbynine.transit.path.api.Line.HobokenWtc
import com.sixbynine.transit.path.api.Line.JournalSquare33rd
import com.sixbynine.transit.path.api.Line.NewarkWtc
import com.sixbynine.transit.path.api.impl.LineComputer
import com.sixbynine.transit.path.app.ui.ColorWrapper
import com.sixbynine.transit.path.app.ui.Colors
import com.sixbynine.transit.path.app.ui.common.AppUiTrainData
import com.sixbynine.transit.path.widget.WidgetData.SignData
import com.sixbynine.transit.path.widget.WidgetData.TrainData

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
