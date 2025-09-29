package com.desaiwang.transit.path.widget

import com.desaiwang.transit.path.app.settings.TimeDisplay
import com.desaiwang.transit.path.app.settings.TimeDisplay.Clock
import com.desaiwang.transit.path.app.settings.TimeDisplay.Relative
import com.desaiwang.transit.path.app.ui.FontInfo
import com.desaiwang.transit.path.app.ui.SizeWrapper
import com.desaiwang.transit.path.model.ColorWrapper
import com.desaiwang.transit.path.model.DepartureBoardData.SignData
import com.desaiwang.transit.path.model.DepartureBoardData.StationData
import com.desaiwang.transit.path.util.localizedString
import com.desaiwang.transit.path.widget.GroupedWidgetLayoutInfo.SignLayoutInfo
import kotlinx.datetime.Instant

@Suppress("unused") // Called by swift
class GroupedWidgetLayoutHelper(
    private val station: StationData,
    private val displayAt: Instant,
    private val timeDisplay: TimeDisplay,
    private val width: Double,
    private val height: Double,
    private val measure: (text: String, font: FontInfo) -> SizeWrapper,
) {

    fun computeLayoutInfo(): GroupedWidgetLayoutInfo {
        val titleHeight = measureHeight("Hello", Font.StationTitle)
        val stationHeadlineHeight = maxOf(12.0, measureHeight("WTC", Font.SignTitle))
        val arrivalTextHeight = measureHeight("in 10 min", Font.NextArrivalText)

        val signCount = station.signs.size

        val sizeConfiguration = chooseLargestStep(SizeConfiguration.Steps) { step ->
            val subLineCount = if (step.hasSubtext) 1 else 0
            val spacingBelowTitle = step.spacingBelowTitle
            val spacingBelowHeadSign = step.spacingBelowHeadSign
            val spacingBetweenSigns = step.spacingBetweenSigns

            val heightForSigns = height - titleHeight - spacingBelowTitle

            val heightNeededForSign =
                stationHeadlineHeight + subLineCount * arrivalTextHeight + spacingBelowHeadSign
            val totalHeightNeeded =
                heightNeededForSign * signCount + (spacingBetweenSigns * (signCount - 1))

            totalHeightNeeded <= heightForSigns
        }

        val spaceForHeadSign = if (sizeConfiguration.hasSubtext) {
            width - 20 - measureWidth("10 min", Font.NextArrivalText)
        } else {
            0.0
        }

        val signLayoutInfos =
            station
                .signs
                .sortedBy { it.projectedArrivals.minOrNull() }
                .mapNotNull { sign ->
                    val title =
                        WidgetDataFormatter.formatHeadSign(
                            title = sign.title,
                            fits = { measure(it, Font.SignTitle).width <= spaceForHeadSign }
                        )

                    val nextArrivalTime =
                        sign.projectedArrivals.firstOrNull() ?: return@mapNotNull null
                    val nextArrival = when (timeDisplay) {
                        Relative -> {
                            WidgetDataFormatter.formatRelativeTime(displayAt, nextArrivalTime)
                        }

                        Clock -> {
                            WidgetDataFormatter.formatTime(nextArrivalTime)
                        }
                    }

                    val subtext = createSubtext(sizeConfiguration, sign)
                    SignLayoutInfo(
                        title = title,
                        colors = sign.colors,
                        nextArrival = nextArrival,
                        subtext = subtext,
                    )
                }

        return GroupedWidgetLayoutInfo(
            signs = signLayoutInfos,
            spacingBetweenSigns = sizeConfiguration.spacingBetweenSigns.toDouble(),
            lineCountBelowTitle = if (sizeConfiguration.hasSubtext) 1 else 0,
            spacingBelowTitle = sizeConfiguration.spacingBelowTitle.toDouble(),
            spacingBelowHeadSign = sizeConfiguration.spacingBelowHeadSign.toDouble(),
        )
    }

    private fun createSubtext(
        sizeConfiguration: SizeConfiguration,
        sign: SignData,
    ): String? {
        if (!sizeConfiguration.hasSubtext) return null

        val nextArrivals = sign.projectedArrivals.drop(1)
        if (nextArrivals.isEmpty()) {
            return " "
        }

        val timesToText: (List<Instant>) -> String = { times ->
            joinAdditionalTimes(timeDisplay, times, displayAt)
        }

        val steps = (nextArrivals.size downTo 1).toList()
        return chooseWidestText(
            maxWidth = width,
            font = Font.ArrivalLinesText,
            steps = steps
        ) { step ->
            timesToText(nextArrivals.take(step))
        }
    }

    private object Font {
        val StationTitle = FontInfo(14, isBold = true)
        val SignTitle = FontInfo(12, isBold = true)
        val NextArrivalText = FontInfo(12, isBold = true, isMonospacedDigit = true)
        val ArrivalLinesText = FontInfo(11, isMonospacedDigit = true)
    }

    private fun <T> chooseWidestText(
        maxWidth: Double,
        font: FontInfo,
        steps: List<T>,
        stringify: (T) -> String
    ): String {
        val step = chooseLargestStep(steps) { measureWidth(stringify(it), font) <= maxWidth }
        return stringify(step)
    }

    private fun <T> chooseLargestStep(steps: List<T>, fits: (T) -> Boolean): T {
        require(steps.isNotEmpty())
        steps.forEachIndexed { index, step ->
            if (index == steps.lastIndex) {
                return@forEachIndexed
            }

            if (fits(step)) {
                return step
            }
        }
        return steps.last()
    }

    private fun measureHeight(text: String, font: FontInfo): Double {
        return measure(text, font).height
    }

    private fun measureWidth(text: String, font: FontInfo): Double {
        return measure(text, font).width
    }

    companion object {
        private val relSubtextPrefix = localizedString(en = "also in ", es = "y en ")
        private val clockSubtextPrefix = localizedString(en = "also at ", es = "y a las ")

        fun joinAdditionalTimes(
            timeDisplay: TimeDisplay,
            times: List<Instant>,
            displayAt: Instant,
        ) = when (timeDisplay) {
            Relative -> times.joinToString(prefix = relSubtextPrefix, postfix = " min") { time ->
                val minutesToArrival = (time - displayAt).inWholeMinutes
                minutesToArrival.coerceAtLeast(0).toString()
            }

            Clock -> times.joinToString(prefix = clockSubtextPrefix) { time ->
                WidgetDataFormatter.formatTime(time)
            }
        }
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
        val subtext: String?,
    )
}

