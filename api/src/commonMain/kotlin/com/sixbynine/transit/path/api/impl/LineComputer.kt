package com.sixbynine.transit.path.api.impl

import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.Line.Hoboken33rd
import com.sixbynine.transit.path.api.Line.HobokenWtc
import com.sixbynine.transit.path.api.Line.JournalSquare33rd
import com.sixbynine.transit.path.api.Line.NewarkWtc
import com.sixbynine.transit.path.model.ColorWrapper
import com.sixbynine.transit.path.model.Colors

object LineComputer {

    private val NyNorthStations = setOf("CHR", "09S", "14S", "23S", "33S")

    fun computeLines(
        station: String,
        target: String,
        colors: Collection<ColorWrapper>
    ): Set<Line> {
        val lines = mutableSetOf<Line>()

        // First, make sure we match if there are any matching colors.
        colors.forEach {
            if (it approxEquals Colors.Wtc33sSingle) {
                lines += Line.permanentLinesForWtc33rd
            }

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