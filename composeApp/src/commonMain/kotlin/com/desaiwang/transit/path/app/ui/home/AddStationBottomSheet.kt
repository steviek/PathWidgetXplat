package com.desaiwang.transit.path.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desaiwang.transit.path.api.Station
import com.desaiwang.transit.path.app.ui.PathBottomSheet
import com.desaiwang.transit.path.app.ui.gutter
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Intent.StationBottomSheetDismissed
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Intent.StationBottomSheetSelection

@Composable
fun HomeScreenScope.AddStationBottomSheet() {
    PathBottomSheet(
        isShown = state.showAddStationBottomSheet,
        onDismissRequest = { onIntent(StationBottomSheetDismissed) }
    ) {
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            items(state.unselectedStations, key = { it.pathApiName }) {
                AddStationRow(it)
            }
        }
    }
}

@Composable
private fun HomeScreenScope.AddStationRow(station: Station) {
    Box(modifier = Modifier.fillMaxWidth()
        .height(48.dp)
        .clickable { onIntent(StationBottomSheetSelection(station)) }
        .padding(horizontal = gutter()),
        contentAlignment = Alignment.CenterStart) {
        Text(
            text = station.displayName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
