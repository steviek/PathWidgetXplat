package com.sixbynine.transit.path.app.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.sixbynine.transit.path.MR.strings
import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.Line.Hoboken33rd
import com.sixbynine.transit.path.api.Line.HobokenWtc
import com.sixbynine.transit.path.api.Line.JournalSquare33rd
import com.sixbynine.transit.path.api.Line.NewarkWtc
import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.StationSort.Alphabetical
import com.sixbynine.transit.path.api.StationSort.NjAm
import com.sixbynine.transit.path.api.StationSort.NyAm
import com.sixbynine.transit.path.api.TrainFilter
import com.sixbynine.transit.path.api.TrainFilter.All
import com.sixbynine.transit.path.api.TrainFilter.Interstate
import com.sixbynine.transit.path.app.settings.StationLimit
import com.sixbynine.transit.path.app.settings.TimeDisplay
import com.sixbynine.transit.path.app.ui.ColorWrapper
import com.sixbynine.transit.path.app.ui.Colors
import com.sixbynine.transit.path.resources.getString
import com.sixbynine.transit.path.widget.WidgetDataFormatter
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.datetime.Clock.System
import kotlin.time.Duration.Companion.minutes

val StationLimit.displayName: StringResource
    get() = when (this) {
        StationLimit.None -> strings.station_filter_none
        StationLimit.Four -> strings.station_filter_four
        StationLimit.Six -> strings.station_filter_six
        StationLimit.OnePerLine -> strings.station_filter_one_per_line
        StationLimit.TwoPerLine -> strings.station_filter_two_per_line
        StationLimit.ThreePerLine -> strings.station_filter_three_per_line
    }

val StationSort.title: StringResource
    get() = when (this) {
        Alphabetical -> strings.station_order_fixed
        NjAm -> strings.station_order_nj_am_title
        NyAm -> strings.station_order_ny_am_title
    }

val StationSort.subtitle: StringResource?
    get() = when (this) {
        Alphabetical -> null
        NjAm -> strings.station_order_nj_am_subtext
        NyAm -> strings.station_order_ny_am_subtext
    }

val TimeDisplay.title: StringResource
    get() = when (this) {
        TimeDisplay.Relative -> strings.setting_time_display_relative
        TimeDisplay.Clock -> strings.setting_time_display_clock
    }

val TrainFilter.title: StringResource
    get() = when (this) {
        All -> strings.show_all_trains
        Interstate -> strings.show_interstate_trains
    }

val TrainFilter.subtext: StringResource?
    get() = when (this) {
        All -> null
        Interstate -> strings.interstate_explanation
    }

@Composable
fun TimeDisplay.subtitle(): String = when (this) {
    TimeDisplay.Relative -> stringResource(strings.setting_time_display_relative_subtitle)
    TimeDisplay.Clock -> stringResource(
        strings.setting_time_display_clock_subtitle,
        getClockDisplayTimeLabel()
    )
}

val Line.title: String get() = when (this) {
    NewarkWtc -> "Newark ⇆ World Trade Center"
    HobokenWtc -> "Hoboken ⇆ World Trade Center"
    JournalSquare33rd -> "Journal Square ⇆ 33rd Street"
    Hoboken33rd -> "Hoboken ⇆ 33rd Street"
}

val Line.colors: List<ColorWrapper> get() = when (this) {
    NewarkWtc -> Colors.NwkWtc
    HobokenWtc -> Colors.HobWtc
    JournalSquare33rd -> Colors.Jsq33s
    Hoboken33rd -> Colors.Hob33s

}

val Set<Line>.title: String
    @Composable
    get() = remember(this) {
        if (size == Line.entries.size) {
            return@remember getString(strings.lines_show_all)
        }

        joinToString(separator = "\n") { it.title }
    }

@Composable
private fun getClockDisplayTimeLabel(): String {
    return remember {
        WidgetDataFormatter.formatTime(System.now() + 5.minutes)
    }
}
