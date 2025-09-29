package com.desaiwang.transit.path

import com.desaiwang.transit.path.api.Line.NewarkWtc
import com.desaiwang.transit.path.api.Stations
import com.desaiwang.transit.path.api.alerts.AlertText
import com.desaiwang.transit.path.api.alerts.canHideTrainsAt
import com.desaiwang.transit.path.api.alerts.everbridge.EverbridgeAlert
import com.desaiwang.transit.path.api.alerts.everbridge.EverbridgeAlerts
import com.desaiwang.transit.path.api.alerts.everbridge.IncidentMessage
import com.desaiwang.transit.path.api.alerts.everbridge.Variable
import com.desaiwang.transit.path.api.alerts.everbridge.toCommonAlert
import com.desaiwang.transit.path.api.alerts.getText
import com.desaiwang.transit.path.api.alerts.isDisplayedAt
import com.desaiwang.transit.path.test.TestSetupHelper
import com.desaiwang.transit.path.util.JsonFormat
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month.DECEMBER
import kotlinx.datetime.Month.OCTOBER
import kotlinx.serialization.encodeToString
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EverbridgeAlertsTest {
    @BeforeTest
    fun setUp() {
        TestSetupHelper.setUp()
    }

    @Test
    fun `current alerts text`() {
        val alerts = EverbridgeAlerts(
            HobokenElevatorDown,
            NwkWtcDown
        )

        val json = JsonFormat.encodeToString(alerts)

        assertEquals(alerts, JsonFormat.decodeFromString(json))

        println(json)
    }

    @Test
    fun `station alert conversion`() {
        val alerts = EverbridgeAlerts(HobokenElevatorDown).data.map { it.toCommonAlert() }
        val hobokenAlert = alerts.single()

        assertEquals(listOf(Stations.Hoboken.pathApiName), hobokenAlert.stations)
        assertContains(hobokenAlert.message?.getText("en").orEmpty(), "At Hoboken, elevator")
        assertTrue(hobokenAlert.isDisplayedAt(LocalDateTime(2024, DECEMBER, 23, 16, 21)))
        assertFalse(hobokenAlert.isGlobal)
        assertEquals("INFO", hobokenAlert.level)
    }

    @Test
    fun `global alert conversion`() {
        val alerts = EverbridgeAlerts(NwkWtcDown).data.map { it.toCommonAlert() }
        val result = alerts.single()
        assertEquals(result.stations, emptyList())
        assertEquals(setOf(NewarkWtc), result.lines)
        assertEquals(result.message, AlertText(NwkWtcDown.incidentMessage.preMessage))
        assertTrue(result.isDisplayedAt(LocalDateTime(2024, OCTOBER, 21, 13, 57)))
        assertFalse(result.canHideTrainsAt(LocalDateTime(2024, OCTOBER, 21, 13, 57)))
        assertTrue(result.isGlobal)
    }

    private companion object {
        val HobokenElevatorDown = EverbridgeAlert(
            incidentMessage = IncidentMessage(
                subject = "PATHAlert - Elevators",
                preMessage = "At Hoboken, elevator from street to fare zone is temporarily out of service. Call 800-234-PATH or use Passenger Assistance Phone if no agent is available. We regret this inconvenience.",
                formVariableItems = listOf(
                    Variable(
                        value = listOf("At Hoboken, elevator from street to fare zone is"),
                        variableName = "Station Elevator Description",
                        isRequired = true,
                        seq = 1
                    ),
                    Variable(
                        value = listOf("temporarily out of service"),
                        variableName = "Elevator Status",
                        isRequired = true,
                        seq = 2
                    ),
                    Variable(
                        value = listOf("Call 800-234-PATH or use Passenger Assistance Phone if no agent is available."),
                        variableName = "Elevator Guidance",
                        isRequired = false,
                        seq = 3
                    ),
                ),
                dateFormat = "mm-dd-yyyy",
            ),
            createdDate = Instant.fromEpochMilliseconds(1734988841776),
            modifiedDate = Instant.fromEpochMilliseconds(1734988841776)
        )
        val NwkWtcDown = EverbridgeAlert(
            incidentMessage = IncidentMessage(
                subject = "PATHAlert Update: Track Condition",
                preMessage = "01:56 PM: NWK-WTC delays continue. Next update w/in 15m.",
                formVariableItems = listOf(
                    Variable(
                        value = listOf("NWK-WTC"),
                        variableName = "Lines",
                        isRequired = true,
                        seq = 1
                    ),
                    Variable(
                        value = listOf("delays continue."),
                        variableName = "Status",
                        isRequired = true,
                        seq = 2
                    ),
                    Variable(
                        value = listOf("Crew working to resolve track condition"),
                        variableName = "Track Condition Reason Update",
                        isRequired = false,
                        seq = 3
                    ),
                    Variable(
                        value = listOf("between"),
                        variableName = "Preposition",
                        isRequired = true,
                        seq = 4
                    ),
                    Variable(
                        value = listOf("HAR", "JSQ"),
                        variableName = "Station",
                        isRequired = true,
                        seq = 5
                    ),
                    Variable(
                        variableName = "NJT Cross Honor Status",
                        isRequired = false,
                        seq = 6
                    ),
                    Variable(
                        variableName = "NJT Cross Honoring at",
                        isRequired = false,
                        seq = 7
                    ),
                    Variable(
                        variableName = "NYWW Cross Honor Status",
                        isRequired = false,
                        seq = 8
                    ),
                    Variable(
                        variableName = "NYWW Cross Honoring at",
                        isRequired = false,
                        seq = 9
                    ),
                    Variable(
                        variableName = "Cross Honor Start/End Time",
                        isRequired = false,
                        seq = 10
                    ),
                    Variable(
                        variableName = "Time Select",
                        isRequired = false,
                        seq = 11
                    ),
                    Variable(
                        value = listOf("Next update w/in 15m."),
                        variableName = "Guidance",
                        isRequired = false,
                        seq = 12
                    )
                ),
                dateFormat = "mm-dd-yyyy",
            ),
            createdDate = Instant.fromEpochMilliseconds(1729533384417),
            modifiedDate = Instant.fromEpochMilliseconds(1729533384417)
        )
    }
}