data class SizeConfiguration(
    val hasSubtext: Boolean,
    val spacingBetweenSigns: Int,
    val spacingBelowTitle: Int,
    val spacingBelowHeadSign: Int = 2
) {
    companion object {
        val Steps = listOf(
            SizeConfiguration(hasSubtext = true, spacingBetweenSigns = 16, spacingBelowTitle = 8),
            SizeConfiguration(hasSubtext = true, spacingBetweenSigns = 12, spacingBelowTitle = 8),
            SizeConfiguration(hasSubtext = true, spacingBetweenSigns = 8, spacingBelowTitle = 8),
            SizeConfiguration(hasSubtext = true, spacingBetweenSigns = 6, spacingBelowTitle = 8),
            SizeConfiguration(hasSubtext = true, spacingBetweenSigns = 6, spacingBelowTitle = 6),
            SizeConfiguration(hasSubtext = true, spacingBetweenSigns = 6, spacingBelowTitle = 4),
            SizeConfiguration(hasSubtext = true, spacingBetweenSigns = 4, spacingBelowTitle = 4),
            SizeConfiguration(hasSubtext = false, spacingBetweenSigns = 16, spacingBelowTitle = 8),
            SizeConfiguration(hasSubtext = false, spacingBetweenSigns = 12, spacingBelowTitle = 8),
            SizeConfiguration(hasSubtext = false, spacingBetweenSigns = 8, spacingBelowTitle = 8),
            SizeConfiguration(hasSubtext = false, spacingBetweenSigns = 6, spacingBelowTitle = 8),
            SizeConfiguration(hasSubtext = false, spacingBetweenSigns = 6, spacingBelowTitle = 6),
            SizeConfiguration(hasSubtext = false, spacingBetweenSigns = 6, spacingBelowTitle = 4),
            SizeConfiguration(hasSubtext = false, spacingBetweenSigns = 4, spacingBelowTitle = 4),
        )
    }
}
