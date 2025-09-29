package com.desaiwang.transit.path.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import com.desaiwang.transit.path.app.ui.home.HomeScreenContract.Intent.UpdateNowClicked
import com.desaiwang.transit.path.app.ui.theme.Dimensions
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.path_api_busted
import pathwidgetxplat.composeapp.generated.resources.path_api_busted_name
import pathwidgetxplat.composeapp.generated.resources.update_now
import pathwidgetxplat.composeapp.generated.resources.updating

private val MinHeight = 76.dp

@Composable
fun HomeScreenScope.DepartureBoardFooter() {
    when {
        state.isLoading && state.data != null -> {
            Column(
                modifier = Modifier.heightIn(MinHeight),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(string.updating),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        state.updateFooterText == null -> {}

        state.useColumnForFooter -> {
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
        }

        else -> {
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
    }

    PathBustedErrorBanner()
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
        Text(stringResource(string.update_now))
    }
}

@Composable
private fun HomeScreenScope.PathBustedErrorBanner(modifier: Modifier = Modifier) {
    if (!state.isPathApiBusted) {
        return
    }
    Row(
        modifier.fillMaxWidth().background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 16.dp).padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        val scheduleName = state.scheduleName
        Text(
            text = if (scheduleName == null) {
                stringResource(string.path_api_busted)
            } else {
                stringResource(string.path_api_busted_name, scheduleName)
            },
            color = MaterialTheme.colorScheme.onErrorContainer,
            textAlign = TextAlign.Center,
        )
    }
}
