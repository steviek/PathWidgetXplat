package com.desaiwang.transit.path.api

import com.desaiwang.transit.path.preferences.IntPersistable

enum class Line(override val number: Int) : IntPersistable {
    NewarkWtc(1), HobokenWtc(2), JournalSquare33rd(3), Hoboken33rd(4);

    companion object {
        val permanentLines: List<Line> =
            listOf(NewarkWtc, HobokenWtc, JournalSquare33rd, Hoboken33rd)

        val permanentLinesForWtc33rd = listOf(HobokenWtc, JournalSquare33rd)
    }
}
