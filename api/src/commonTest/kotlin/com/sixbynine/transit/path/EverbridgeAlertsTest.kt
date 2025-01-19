package com.sixbynine.transit.path

import com.sixbynine.transit.path.api.Line.HobokenWtc
import com.sixbynine.transit.path.api.Line.NewarkWtc
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.alerts.AlertText
import com.sixbynine.transit.path.api.alerts.isActiveAt
import com.sixbynine.transit.path.api.everbridge.EverbridgeAlert
import com.sixbynine.transit.path.api.everbridge.EverbridgeAlerts
import com.sixbynine.transit.path.api.everbridge.IncidentMessage
import com.sixbynine.transit.path.api.everbridge.Variable
import com.sixbynine.transit.path.api.everbridge.getAlertsForLines
import com.sixbynine.transit.path.api.everbridge.getAlertsForStation
import com.sixbynine.transit.path.test.TestSetupHelper
import com.sixbynine.transit.path.util.JsonFormat
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month.DECEMBER
import kotlinx.datetime.Month.OCTOBER
import kotlinx.serialization.encodeToString
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
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
        val alerts = EverbridgeAlerts(HobokenElevatorDown)
        val result = alerts.getAlertsForStation(Stations.Hoboken).first()
        assertEquals(result.stations, listOf(Stations.Hoboken.pathApiName))
        assertEquals(result.message, AlertText(HobokenElevatorDown.incidentMessage.preMessage))
        assertNull(result.trains.all)
        assertNull(result.trains.headSigns)
        assertTrue(result.isActiveAt(LocalDateTime(2024, DECEMBER, 23, 16, 21)))
        assertTrue(alerts.getAlertsForStation(Stations.JournalSquare).isEmpty())
    }

    @Test
    fun `global alert conversion`() {
        val alerts = EverbridgeAlerts(NwkWtcDown)
        val result = alerts.getAlertsForLines(listOf(NewarkWtc)).first()
        assertEquals(result.stations, emptyList())
        assertEquals(result.message, AlertText(NwkWtcDown.incidentMessage.preMessage))
        assertNull(result.trains.all)
        assertNull(result.trains.headSigns)
        assertTrue(result.isActiveAt(LocalDateTime(2024, OCTOBER, 21, 13, 57)))
        assertTrue(result.isGlobal)
        assertTrue(alerts.getAlertsForLines(listOf(HobokenWtc)).isEmpty())
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