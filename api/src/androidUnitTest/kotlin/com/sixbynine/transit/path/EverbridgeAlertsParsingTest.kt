package com.sixbynine.transit.path

import com.sixbynine.transit.path.api.Line.NewarkWtc
import com.sixbynine.transit.path.api.alerts.isDisplayedAt
import com.sixbynine.transit.path.api.everbridge.EverbridgeAlerts
import com.sixbynine.transit.path.api.everbridge.date
import com.sixbynine.transit.path.api.everbridge.getAlertsForLines
import com.sixbynine.transit.path.test.TestSetupHelper
import com.sixbynine.transit.path.time.NewYorkTimeZone
import com.sixbynine.transit.path.util.JsonFormat
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import org.junit.Before
import java.time.Month.DECEMBER
import java.time.Month.JANUARY
import kotlin.test.Test
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

        val line =
            alert.incidentMessage
                .formVariableItems
                .first { it.variableName == "Lines" }
                .value
                .orEmpty()
                .single()
        assertEquals("NWK-WTC", line)
        assertEquals(LocalDate(2025, JANUARY, 17), alert.incidentMessage.date)

        val result = alerts.getAlertsForLines(listOf(NewarkWtc)).first()
        assertTrue { result.isDisplayedAt(LocalDateTime(2025, JANUARY, 17, 12, 0)) }
        assertFalse { result.isDisplayedAt(LocalDateTime(2025, JANUARY, 18, 12, 0)) }
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
