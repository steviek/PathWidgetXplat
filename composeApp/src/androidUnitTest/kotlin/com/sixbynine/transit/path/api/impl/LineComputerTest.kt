package com.sixbynine.transit.path.api.impl

import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.Line.HobokenWtc
import com.sixbynine.transit.path.api.Line.JournalSquare33rd
import com.sixbynine.transit.path.model.Colors
import com.sixbynine.transit.path.test.TestSetupHelper
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertContains

class LineComputerTest {
    @Before
    fun `set up`() {
        TestSetupHelper.setUp()
    }

    @Test
    fun `wtc-33s line from hob-wtc colors`() {
        var lines = LineComputer.computeLines("EXP", "33S", Colors.Hob33s)
        listOf(HobokenWtc, JournalSquare33rd).forEach { line ->
            assertContains(lines, line)
        }

        lines = LineComputer.computeLines("EXP", "WTC", Colors.Hob33s)
        assertContains(lines, Line.Hoboken33rd)
    }

    @Test
    fun `wtc-33s line from hob-wtc colors - wtc`() {
        TestSetupHelper.setUp()

        val lines = LineComputer.computeLines("WTC", "33S", Colors.Hob33s)
        listOf(HobokenWtc, JournalSquare33rd).forEach { line ->
            assertContains(lines, line)
        }
    }

    @Test
    fun `wtc-33s line from hob-wtc colors - new`() {
        TestSetupHelper.setUp()

        var lines = LineComputer.computeLines("NEW", "33S", Colors.Hob33s)
        assertContains(lines, Line.JournalSquare33rd)

        lines = LineComputer.computeLines("NEW", "WTC", Colors.Hob33s)
        assertContains(lines, Line.HobokenWtc)
    }

    @Test
    fun `wtc-33s line from purple colors`() {
        TestSetupHelper.setUp()

        var lines = LineComputer.computeLines("EXP", "33S", Colors.HobWtc)
        listOf(HobokenWtc, JournalSquare33rd).forEach { line ->
            assertContains(lines, line)
        }

        lines = LineComputer.computeLines("NEW", "WTC", Colors.HobWtc)
        assertContains(lines, Line.HobokenWtc)
    }
}
