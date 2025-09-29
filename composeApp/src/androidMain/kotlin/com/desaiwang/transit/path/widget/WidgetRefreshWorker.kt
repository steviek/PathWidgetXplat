package com.desaiwang.transit.path.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType.CONNECTED
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.desaiwang.transit.path.Logging
import com.desaiwang.transit.path.MobilePathApplication
import com.desaiwang.transit.path.api.PathApi
import com.desaiwang.transit.path.time.now
import com.desaiwang.transit.path.util.Staleness
import com.desaiwang.transit.path.util.await
import com.desaiwang.transit.path.util.isSuccess
import com.desaiwang.transit.path.util.onFailure
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class WidgetRefreshWorker(
    context: Context,
    private val params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val glanceIds = getGlanceIds()
        if (glanceIds.isEmpty()) {
            cancel()
        }

        val isBackground = params.inputData.getBoolean(IS_BACKGROUND, true)

        Logging.d("Refresh widget data from worker, isBackground=$isBackground")

        PathApi.instance
            .getUpcomingDepartures(
                now = now(),
                staleness = Staleness(staleAfter = 30.seconds, invalidAfter = Duration.INFINITE)
            )
            .await()
            .onFailure { _, hadInternet, data ->
                if (!hadInternet && data != null && isBackground) {
                    // If android didn't give us internet in the background, just give up for now.
                    return Result.success()
                }
            }
            .also {
                Logging.d("Refresh widget data from worker complete, isBackground=$isBackground, wasSuccess=${it.isSuccess()}")
            }

        AndroidWidgetDataRepository.refreshWidgetData(
            force = false,
            canRefreshLocation = !isBackground,
            isBackgroundUpdate = true,
        )

        return Result.success()
    }

    companion object {
        private const val WORK_TAG = "path_widget_refresh"
        private const val ONE_TIME_WORK_TAG = "path_widget_refresh_one_time"
        private const val IS_BACKGROUND = "is_background"
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
                PeriodicWorkRequestBuilder<WidgetRefreshWorker>(15, TimeUnit.MINUTES)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiresBatteryNotLow(true)
                            .setRequiredNetworkType(CONNECTED)
                            .build()
                    )
                    .setInputData(Data.Builder().putBoolean(IS_BACKGROUND, true).build())
                    .build()
            workManager.enqueueUniquePeriodicWork(WORK_TAG, KEEP, workRequest)
        }

        suspend fun scheduleOneTime() {
            if (getGlanceIds().isEmpty()) {
                cancel()
                return
            }

            val workManager = WorkManager.getInstance(context)
            val workRequest = OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(CONNECTED)
                        .build()
                )
                .setInputData(
                    Data.Builder()
                        .putBoolean(IS_BACKGROUND, false)
                        .build()
                )
                .build()
            workManager.enqueueUniqueWork(ONE_TIME_WORK_TAG, ExistingWorkPolicy.KEEP, workRequest)
        }

        private fun cancel() {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(WORK_TAG)
            workManager.cancelUniqueWork(ONE_TIME_WORK_TAG)
        }
    }
}
