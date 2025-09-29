package com.desaiwang.transit.path.widget

import com.desaiwang.transit.path.api.Station

/** Comparator that sorts [Station]s by their display names. */
object StationByDisplayNameComparator : Comparator<Station> {
    override fun compare(first: Station, second: Station): Int {
        // Station names that start with digits should come at the end.
        val firstIsDigit = first.displayName.first().isDigit()
        val secondIsDigit = second.displayName.first().isDigit()
        if (firstIsDigit && !secondIsDigit) {
            return 1
        }

        if (secondIsDigit && !firstIsDigit) {
            return -1
        }

        if (!firstIsDigit) {
            return first.displayName.compareTo(second.displayName)
        }

        // If both the names start with digits, compare by the numerical value. This ensures that i.e.
        // '9th St' comes before '14th St'.
        val firstNumber = first.displayName.takeWhile { it.isDigit() }.toInt()
        val secondNumber = second.displayName.takeWhile { it.isDigit() }.toInt()
        return firstNumber.compareTo(secondNumber)
    }
}
