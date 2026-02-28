package com.sixbynine.transit.path.app.ui.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.sixbynine.transit.path.R
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

@Composable
actual fun IconPainter(icon: IconType): Painter {
    return when (icon) {
        Edit -> painterResource(R.drawable.ic_edit)
        Station -> painterResource(R.drawable.ic_station)
        Filter -> painterResource(R.drawable.ic_filter)
        Sort -> painterResource(R.drawable.ic_sort)
        LayoutOneColumn -> painterResource(R.drawable.ic_one_column)
        ArrowUp -> painterResource(R.drawable.ic_arrow_up)
        ArrowDown -> painterResource(R.drawable.ic_arrow_down)
        Settings -> painterResource(R.drawable.ic_settings)
        Delete -> painterResource(R.drawable.ic_delete)
        Back -> painterResource(R.drawable.ic_arrow_back)
        ExpandDown -> painterResource(R.drawable.ic_down)
        Internet -> painterResource(R.drawable.ic_open_in_new)
    }
}
