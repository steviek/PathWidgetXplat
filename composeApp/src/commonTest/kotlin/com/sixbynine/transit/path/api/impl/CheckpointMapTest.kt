package com.sixbynine.transit.path.api.impl

import com.sixbynine.transit.path.api.Stations.Harrison
import com.sixbynine.transit.path.api.Stations.Hoboken
import com.sixbynine.transit.path.api.Stations.JournalSquare
import com.sixbynine.transit.path.api.Stations.Newark
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes

class CheckpointMapTest {

    @Test
    fun `basic functions`() {
        assertEquals(0.minutes, Map[Newark])
        assertEquals(1.minutes, Map[Harrison])
        assertEquals(2.minutes, Map[JournalSquare])
        assertNull(Map[Hoboken])

        assertEquals(0, Map.getPosition(Newark))
        assertEquals(1, Map.getPosition(Harrison))
        assertEquals(2, Map.getPosition(JournalSquare))
        assertNull(Map.getPosition(Hoboken))
    }

    @Test
    fun `filtering middle`() {
        val filtered = Map.without(Harrison)

        assertEquals(checkpointMapOf(Newark to 0.minutes, JournalSquare to 2.minutes), filtered)
    }

    @Test
    fun `filtering start`() {
        val filtered = Map.without(Newark)

        assertEquals(checkpointMapOf(Harrison to 0.minutes, JournalSquare to 1.minutes), filtered)
    }

    companion object {
        val Map =
            checkpointMapOf(Newark to 0.minutes, Harrison to 1.minutes, JournalSquare to 2.minutes)
    }
}