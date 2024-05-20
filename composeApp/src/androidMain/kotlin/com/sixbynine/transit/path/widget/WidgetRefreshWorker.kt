package com.sixbynine.transit.path.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType.UNMETERED
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sixbynine.transit.path.MobilePathApplication
import com.sixbynine.transit.path.api.PathApi
import com.sixbynine.transit.path.time.now
import com.sixbynine.transit.path.util.Staleness
import com.sixbynine.transit.path.util.await
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
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

        PathApi.instance
            .getUpcomingDepartures(
                now = now(),
                staleness = Staleness(staleAfter = 30.seconds, invalidAfter = Duration.INFINITE)
            )
            .await()

        glanceIds.forEach { glanceId ->
            DepartureBoardWidget().update(applicationContext, glanceId)
        }

        return Result.success()
    }

    companion object {
        private const val WORK_TAG = "path_widget_refresh"
        private const val ONE_TIME_WORK_TAG = "path_widget_refresh_one_time"
        private val context get() = MobilePathApplication.instance
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

        suspend fun scheduleOneTime() {
            if (getGlanceIds().isEmpty()) {
                cancel()
                return
            }

            val workManager = WorkManager.getInstance(context)
            val workRequest = OneTimeWorkRequestBuilder<WidgetRefreshWorker>().build()
            workManager.enqueueUniqueWork(ONE_TIME_WORK_TAG, ExistingWorkPolicy.KEEP, workRequest)
        }

        private fun cancel() {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(WORK_TAG)
            workManager.cancelUniqueWork(ONE_TIME_WORK_TAG)
        }
    }
}
