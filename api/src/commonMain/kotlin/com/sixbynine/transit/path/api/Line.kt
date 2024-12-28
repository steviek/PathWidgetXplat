package com.sixbynine.transit.path.api

import com.sixbynine.transit.path.preferences.IntPersistable

enum class Line(override val number: Int) : IntPersistable {
    NewarkWtc(1), HobokenWtc(2), JournalSquare33rd(3), Hoboken33rd(4);
}
