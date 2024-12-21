package com.sixbynine.transit.path.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.ExistingWorkPolicy
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
    private val params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val glanceIds = getGlanceIds()
        if (glanceIds.isEmpty()) {
            cancel()
        }

        val canRefreshLocation = params.inputData.getBoolean(CAN_REFRESH_LOCATION, true)

        PathApi.instance
            .getUpcomingDepartures(
                now = now(),
                staleness = Staleness(staleAfter = 30.seconds, invalidAfter = Duration.INFINITE)
            )
            .await()

        AndroidWidgetDataRepository.refreshWidgetData(
            force = false,
            canRefreshLocation = canRefreshLocation,
            isBackgroundUpdate = true,
        )

        return Result.success()
    }

    companion object {
        private const val WORK_TAG = "path_widget_refresh"
        private const val ONE_TIME_WORK_TAG = "path_widget_refresh_one_time"
        private const val CAN_REFRESH_LOCATION = "can_refresh_location"
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
                            .build()
                    )
                    .setInputData(Data.Builder().putBoolean(CAN_REFRESH_LOCATION, false).build())
                    .build()
            workManager.enqueueUniquePeriodicWork(WORK_TAG, KEEP, workRequest)
        }

        suspend fun scheduleOneTime() {
            if (getGlanceIds().isEmpty()) {
                cancel()
                return
            }

            val workManager = WorkManager.getInstance(context)
            val workRequest = OneTimeWorkRequestBuilder<WidgetRefreshWorker>().setInputData(
                Data.Builder()
                    .putBoolean(CAN_REFRESH_LOCATION, true)
                    .build()
            ).build()
            workManager.enqueueUniqueWork(ONE_TIME_WORK_TAG, ExistingWorkPolicy.KEEP, workRequest)
        }

        private fun cancel() {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(WORK_TAG)
            workManager.cancelUniqueWork(ONE_TIME_WORK_TAG)
        }
    }
}
