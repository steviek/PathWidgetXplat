package com.sixbynine.transit.path

import com.sixbynine.transit.path.api.everbridge.EverbridgeAlerts
import com.sixbynine.transit.path.time.NewYorkTimeZone
import com.sixbynine.transit.path.util.JsonFormat
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import java.time.Month.DECEMBER
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EverbridgeAlertsParsingTest {

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
    fun `empty message`() {
        val json = """
            {"status":"Success","data":[]}
        """.trimIndent()

        val alerts = JsonFormat.decodeFromString<EverbridgeAlerts>(json)

        assertTrue(alerts.data.isEmpty())
    }
}
