package com.desaiwang.transit.path.app.ui.icon

import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import com.desaiwang.transit.path.R
import com.desaiwang.transit.path.app.ui.icon.IconType.ArrowDown
import com.desaiwang.transit.path.app.ui.icon.IconType.ArrowUp
import com.desaiwang.transit.path.app.ui.icon.IconType.Back
import com.desaiwang.transit.path.app.ui.icon.IconType.Delete
import com.desaiwang.transit.path.app.ui.icon.IconType.Edit
import com.desaiwang.transit.path.app.ui.icon.IconType.ExpandDown
import com.desaiwang.transit.path.app.ui.icon.IconType.Filter
import com.desaiwang.transit.path.app.ui.icon.IconType.Internet
import com.desaiwang.transit.path.app.ui.icon.IconType.LayoutOneColumn
import com.desaiwang.transit.path.app.ui.icon.IconType.Settings
import com.desaiwang.transit.path.app.ui.icon.IconType.Sort
import com.desaiwang.transit.path.app.ui.icon.IconType.Station

@Composable
actual fun IconPainter(icon: IconType): Painter {
    return when (icon) {
        Edit -> rememberVectorPainter(Filled.Edit)
        Station -> painterResource(R.drawable.ic_station)
        Filter -> painterResource(R.drawable.ic_filter)
        Sort -> painterResource(R.drawable.ic_sort)
        LayoutOneColumn -> painterResource(R.drawable.ic_one_column)
        ArrowUp -> painterResource(R.drawable.ic_arrow_up)
        ArrowDown -> painterResource(R.drawable.ic_arrow_down)
        Settings -> rememberVectorPainter(Filled.Settings)
        Delete -> rememberVectorPainter(Filled.Delete)
        Back -> rememberVectorPainter(Filled.ArrowBack)
        ExpandDown -> painterResource(R.drawable.ic_down)
        Internet -> painterResource(R.drawable.ic_open_in_new)
    }
}
