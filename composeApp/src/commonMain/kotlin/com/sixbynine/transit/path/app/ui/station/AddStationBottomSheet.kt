package com.sixbynine.transit.path.app.ui.station

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.app.ui.PathBottomSheet
import com.sixbynine.transit.path.app.ui.gutter
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.StationBottomSheetDismissed
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.StationBottomSheetSelection
import com.sixbynine.transit.path.app.ui.home.HomeScreenScope

@Composable
fun HomeScreenScope.AddStationBottomSheet() {
    PathBottomSheet(
        isShown = state.showAddStationBottomSheet,
        onDismissRequest = { onIntent(StationBottomSheetDismissed) }) {
        state.unselectedStations.forEach {
            Box(modifier = Modifier.fillMaxWidth()
                .height(48.dp)
                .clickable { onIntent(StationBottomSheetSelection(it)) }
                .padding(horizontal = gutter()),
                contentAlignment = Alignment.CenterStart) {
                Text(
                    text = it.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
