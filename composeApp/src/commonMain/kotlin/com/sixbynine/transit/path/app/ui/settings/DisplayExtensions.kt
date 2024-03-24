package com.sixbynine.transit.path.app.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.sixbynine.transit.path.widget.WidgetDataFormatter
import kotlinx.datetime.Clock.System
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.interstate_explanation
import pathwidgetxplat.composeapp.generated.resources.lines_show_all
import pathwidgetxplat.composeapp.generated.resources.setting_time_display_clock
import pathwidgetxplat.composeapp.generated.resources.setting_time_display_clock_subtitle
import pathwidgetxplat.composeapp.generated.resources.setting_time_display_relative
import pathwidgetxplat.composeapp.generated.resources.setting_time_display_relative_subtitle
import pathwidgetxplat.composeapp.generated.resources.show_all_trains
import pathwidgetxplat.composeapp.generated.resources.show_interstate_trains
import pathwidgetxplat.composeapp.generated.resources.station_filter_four
import pathwidgetxplat.composeapp.generated.resources.station_filter_none
import pathwidgetxplat.composeapp.generated.resources.station_filter_one_per_line
import pathwidgetxplat.composeapp.generated.resources.station_filter_six
import pathwidgetxplat.composeapp.generated.resources.station_filter_three_per_line
import pathwidgetxplat.composeapp.generated.resources.station_filter_two_per_line
import pathwidgetxplat.composeapp.generated.resources.station_order_fixed
import pathwidgetxplat.composeapp.generated.resources.station_order_nj_am_subtext
import pathwidgetxplat.composeapp.generated.resources.station_order_nj_am_title
import pathwidgetxplat.composeapp.generated.resources.station_order_ny_am_subtext
import pathwidgetxplat.composeapp.generated.resources.station_order_ny_am_title
import kotlin.time.Duration.Companion.minutes

val StationLimit.displayName: StringResource
    get() = when (this) {
        StationLimit.None -> string.station_filter_none
        StationLimit.Four -> string.station_filter_four
        StationLimit.Six -> string.station_filter_six
        StationLimit.OnePerLine -> string.station_filter_one_per_line
        StationLimit.TwoPerLine -> string.station_filter_two_per_line
        StationLimit.ThreePerLine -> string.station_filter_three_per_line
    }

val StationSort.title: StringResource
    get() = when (this) {
        Alphabetical -> string.station_order_fixed
        NjAm -> string.station_order_nj_am_title
        NyAm -> string.station_order_ny_am_title
    }

val StationSort.subtitle: StringResource?
    get() = when (this) {
        Alphabetical -> null
        NjAm -> string.station_order_nj_am_subtext
        NyAm -> string.station_order_ny_am_subtext
    }

val TimeDisplay.title: StringResource
    get() = when (this) {
        TimeDisplay.Relative -> string.setting_time_display_relative
        TimeDisplay.Clock -> string.setting_time_display_clock
    }

val TrainFilter.title: StringResource
    get() = when (this) {
        All -> string.show_all_trains
        Interstate -> string.show_interstate_trains
    }

val TrainFilter.subtext: StringResource?
    get() = when (this) {
        All -> null
        Interstate -> string.interstate_explanation
    }

@Composable
fun TimeDisplay.subtitle(): String = when (this) {
    TimeDisplay.Relative -> stringResource(string.setting_time_display_relative_subtitle)
    TimeDisplay.Clock -> stringResource(
        string.setting_time_display_clock_subtitle,
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
    get() {
        if (size == Line.entries.size) {
            return stringResource(string.lines_show_all)
        }

        return remember(this) { joinToString(separator = "\n") { it.title } }
    }

@Composable
private fun getClockDisplayTimeLabel(): String {
    return remember {
        WidgetDataFormatter.formatTime(System.now() + 5.minutes)
    }
}
