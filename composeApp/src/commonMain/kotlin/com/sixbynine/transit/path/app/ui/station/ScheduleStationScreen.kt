package com.sixbynine.transit.path.app.ui.station

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.sixbynine.transit.path.app.ui.ColorCircle
import com.sixbynine.transit.path.app.ui.ColorWrapper

@Composable
fun StationScope.ScheduleStationScreen() {
    val titleToTrain = state.scheduledTrains.groupBy { it.title }
    Column {
        Text(
            state.scheduleName.orEmpty(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )


        LazyColumn(Modifier.fillMaxSize()) {
            titleToTrain.forEach { (title, trains) ->
                val colors = arrayListOf<ColorWrapper>()
                trains.fastForEach {
                    it.colors.forEach { color ->
                        if (color !in colors) {
                            colors.add(color)
                        }
                    }
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        ColorCircle(colors)
                        Spacer(Modifier.width(16.dp))
                        Text(
                            modifier = Modifier.weight(1f),
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        trains.fastForEach {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "00:00",
                                    modifier = Modifier.semantics { contentDescription = "" }
                                        .alpha(0f)
                                )
                                Text(it.displayText)
                            }
                        }
                    }

                }
            }
        }
    }
}