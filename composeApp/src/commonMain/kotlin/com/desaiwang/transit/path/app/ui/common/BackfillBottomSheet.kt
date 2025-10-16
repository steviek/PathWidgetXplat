package com.desaiwang.transit.path.app.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.desaiwang.transit.path.api.Station
import com.desaiwang.transit.path.app.settings.TimeDisplay
import com.desaiwang.transit.path.app.ui.PathBottomSheet
import com.desaiwang.transit.path.app.ui.gutter
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.drawable
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.language_code
import pathwidgetxplat.composeapp.generated.resources.presumed_train
import pathwidgetxplat.composeapp.generated.resources.train_track

@Composable
fun BackfillBottomSheet(
    isShown: Boolean,
    station: Station,
    trainData: AppUiTrainData,
    source: AppUiBackfillSource,
    timeDisplay: TimeDisplay,
    onDismiss: () -> Unit
) {
    PathBottomSheet(isShown = isShown, onDismissRequest = onDismiss) {
        Column(
            Modifier
                .padding(horizontal = gutter())
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(string.presumed_train),
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
                timeDisplay = timeDisplay,
                station = source.station
            )

            Image(
                painter = painterResource(drawable.train_track),
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterHorizontally).size(24.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )

            TrainBox(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                trainData = trainData,
                timeDisplay = timeDisplay,
                station = station
            )

            Spacer(Modifier.height(16.dp))

            Text(
                createSubtext(station, trainData, source, timeDisplay),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun createSubtext(
    station: Station,
    trainData: AppUiTrainData,
    source: AppUiBackfillSource,
    timeDisplay: TimeDisplay,
): AnnotatedString {
    return when (stringResource(string.language_code)) {
        "es" -> createSpanishSubtext(station, trainData, source, timeDisplay)
        else -> createEnglishSubtext(station, trainData, source, timeDisplay)
    }
}

private fun createEnglishSubtext(
    station: Station,
    trainData: AppUiTrainData,
    source: AppUiBackfillSource,
    timeDisplay: TimeDisplay,
) = buildAnnotatedString {
    append("This train is not yet reported for ")
    appendBolded(station.displayName)
    append(" by PATH, but is displayed here because a train to ")
    appendBolded(trainData.title)
    append(" is departing from ")
    appendBolded(source.station.displayName)
    when (timeDisplay) {
        TimeDisplay.Relative -> {
            append(" in ")
            appendBolded(source.displayText)
        }

        TimeDisplay.Clock -> {
            append(" at ")
            appendBolded(source.displayText)
        }
    }
}

private fun createSpanishSubtext(
    station: Station,
    trainData: AppUiTrainData,
    source: AppUiBackfillSource,
    timeDisplay: TimeDisplay,
) = buildAnnotatedString {
    append("Este tren aún no está reportado por PATH para ")
    appendBolded(station.displayName)
    append(", pero se muestra aquí porque un tren con destino a ")
    appendBolded(trainData.title)
    append(" está saliendo de ")
    appendBolded(source.station.displayName)
    when (timeDisplay) {
        TimeDisplay.Relative -> {
            append(" en ")
            appendBolded(source.displayText)
        }

        TimeDisplay.Clock -> {
            append(" a las ")
            appendBolded(source.displayText)
        }
    }
}

private fun AnnotatedString.Builder.appendBolded(text: String) {
    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
    append(text)
    pop()
}

@Composable
private fun TrainBox(trainData: AppUiTrainData, station: Station, timeDisplay: TimeDisplay, modifier: Modifier = Modifier) {
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
            listOf(trainData),
            timeDisplay = timeDisplay,
            textStyle = MaterialTheme.typography.bodyMedium,
            textColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
