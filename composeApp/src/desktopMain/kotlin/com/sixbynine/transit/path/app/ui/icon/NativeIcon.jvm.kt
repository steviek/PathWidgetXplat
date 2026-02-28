package com.sixbynine.transit.path.app.ui.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.sixbynine.transit.path.app.ui.icon.IconType.ArrowDown
import com.sixbynine.transit.path.app.ui.icon.IconType.ArrowUp
import com.sixbynine.transit.path.app.ui.icon.IconType.Back
import com.sixbynine.transit.path.app.ui.icon.IconType.Delete
import com.sixbynine.transit.path.app.ui.icon.IconType.Edit
import com.sixbynine.transit.path.app.ui.icon.IconType.ExpandDown
import com.sixbynine.transit.path.app.ui.icon.IconType.Filter
import com.sixbynine.transit.path.app.ui.icon.IconType.Internet
import com.sixbynine.transit.path.app.ui.icon.IconType.LayoutOneColumn
import com.sixbynine.transit.path.app.ui.icon.IconType.Settings
import com.sixbynine.transit.path.app.ui.icon.IconType.Sort
import com.sixbynine.transit.path.app.ui.icon.IconType.Station
import org.jetbrains.compose.resources.painterResource
import pathwidgetxplat.composeapp.generated.resources.Res
import pathwidgetxplat.composeapp.generated.resources.ic_arrow_back
import pathwidgetxplat.composeapp.generated.resources.ic_arrow_down
import pathwidgetxplat.composeapp.generated.resources.ic_arrow_up
import pathwidgetxplat.composeapp.generated.resources.ic_delete
import pathwidgetxplat.composeapp.generated.resources.ic_down
import pathwidgetxplat.composeapp.generated.resources.ic_edit
import pathwidgetxplat.composeapp.generated.resources.ic_filter
import pathwidgetxplat.composeapp.generated.resources.ic_one_column
import pathwidgetxplat.composeapp.generated.resources.ic_open_in_new
import pathwidgetxplat.composeapp.generated.resources.ic_settings
import pathwidgetxplat.composeapp.generated.resources.ic_sort
import pathwidgetxplat.composeapp.generated.resources.ic_station

@Composable
actual fun IconPainter(icon: IconType): Painter {
    return when (icon) {
        Edit -> painterResource(Res.drawable.ic_edit)
        Station -> painterResource(Res.drawable.ic_station)
        Filter -> painterResource(Res.drawable.ic_filter)
        Sort -> painterResource(Res.drawable.ic_sort)
        LayoutOneColumn -> painterResource(Res.drawable.ic_one_column)
        ArrowUp -> painterResource(Res.drawable.ic_arrow_up)
        ArrowDown -> painterResource(Res.drawable.ic_arrow_down)
        Settings -> painterResource(Res.drawable.ic_settings)
        Delete -> painterResource(Res.drawable.ic_delete)
        Back -> painterResource(Res.drawable.ic_arrow_back)
        ExpandDown -> painterResource(Res.drawable.ic_down)
        Internet -> painterResource(Res.drawable.ic_open_in_new)
    }
}
