package com.sixbynine.transit.path.widget.ui

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import com.sixbynine.transit.path.widget.glance.GlanceTheme

@Composable
fun WidgetContent(state: WidgetState) {
    val result = state.result
    GlanceTheme {
        Box(
            modifier = GlanceModifier.appWidgetBackground()
                .background(GlanceTheme.colors.background)
                .fillMaxSize()
        ) {
            // This is a workaround for a bug in glance where the
            // things composed after a lazy column don't get shown sometimes?
            Column(GlanceModifier.fillMaxSize()) {
                Spacer(GlanceModifier.defaultWeight())

                WidgetFooter(
                    state = state,
                    modifier = GlanceModifier.height(WidgetFooterHeight)
                )
            }

            Box(GlanceModifier.padding(bottom = WidgetFooterHeight)) {
                DepartureBoard(result = result, modifier = GlanceModifier.fillMaxSize())
            }
        }
    }
}