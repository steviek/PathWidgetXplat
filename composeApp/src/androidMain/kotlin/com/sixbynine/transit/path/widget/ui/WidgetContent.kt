package com.sixbynine.transit.path.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
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
import com.sixbynine.transit.path.MainActivity
import com.sixbynine.transit.path.widget.glance.GlanceTheme
import com.sixbynine.transit.path.widget.glance.Text
import com.sixbynine.transit.path.widget.glance.stringResource
import com.sixbynine.transit.path.widget.startConfigurationActivityAction
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.complete_widget_setup

@Composable
fun WidgetContent(state: WidgetState) {
    GlanceTheme {
        Box(
            modifier = GlanceModifier.appWidgetBackground()
                .background(GlanceTheme.colors.background)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            if (state.needsSetup) {
                SetupView()
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
        Spacer(GlanceModifier.fillMaxSize().clickable(actionStartActivity<MainActivity>()))

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