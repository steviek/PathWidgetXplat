package com.desaiwang.transit.path.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.TextAlign
import com.desaiwang.transit.path.MainActivity
import com.desaiwang.transit.path.api.PathApiException
import com.desaiwang.transit.path.util.isFailure
import com.desaiwang.transit.path.widget.glance.GlanceTheme
import com.desaiwang.transit.path.widget.glance.Text
import com.desaiwang.transit.path.widget.glance.stringResource
import com.desaiwang.transit.path.widget.startConfigurationActivityAction
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.complete_widget_setup
import pathwidgetxplat.composeapp.generated.resources.failed_to_fetch
import pathwidgetxplat.composeapp.generated.resources.failed_to_fetch_path_fault

@Composable
fun WidgetContent(state: WidgetState) {
    GlanceTheme {
        Box(
            modifier = GlanceModifier.appWidgetBackground()
                .background(GlanceTheme.colors.background)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val result = state.result
            if (state.needsSetup) {
                SetupView()
            } else if (result.isFailure() && result.data?.stations.orEmpty().isEmpty()) {
                ErrorView(state, isPathApiError = result.error is PathApiException)
            } else {
                MainWidgetContent(state)
            }
        }
    }
}

@Composable
private fun MainWidgetContent(state: WidgetState) {
    val result = state.result
    // This is a workaround for a bug in glance where the
    // things composed after a lazy column don't get shown sometimes?
    Column(GlanceModifier.fillMaxSize()) {
        Spacer(GlanceModifier.defaultWeight())

        WidgetFooter(
            state = state,
            modifier = GlanceModifier.height(WidgetFooterHeight)
        )
    }

    Box(
        GlanceModifier
            .fillMaxSize()
            .padding(bottom = WidgetFooterHeight),
        contentAlignment = Alignment.TopCenter
    ) {
        Spacer(GlanceModifier.fillMaxSize().clickable(MainActivity.createAppWidgetLaunchAction()))

        DepartureBoard(result = result, modifier = GlanceModifier.fillMaxWidth())
    }
}

@Composable
private fun SetupView() {
    Text(
        modifier = GlanceModifier
            .clickable(startConfigurationActivityAction())
            .padding(16.dp),
        text = stringResource(string.complete_widget_setup),
        color = GlanceTheme.colors.primary,
        fontSize = 18.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun ErrorView(state: WidgetState, isPathApiError: Boolean) {
    val text = if (isPathApiError) {
        string.failed_to_fetch_path_fault
    } else {
        string.failed_to_fetch
    }
    Column(GlanceModifier.fillMaxSize()) {
        Text(
            modifier = GlanceModifier.defaultWeight().padding(16.dp),
            text = stringResource(text),
            color = GlanceTheme.colors.error,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        WidgetFooter(
            state = state,
            modifier = GlanceModifier.height(WidgetFooterHeight)
        )
    }
}
