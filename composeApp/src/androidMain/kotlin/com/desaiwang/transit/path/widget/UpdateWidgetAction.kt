package com.desaiwang.transit.path.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

class UpdateWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        AndroidWidgetDataRepository.refreshWidgetData(
            force = true,
            canRefreshLocation = true,
            isBackgroundUpdate = false
        )
    }
}