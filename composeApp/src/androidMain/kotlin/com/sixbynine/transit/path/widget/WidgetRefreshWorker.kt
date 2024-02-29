package com.sixbynine.transit.path.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.NetworkType.UNMETERED
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sixbynine.transit.path.PathApplication
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val glanceIds = getGlanceIds()
        if (glanceIds.isEmpty()) {
            cancel()
        }

        glanceIds.forEach { glanceId ->
            DepartureBoardWidget().update(applicationContext, glanceId)
        }

        return Result.success()
    }

    companion object {
        private const val WORK_TAG = "path_widget_refresh"
        private val context get() = PathApplication.instance
        private val glanceAppWidgetManager get() = GlanceAppWidgetManager(context)

        private suspend fun getGlanceIds() =
            glanceAppWidgetManager.getGlanceIds(DepartureBoardWidget::class.java)

        suspend fun schedule() {
            if (getGlanceIds().isEmpty()) {
                cancel()
                return
            }

            val workManager = WorkManager.getInstance(context)
            val workRequest =
                PeriodicWorkRequestBuilder<WidgetRefreshWorker>(15.minutes.toJavaDuration())
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiresBatteryNotLow(true)
                            .setRequiredNetworkType(UNMETERED)
                            .build()
                    )
                    .build()
            workManager.enqueueUniquePeriodicWork(WORK_TAG, KEEP, workRequest)
        }

        private fun cancel() {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(WORK_TAG)
        }
    }
}
