package com.sixbynine.transit.path.api.templine

import com.sixbynine.transit.path.schedule.LocalDateTimeSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.serialization.Serializable

@Serializable
data class HobClosureConfig(
    val tempLineInfo: TempLineInfo,
    @Serializable(with = LocalDateTimeSerializer::class) val validFrom: LocalDateTime?,
    @Serializable(with = LocalDateTimeSerializer::class) val validTo: LocalDateTime?,
) {
    companion object {
        val fallback get() = HobClosureConfig(
            tempLineInfo = TempLineInfo.fallback,
            validFrom = LocalDateTime(2025, Month.JANUARY, 18, 23, 59),
            validTo = LocalDateTime(2025, Month.FEBRUARY, 28, 5, 0),
        )
    }
}

@Serializable
data class TempLineInfo(
    val displayName: String,
    val codes: List<String>,
    val lightColor: String,
    val darkColor: String?,
) {
    companion object {
        val fallback get() = TempLineInfo(
            displayName = "33rd Street ⇆ World Trade Center",
            codes = listOf("WTC-33", "33-WTC"),
            lightColor = "65C100",
            darkColor = null,
        )
    }
}
