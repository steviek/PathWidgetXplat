package com.sixbynine.transit.path.app.ui.common

import com.sixbynine.transit.path.api.BackfillSource
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.app.settings.TimeDisplay
import com.sixbynine.transit.path.app.settings.TimeDisplay.Relative
import com.sixbynine.transit.path.app.ui.ColorWrapper
import com.sixbynine.transit.path.time.now
import com.sixbynine.transit.path.util.localizedString
import com.sixbynine.transit.path.widget.WidgetData.TrainData
import com.sixbynine.transit.path.widget.WidgetDataFormatter
import kotlinx.datetime.Instant

data class AppUiTrainData(
    val id: String,
    val title: String,
    val colors: List<ColorWrapper>,
    val projectedArrival: Instant,
    val displayText: String,
    val isDelayed: Boolean = false,
    val backfill: AppUiBackfillSource? = null,
) {
    val isBackfilled: Boolean
        get() = backfill != null

    companion object {
        fun create(train: TrainData, timeDisplay: TimeDisplay, now: Instant = now()): AppUiTrainData {
            return AppUiTrainData(
                id = train.id,
                title = train.title,
                colors = train.colors,
                displayText = trainDisplayTime(
                    timeDisplay,
                    isDelayed = train.isDelayed,
                    isBackfilled = train.isBackfilled,
                    train.projectedArrival
                ),
                projectedArrival = train.projectedArrival,
                isDelayed = train.isDelayed,
                backfill = train.backfillSource?.let {
                    AppUiBackfillSource(
                        it,
                        trainDisplayTime(
                            timeDisplay,
                            isDelayed = train.isDelayed,
                            isBackfilled = false,
                            it.projectedArrival,
                            now = now,
                        )
                    )
                },
            )
        }
    }
}

fun TrainData.toAppUiTrainData(timeDisplay: TimeDisplay, now: Instant = now()): AppUiTrainData {
    return AppUiTrainData.create(this, timeDisplay, now)
}

data class AppUiBackfillSource(
    val source: BackfillSource,
    val displayText: String,
) {
    val projectedArrival: Instant
        get() = source.projectedArrival

    val station: Station
        get() = source.station
}

fun trainDisplayTime(
    timeDisplay: TimeDisplay,
    isDelayed: Boolean,
    isBackfilled: Boolean,
    projectedArrival: Instant,
    now: Instant = now(),
): String {
    return with(StringBuilder()) {
        if (isBackfilled) append("~")

        if (isDelayed) append(localizedString(en = "Delayed - ", es = "Retrasado - "))

        val time = when (timeDisplay) {
            Relative -> WidgetDataFormatter.formatRelativeTime(now, projectedArrival)

            TimeDisplay.Clock -> WidgetDataFormatter.formatTime(projectedArrival)
        }
        append(time)

        toString()
    }
}
