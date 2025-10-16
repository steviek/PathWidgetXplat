package com.desaiwang.transit.path.app.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.desaiwang.transit.path.api.Station
import com.desaiwang.transit.path.app.settings.TimeDisplay


@Composable
fun TrainLineContentWithWithBackfillBottomSheet(
    data: List<AppUiTrainData>,
    timeDisplay: TimeDisplay,
    station: Station,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    subtitleTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    TrainLineContent(
        data = data,
        timeDisplay = timeDisplay,
        modifier = Modifier.run {
            if (data.firstOrNull()?.isBackfilled == true) {
                clickable { showBottomSheet = true }
            } else {
                this
            }
        }
            .then(modifier)
            .fillMaxWidth(),
        textStyle = textStyle,
        subtitleTextStyle = subtitleTextStyle,
        textColor = textColor,
    )

    val firstTrain = data.firstOrNull()
    val backfill = firstTrain?.backfill
    if (backfill != null) {
        BackfillBottomSheet(
            isShown = showBottomSheet,
            station = station,
            trainData = firstTrain,
            source = backfill,
            onDismiss = { showBottomSheet = false },
            timeDisplay = timeDisplay,
        )
    }
}
