package com.sixbynine.transit.path.api

import com.sixbynine.transit.path.api.Line.Hoboken33rd
import com.sixbynine.transit.path.api.Line.HobokenWtc
import com.sixbynine.transit.path.api.Line.JournalSquare33rd
import com.sixbynine.transit.path.api.Line.NewarkWtc
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

private fun Line.matches(colors: Collection<ColorWrapper>, title: String): Boolean {
    val isHobTempLine = colors.any { it.approxEquals(Colors.Wtc33sSingle) }
    when (this) {
        NewarkWtc -> {
            if (colors.any { it in Colors.NwkWtc }) return true
            return false
        }

        HobokenWtc -> {
            if (colors.any { it in Colors.HobWtc }) return true
            if (isHobTempLine) return true
            return false
        }

        JournalSquare33rd -> {
            if (colors.any { it in Colors.Jsq33s }) return true
            if (isHobTempLine) return true
            return false
        }

        Hoboken33rd -> {
            if (colors.any { it in Colors.Hob33s }) return true
            return false
        }
    }
}

fun Line.matches(data: TrainData): Boolean {
    return matches(data.colors, data.title)
}

fun Collection<Line>.anyMatch(data: TrainData): Boolean {
    if (containsAll(Line.permanentLines)) return true
    return any { it.matches(data) }
}

fun Collection<Line>.anyMatch(data: SignData): Boolean {
    if (containsAll(Line.permanentLines)) return true
    return any { it.matches(data.colors, data.title) }
}

fun Line.matches(data: AppUiTrainData): Boolean {
    return matches(data.colors, data.title)
}
