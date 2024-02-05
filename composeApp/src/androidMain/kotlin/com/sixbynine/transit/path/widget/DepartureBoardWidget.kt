package com.sixbynine.transit.path.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.text.Text

class DepartureBoardWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Text("Hello, world")
        }
    }

}

class DepartureBoardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget get() = DepartureBoardWidget()
}
