package com.sixbynine.transit.path.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.sixbynine.transit.path.app.settings.TimeDisplay
import com.sixbynine.transit.path.app.ui.ColorCircle
import com.sixbynine.transit.path.app.ui.ColorWrapper
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.TrainData
import com.sixbynine.transit.path.time.now
import com.sixbynine.transit.path.widget.GroupedWidgetLayoutHelper

@Composable
fun HomeScreenScope.TrainLineContent(
    data: TrainData,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    subtitleTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    fullWidth: Boolean = true,
) {
    TrainLineContent(
        data = listOf(data),
        modifier = modifier,
        textStyle = textStyle,
        subtitleTextStyle = subtitleTextStyle,
        textColor = textColor,
        fullWidth = fullWidth,
    )
}

@Composable
fun HomeScreenScope.TrainLineContent(
    data: List<TrainData>,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    subtitleTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    fullWidth: Boolean = true,
) {
    TrainLineContent(
        data = data,
        timeDisplay = state.timeDisplay,
        modifier = modifier,
        textStyle = textStyle,
        subtitleTextStyle = subtitleTextStyle,
        textColor = textColor,
        fullWidth = fullWidth,
    )
}


@Composable
fun TrainLineContent(
    data: List<TrainData>,
    timeDisplay: TimeDisplay,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    subtitleTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    fullWidth: Boolean = true,
) {
    val colors = ArrayList<ColorWrapper>(2)
    data.fastForEach {
        it.colors.fastForEach { color ->
            if (color !in colors) {
                colors.add(color)
            }
        }
    }
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ColorCircle(colors)
            Spacer(Modifier.width(16.dp))
            Text(
                modifier = Modifier.weight(1f, fill = fullWidth),
                text = data.firstOrNull()?.title.orEmpty(),
                style = textStyle,
                color = textColor,
            )
            Spacer(Modifier.width(16.dp))
            Text(
                modifier = Modifier.widthIn(min = 60.dp),
                textAlign = TextAlign.End,
                text = data.firstOrNull()?.displayText.orEmpty(),
                style = textStyle,
                color = textColor
            )
        }

        if (data.size > 1) {
            val additionalTimes = data.drop(1).map { it.projectedArrival }
            Text(
                text = GroupedWidgetLayoutHelper.joinAdditionalTimes(
                    timeDisplay,
                    additionalTimes,
                    now()
                ),
                style = subtitleTextStyle,
                color = textColor,
            )
        }
    }
}
