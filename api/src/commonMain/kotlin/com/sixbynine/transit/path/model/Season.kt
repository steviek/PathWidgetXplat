package com.sixbynine.transit.path.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.Month.DECEMBER
import kotlinx.datetime.Month.JUNE
import kotlinx.datetime.Month.MARCH
import kotlinx.datetime.Month.SEPTEMBER
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

        val springEquinox = LocalDate(year, MARCH, 20)
        val summerSolstice = LocalDate(year, JUNE, 21)
        val fallEquinox = LocalDate(year, SEPTEMBER, 22)
        val winterSolstice = LocalDate(year, DECEMBER, 21)

        return when {
            localDate >= winterSolstice -> Season.Winter
            localDate >= fallEquinox -> Season.Fall
            localDate >= summerSolstice -> Season.Summer
            localDate >= springEquinox -> Season.Spring
            else -> Season.Winter
        }
    }
}

