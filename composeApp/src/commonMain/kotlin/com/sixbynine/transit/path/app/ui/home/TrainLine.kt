package com.sixbynine.transit.path.app.ui.home

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
import com.sixbynine.transit.path.app.ui.ColorCircle
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.TrainData

@Composable
fun TrainLineContent(
    data: TrainData,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    fullWidth: Boolean = true,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        ColorCircle(data.colors)
        Spacer(Modifier.width(16.dp))
        Text(
            modifier = Modifier.weight(1f, fill = fullWidth),
            text = data.title,
            style = textStyle,
            color = textColor,
        )
        Spacer(Modifier.width(16.dp))
        Text(
            modifier = Modifier.widthIn(min = 60.dp),
            textAlign = TextAlign.End,
            text = data.displayText,
            style = textStyle,
            color = textColor
        )
    }
}
