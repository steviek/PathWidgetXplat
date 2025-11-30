package com.sixbynine.transit.path.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class Season {
    Spring,
    Summer,
    Fall,
    Winter
}

object SeasonUtils {
    /**
     * Determines the current season based on equinoxes and solstices.
     * Uses the local system timezone for date calculations.
     *
     * Approximate dates:
     * - Spring Equinox: March 20
     * - Summer Solstice: June 21
     * - Fall Equinox: September 22
     * - Winter Solstice: December 21
     */
    fun getSeasonForInstant(instant: Instant): Season {
        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val year = localDate.year

        val springEquinox = LocalDate(year, 3, 20)
        val summerSolstice = LocalDate(year, 6, 21)
        val fallEquinox = LocalDate(year, 9, 22)
        val winterSolstice = LocalDate(year, 12, 21)

        return when {
            localDate >= winterSolstice -> Season.Winter
            localDate >= fallEquinox -> Season.Fall
            localDate >= summerSolstice -> Season.Summer
            localDate >= springEquinox -> Season.Spring
            else -> Season.Winter
        }
    }
}

