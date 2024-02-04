package com.sixbynine.transit.path.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.MR.images
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.app.settings.TimeDisplay
import com.sixbynine.transit.path.app.ui.PathBottomSheet
import com.sixbynine.transit.path.app.ui.gutter
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.HomeBackfillSource
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.TrainData
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun HomeScreenScope.BackfillBottomSheet(
    isShown: Boolean,
    station: Station,
    trainData: TrainData,
    source: HomeBackfillSource,
    onDismiss: () -> Unit
) {
    PathBottomSheet(isShown = isShown, onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = gutter()).padding(bottom = 16.dp)) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "Presumed train",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(16.dp))

            TrainBox(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                trainData = trainData.copy(
                    projectedArrival = source.projectedArrival,
                    displayText = source.displayText,
                    backfill = null,
                ),
                station = source.station
            )

            Image(
                painter = painterResource(images.train_track),
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterHorizontally).size(24.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )

            TrainBox(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                trainData = trainData,
                station = station
            )

            Spacer(Modifier.height(16.dp))

            val subtext = buildAnnotatedString {
                append("This train is not yet reported for ")
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append(station.displayName)
                pop()
                append(" by PATH, but is displayed here because a train to ")
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append(trainData.title)
                pop()
                append(" is departing from ")
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append(source.station.displayName)
                pop()
                when (state.timeDisplay) {
                    TimeDisplay.Relative -> {
                        append(" in ")
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                        append(source.displayText)
                        pop()
                    }

                    TimeDisplay.Clock -> {
                        append(" at ")
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                        append(source.displayText)
                        pop()
                    }
                }
                append(".")
            }
            Text(
                subtext,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TrainBox(trainData: TrainData, station: Station, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 12.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = station.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        TrainLineContent(
            trainData,
            textStyle = MaterialTheme.typography.bodyMedium,
            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
            fullWidth = false,
        )
    }
}
