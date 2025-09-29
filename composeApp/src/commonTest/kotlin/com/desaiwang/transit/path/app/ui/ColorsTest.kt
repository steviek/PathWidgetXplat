package com.desaiwang.transit.path.app.ui

import com.desaiwang.transit.path.model.Colors
import com.desaiwang.transit.path.model.Colors.approxEquals
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ColorsTest {
    @Test
    fun approxEquals() {
        val c1 = Colors.parse("#4287f5")
        val c2 = Colors.parse("#3a80f0")
        assertTrue(c1 approxEquals c2)

        val c3 = Colors.parse("#053685")
        assertFalse(c1 approxEquals c3)

        val c4 = Colors.parse("#d00dd6")
        assertFalse(c1 approxEquals c4)
        assertFalse(c3 approxEquals c4)

        val c5 = Colors.parse("#782C94")
        val c6 = Colors.parse("#6b128c")
        assertTrue(c5 approxEquals c6)
        assertFalse(c4 approxEquals c6)

        val c7 = Colors.parse("#19bf32")
        assertFalse(c7 approxEquals c6)
        assertFalse(c7 approxEquals c5)
        assertFalse(c7 approxEquals c4)
        assertFalse(c7 approxEquals c3)
        assertFalse(c7 approxEquals c2)
    }
}
