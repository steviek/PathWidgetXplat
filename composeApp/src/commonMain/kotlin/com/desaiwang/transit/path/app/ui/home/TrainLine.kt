package com.desaiwang.transit.path.app.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.desaiwang.transit.path.app.ui.common.AppUiTrainData

@Composable
fun HomeScreenScope.TrainLineContent(
    data: AppUiTrainData,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    subtitleTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    TrainLineContent(
        data = listOf(data),
        modifier = modifier,
        textStyle = textStyle,
        subtitleTextStyle = subtitleTextStyle,
        textColor = textColor,
    )
}

@Composable
fun HomeScreenScope.TrainLineContent(
    data: List<AppUiTrainData>,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    subtitleTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    com.desaiwang.transit.path.app.ui.common.TrainLineContent(
        data = data,
        timeDisplay = state.timeDisplay,
        modifier = modifier,
        textStyle = textStyle,
        subtitleTextStyle = subtitleTextStyle,
        textColor = textColor,
    )
}
