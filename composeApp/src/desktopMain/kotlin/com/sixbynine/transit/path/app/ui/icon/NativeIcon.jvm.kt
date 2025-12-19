package com.sixbynine.transit.path.app.ui.icon

import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
import pathwidgetxplat.composeapp.generated.resources.ic_arrow_down
import pathwidgetxplat.composeapp.generated.resources.ic_arrow_up
import pathwidgetxplat.composeapp.generated.resources.ic_down
import pathwidgetxplat.composeapp.generated.resources.ic_filter
import pathwidgetxplat.composeapp.generated.resources.ic_one_column
import pathwidgetxplat.composeapp.generated.resources.ic_open_in_new
import pathwidgetxplat.composeapp.generated.resources.ic_sort
import pathwidgetxplat.composeapp.generated.resources.ic_station

@Composable
actual fun IconPainter(icon: IconType): Painter {
    return when (icon) {
        Edit -> rememberVectorPainter(Filled.Edit)
        Station -> painterResource(Res.drawable.ic_station)
        Filter -> painterResource(Res.drawable.ic_filter)
        Sort -> painterResource(Res.drawable.ic_sort)
        LayoutOneColumn -> painterResource(Res.drawable.ic_one_column)
        ArrowUp -> painterResource(Res.drawable.ic_arrow_up)
        ArrowDown -> painterResource(Res.drawable.ic_arrow_down)
        Settings -> rememberVectorPainter(Filled.Settings)
        Delete -> rememberVectorPainter(Filled.Delete)
        Back -> rememberVectorPainter(Filled.ArrowBack)
        ExpandDown -> painterResource(Res.drawable.ic_down)
        Internet -> painterResource(Res.drawable.ic_open_in_new)
    }
}
