package com.sixbynine.transit.path.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.MR.strings
import com.sixbynine.transit.path.app.ui.home.HomeScreenContract.Intent.UpdateNowClicked
import com.sixbynine.transit.path.app.ui.theme.Dimensions
import dev.icerock.moko.resources.compose.stringResource

private val MinHeight = 76.dp

@Composable
fun HomeScreenScope.DepartureBoardFooter() {
    if (state.updateFooterText != null) {
        if (state.useColumnForFooter) {
            Column(
                modifier =
                Modifier.fillMaxWidth()
                    .heightIn(MinHeight)
                    .padding(top = 8.dp)
                    .padding(horizontal = Dimensions.gutter(state.isTablet)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                UpdatedText()
                UpdateNowButton()
            }
        } else {
            FlowRow(
                modifier = Modifier
                    .heightIn(MinHeight)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .padding(horizontal = Dimensions.gutter(state.isTablet)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.Center
            ) {
                UpdatedText(modifier = Modifier.align(Alignment.CenterVertically))
                UpdateNowButton(modifier = Modifier.align(Alignment.CenterVertically))
            }
        }
    } else if (state.isLoading && state.data != null) {
        Column(
            modifier = Modifier.heightIn(MinHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(
                modifier = Modifier.padding(8.dp),
                text = stringResource(strings.updating),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun HomeScreenScope.UpdatedText(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = state.updateFooterText.orEmpty(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun HomeScreenScope.UpdateNowButton(modifier: Modifier = Modifier) {
    TextButton(
        modifier = modifier,
        onClick = { onIntent(UpdateNowClicked) }
    ) {
        Text(stringResource(strings.update_now))
    }
}
