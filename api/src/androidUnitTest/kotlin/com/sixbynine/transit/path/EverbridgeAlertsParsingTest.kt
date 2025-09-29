package com.desaiwang.transit.path

import com.desaiwang.transit.path.api.Line
import com.desaiwang.transit.path.api.alerts.everbridge.EverbridgeAlerts
import com.desaiwang.transit.path.api.alerts.everbridge.date
import com.desaiwang.transit.path.api.alerts.everbridge.toCommonAlert
import com.desaiwang.transit.path.api.alerts.getText
import com.desaiwang.transit.path.api.alerts.isDisplayedAt
import com.desaiwang.transit.path.test.TestSetupHelper
import com.desaiwang.transit.path.time.NewYorkTimeZone
import com.desaiwang.transit.path.util.JsonFormat
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import org.junit.Before
import java.time.Month.DECEMBER
import java.time.Month.JANUARY
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EverbridgeAlertsParsingTest {

    @Before
    fun `set up`() {
        TestSetupHelper.setUp()
    }

    @Test
    fun `parsing values`() {
        val json =
            EverbridgeAlertsParsingTest::class.java
                .getResource("everbridge_alert.json")!!
                .readText()

        val alerts = JsonFormat.decodeFromString<EverbridgeAlerts>(json)
        val alert = alerts.data.single()

        assertEquals(
            LocalDateTime(2024, DECEMBER, 28, 14, 30).toInstant(NewYorkTimeZone),
            alert.modifiedDate,
        )

        val line =
            alert.incidentMessage
                .formVariableItems
                .first { it.variableName == "Lines" }
                .value
                .orEmpty()
                .single()
        assertEquals("NWK-WTC", line)
    }

    @Test
    fun `not active from previous day`() {
        val json =
            EverbridgeAlertsParsingTest::class.java
                .getResource("everbridge_alert_jan18.json")!!
                .readText()

        val alerts = JsonFormat.decodeFromString<EverbridgeAlerts>(json)

        val alert = alerts.data.single()
        assertEquals(LocalDate(2025, JANUARY, 17), alert.incidentMessage.date)

        val result = alert.toCommonAlert()
        assertEquals(setOf(Line.NewarkWtc), result.lines)
        assertTrue { result.isDisplayedAt(LocalDateTime(2025, JANUARY, 17, 12, 0)) }
        assertFalse { result.isDisplayedAt(LocalDateTime(2025, JANUARY, 18, 12, 0)) }
    }

    @Test
    fun `jan 23 parsing`() {
        val json =
            EverbridgeAlertsParsingTest::class.java
                .getResource("everbridge_alert_jan23.json")!!
                .readText()

        val alerts = JsonFormat.decodeFromString<EverbridgeAlerts>(json)
        val alert = alerts.data.single().toCommonAlert()

        assertEquals(setOf(Line.HobokenWtc), alert.lines)
        assertEquals("WARN", alert.level)
        assertTrue(alert.isGlobal)
        assertTrue(alert.isDisplayedAt(LocalDateTime(2025, JANUARY, 23, 20, 52)))
        assertContains(alert.message?.getText("en")!!, "HOB-WTC suspended until the late evening")
    }

    @Test
    fun `empty message`() {
        val json = """
            {"status":"Success","data":[]}
        """.trimIndent()

        val alerts = JsonFormat.decodeFromString<EverbridgeAlerts>(json)

        assertTrue(alerts.data.isEmpty())
    }
}
