package com.sixbynine.transit.path.widget

import com.sixbynine.transit.path.app.settings.TimeDisplay
import com.sixbynine.transit.path.app.settings.TimeDisplay.Clock
import com.sixbynine.transit.path.app.settings.TimeDisplay.Relative
import com.sixbynine.transit.path.app.ui.ColorWrapper
import com.sixbynine.transit.path.app.ui.FontInfo
import com.sixbynine.transit.path.app.ui.SizeWrapper
import com.sixbynine.transit.path.widget.GroupedWidgetLayoutInfo.SignLayoutInfo
import com.sixbynine.transit.path.widget.WidgetData.StationData
import kotlinx.datetime.Instant

object GroupedWidgetLayoutHelper {
    fun computeLayoutInfo(
        station: StationData,
        displayAt: Instant,
        timeDisplay: TimeDisplay,
        width: Double,
        height: Double,
        measure: (text: String, font: FontInfo) -> SizeWrapper,
    ): GroupedWidgetLayoutInfo {
        fun measureHeight(text: String, font: FontInfo): Double {
            return measure(text, font).height
        }

        fun measureWidth(text: String, font: FontInfo): Double {
            return measure(text, font).width
        }

        val titleHeight = measureHeight("Hello", Font.StationTitle)
        // Title plus some spacing below it
        val stationHeadlineHeight = maxOf(12.0, measureHeight("WTC", Font.SignTitle))
        val arrivalTextHeight = measureHeight("in 10 min", Font.NextArrivalText)

        val signCount = station.signs.size
        var subLineCount = 1
        var spacingBetweenSigns = 16
        var spacingBelowTitle = 8
        val spacingBelowHeadSign = 2
        while (subLineCount > 0 || spacingBetweenSigns > 4 || spacingBelowTitle > 4) {
            val heightForSigns = height - titleHeight - spacingBelowTitle

            val heightNeededForSign =
                stationHeadlineHeight + subLineCount * arrivalTextHeight + spacingBelowHeadSign
            val totalHeightNeeded =
                heightNeededForSign * signCount + (spacingBetweenSigns * (signCount - 1))
            if (totalHeightNeeded <= heightForSigns) {
                // If this fits, let's go with this...
                break
            }

            // Otherwise, shrink something to try for the next level down.
            when {
                spacingBetweenSigns > 12 -> spacingBetweenSigns = 12
                spacingBetweenSigns > 10 -> spacingBetweenSigns = 10
                spacingBetweenSigns > 8 -> spacingBetweenSigns = 8
                spacingBetweenSigns > 6 -> spacingBetweenSigns = 6
                spacingBelowTitle > 6 -> spacingBelowTitle = 6
                spacingBelowTitle > 4 -> spacingBelowTitle = 4
                spacingBetweenSigns > 4 -> spacingBetweenSigns = 4
                else -> {
                    spacingBetweenSigns = 16
                    spacingBelowTitle = 8
                    subLineCount--
                }
            }
        }

        val spaceForHeadSign =
            if (subLineCount > 0) {
                width - 20 - measureWidth("10 min", Font.NextArrivalText)
            } else {
                0.0
            }


        val signLayoutInfos =
            station
                .signs
                .sortedBy { it.projectedArrivals.minOrNull() }
                .map { sign ->
                    val title = WidgetDataFormatter.formatHeadSign(
                        title = sign.title,
                        fits = {
                            measure(it, Font.SignTitle).width <= spaceForHeadSign
                        }
                    )

                    val firstArrival: String
                    val formattedArrivalTimes = when (timeDisplay) {
                        Relative -> run {
                            val availableWidth = when (subLineCount) {
                                0 -> width - 20 - measureWidth(title, Font.SignTitle)
                                1 -> width
                                else -> width * subLineCount * 0.9
                            }
                            firstArrival =
                                sign.projectedArrivals.firstOrNull()?.let { time ->
                                    WidgetDataFormatter.formatRelativeTime(
                                        displayAt,
                                        time
                                    )
                                }.orEmpty()


                            for (i in sign.projectedArrivals.size downTo 2) {
                                val formattedTimes =
                                    sign.projectedArrivals.take(i).drop(1).joinToString(
                                        prefix = "also in ",
                                        postfix = " min",
                                    ) { time ->
                                        val minutesToArrival = (time - displayAt).inWholeMinutes
                                        minutesToArrival.coerceAtLeast(0).toString()
                                    }
                                if (measureWidth(
                                        formattedTimes,
                                        Font.NextArrivalText
                                    ) <= availableWidth
                                ) {
                                    return@run formattedTimes
                                }
                            }

                            firstArrival
                        }

                        Clock -> {
                            firstArrival = sign.projectedArrivals.firstOrNull()
                                ?.let { WidgetDataFormatter.formatTime(it) }.orEmpty()
                            sign.projectedArrivals.joinToString {
                                WidgetDataFormatter.formatTime(it)
                            }
                        }
                    }

                    SignLayoutInfo(
                        title = title,
                        colors = sign.colors,
                        nextArrival = firstArrival,
                        formattedArrivalTimes = formattedArrivalTimes,
                    )
                }

        return GroupedWidgetLayoutInfo(
            signs = signLayoutInfos,
            spacingBetweenSigns = spacingBetweenSigns.toDouble(),
            lineCountBelowTitle = subLineCount,
            spacingBelowTitle = spacingBelowTitle.toDouble(),
            spacingBelowHeadSign = spacingBelowHeadSign.toDouble(),
        )
    }

    private object Font {
        val StationTitle = FontInfo(14, isBold = true)
        val SignTitle = FontInfo(12, isBold = true)
        val NextArrivalText = FontInfo(12, isBold = true, isMonospacedDigit = true)
        val ArrivalLinesText = FontInfo(11, isMonospacedDigit = true)
    }
}

data class GroupedWidgetLayoutInfo(
    val signs: List<SignLayoutInfo>,
    val spacingBetweenSigns: Double,
    val lineCountBelowTitle: Int,
    val spacingBelowTitle: Double,
    val spacingBelowHeadSign: Double,
) {
    data class SignLayoutInfo(
        val title: String,
        val colors: List<ColorWrapper>,
        val nextArrival: String,
        val formattedArrivalTimes: String,
    )
}

data class RelativeTimesFormatConfiguration(val postfix: String, val separator: String) {
    companion object {
        val WiderOptions = listOf(
            RelativeTimesFormatConfiguration(" min", ", "),
            RelativeTimesFormatConfiguration(" min", ","),
            RelativeTimesFormatConfiguration("", ", "),
        )

        val Shortest = RelativeTimesFormatConfiguration("", ",")
    }
}

private fun formatRelativeTimes(
    option: RelativeTimesFormatConfiguration,
    displayAt: Instant,
    times: List<Instant>
): String {
    return times.joinToString(
        postfix = option.postfix,
        separator = option.separator,
    ) { time ->
        val minutesToArrival = (time - displayAt).inWholeMinutes
        minutesToArrival.coerceAtLeast(0).toString()
    }
}
