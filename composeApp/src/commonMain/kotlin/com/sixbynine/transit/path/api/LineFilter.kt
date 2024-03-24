package com.sixbynine.transit.path.api

import com.sixbynine.transit.path.api.LineFilter.Hoboken33rd
import com.sixbynine.transit.path.api.LineFilter.HobokenWtc
import com.sixbynine.transit.path.api.LineFilter.JournalSquare33rd
import com.sixbynine.transit.path.api.LineFilter.NewarkWtc
import com.sixbynine.transit.path.app.ui.ColorWrapper
import com.sixbynine.transit.path.app.ui.Colors
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract
import com.sixbynine.transit.path.preferences.IntPersistable
import com.sixbynine.transit.path.widget.WidgetData.SignData
import com.sixbynine.transit.path.widget.WidgetData.TrainData

enum class LineFilter(override val number: Int) : IntPersistable {
    NewarkWtc(1), HobokenWtc(2), JournalSquare33rd(3), Hoboken33rd(4);
}

private fun LineFilter.matches(colors: Collection<ColorWrapper>, title: String): Boolean {
    when (this) {
        NewarkWtc -> {
            if (colors.any { it in Colors.NwkWtc }) return true
            return false
        }
        HobokenWtc -> {
            if (colors.any { it in Colors.HobWtc}) return true
            return false
        }
        JournalSquare33rd -> {
            if (colors.any { it in Colors.Jsq33s}) return true
            return false
        }
        Hoboken33rd -> {
            if (colors.any { it in Colors.Hob33s}) return true
            return false
        }
    }
}

fun LineFilter.matches(data: TrainData): Boolean {
    return matches(data.colors, data.title)
}

fun Collection<LineFilter>.anyMatch(data: TrainData): Boolean {
    if (size == LineFilter.entries.size) return true
    return any { it.matches(data) }
}

fun Collection<LineFilter>.anyMatch(data: SignData): Boolean {
    if (size == LineFilter.entries.size) return true
    return any { it.matches(data.colors, data.title) }
}

fun LineFilter.matches(data: HomeScreenContract.TrainData): Boolean {
    return matches(data.colors, data.title)
}
