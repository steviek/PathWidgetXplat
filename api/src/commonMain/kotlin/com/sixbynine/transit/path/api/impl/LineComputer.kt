package com.desaiwang.transit.path.api.impl

import com.desaiwang.transit.path.api.Line
import com.desaiwang.transit.path.api.Line.Hoboken33rd
import com.desaiwang.transit.path.api.Line.HobokenWtc
import com.desaiwang.transit.path.api.Line.JournalSquare33rd
import com.desaiwang.transit.path.api.Line.NewarkWtc
import com.desaiwang.transit.path.model.ColorWrapper
import com.desaiwang.transit.path.model.Colors

/**
 * Computes which PATH train lines are running between stations based on:
 * 1. Train colors displayed in the station
 * 2. Origin and destination station pairs
 * 
 * This is needed because sometimes the same physical train can serve different lines
 * at different times of day, and the color indicators at stations may not always
 * match the actual service pattern.
 */
object LineComputer {

    // Stations on the northern Manhattan portion of PATH (33rd St line)
    private val NyNorthStations = setOf("CHR", "09S", "14S", "23S", "33S")

    /**
     * Determines which PATH train lines could be running between two stations.
     *
     * @param station The station code where the train is currently located
     * @param target The destination station code
     * @param colors The colors shown for this train in the station display
     * @return Set of possible PATH lines this train could be running on
     */
    fun computeLines(
        station: String,
        target: String,
        colors: Collection<ColorWrapper>
    ): Set<Line> {
        val lines = mutableSetOf<Line>()

        // First, make sure we match if there are any matching colors.
        colors.forEach {
            when (it) {
                Colors.NwkWtcSingle -> lines += NewarkWtc
                Colors.HobWtcSingle -> lines += HobokenWtc
                Colors.Hob33sSingle -> lines += Hoboken33rd
                Colors.Jsq33sSingle -> lines += JournalSquare33rd
            }
        }

        // This logic is an attempt to cover the case where schedules have changed to different
        // colors and different ending stations.
        when (station) {
            // Stations that only have one line.
            "NWK", "HAR" -> lines += NewarkWtc

            "JSQ" -> when (target) {
                "NWK", "HAR", "WTC", "EXP" -> lines += NewarkWtc
                "NEW", in NyNorthStations -> lines += JournalSquare33rd
            }

            "GRV" -> when (target) {
                "NWK", "HAR", "WTC", "EXP" -> lines += NewarkWtc
                "NEW", in NyNorthStations -> lines += JournalSquare33rd
                "JSQ" -> lines += listOf(NewarkWtc, JournalSquare33rd)
            }

            "EXP" -> when (target) {
                "NWK", "HAR", "WTC", "EXP", "GRV" -> lines += NewarkWtc
                "NEW", "HOB" -> lines += HobokenWtc
                in NyNorthStations -> lines += Line.permanentLinesForWtc33rd
            }

            "NEW" -> when (target) {
                "EXP", "HOB", "WTC" -> lines += HobokenWtc
                "GRV", "JSQ", in NyNorthStations -> lines += JournalSquare33rd
            }

            "HOB" -> when (target) {
                "EXP", "NEW" -> lines += HobokenWtc
                "GRV", "JSQ" -> lines += listOf(JournalSquare33rd, Hoboken33rd)
                in NyNorthStations -> lines += Hoboken33rd
            }

            "WTC" -> when (target) {
                "EXP" -> lines += listOf(NewarkWtc, HobokenWtc)
                "NWK", "HAR", "JSQ", "GRV" -> lines += NewarkWtc
                "NEW", "HOB" -> lines += HobokenWtc
                in NyNorthStations -> lines += Line.permanentLinesForWtc33rd
            }

            in NyNorthStations -> when (target) {
                in NyNorthStations -> lines += listOf(JournalSquare33rd, Hoboken33rd)
                "HOB" -> lines += Hoboken33rd
                "GRV", "JSQ" -> lines += JournalSquare33rd
                "WTC" -> lines += Line.permanentLinesForWtc33rd
            }
        }

        return lines
    }
}