package com.sixbynine.transit.path.api

import com.sixbynine.transit.path.api.templine.HobClosureConfigRepository
import com.sixbynine.transit.path.preferences.IntPersistable
import com.sixbynine.transit.path.time.NewYorkTimeZone
import com.sixbynine.transit.path.time.now
import kotlinx.datetime.toLocalDateTime

enum class Line(override val number: Int) : IntPersistable {
    NewarkWtc(1), HobokenWtc(2), JournalSquare33rd(3), Hoboken33rd(4), Wtc33rd(5);

    companion object {
        val all: List<Line> by lazy {
            entries.filter {
                if (it != Wtc33rd) return@filter true

                val now = now().toLocalDateTime(NewYorkTimeZone)
                val config = HobClosureConfigRepository.getConfig()
                if (config.validFrom != null && now < config.validFrom) return@filter false
                if (config.validTo != null && now > config.validTo) return@filter false

                return@filter true
            }
        }
    }
}
