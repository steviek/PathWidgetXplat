package com.sixbynine.transit.path.api.everbridge

import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.alerts.Alert
import com.sixbynine.transit.path.api.alerts.AlertText
import com.sixbynine.transit.path.api.alerts.Schedule
import com.sixbynine.transit.path.api.alerts.TrainFilter
import com.sixbynine.transit.path.time.NewYorkTimeZone
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private const val PATH_ALERTS_URL = "https://www.panynj.gov/path/en/alerts.html"

@Serializable
data class EverbridgeAlerts(
    val data: List<EverbridgeAlert>,
    val status: String,
) {
    constructor(vararg everbridgeAlerts: EverbridgeAlert) : this(
        everbridgeAlerts.toList(),
        "success"
    )
}

@Serializable
data class EverbridgeAlert(
    val incidentMessage: IncidentMessage,
    @JsonNames("CreatedDate")
    val createdDate: Long,
    /** It is suspected that these two dates are the same anyway **/
    @JsonNames("ModifiedDate")
    val modifiedDate: Long,
)

@Serializable
data class IncidentMessage(
    /** Title of the alert, usually describes the cause **/
    val subject: String,
    /** The full alert message **/
    val preMessage: String,
    /** Variables **/
    val formVariableItems: List<Variable>,
)

@Serializable
data class Variable(
    /** Exact value of each variable. Can be empty **/
    @JsonNames("val")
    val value: List<String>? = null,
    /** Name of the variable **/
    val variableName: String,
    /** Whether the information is required; true for those stating the outage, false for guidance **/
    val isRequired: Boolean,
    /** Order of variable, increments from 1 **/
    val seq: Int
)

fun EverbridgeAlert.isForStation(station: Station): Boolean {
    val elevatorDesc = incidentMessage.formVariableItems.firstOrNull {
        it.variableName.contains("elevator", true)
    }
    return elevatorDesc?.value?.get(0)?.contains(
        station.displayName.split(" ")[0], false
    ) ?: false
}

fun EverbridgeAlert.isForLines(lineIds: List<Int>): Boolean {
    // this should match the order in Line.kt
    val linesDesc = incidentMessage.formVariableItems.firstOrNull { it.variableName == "Lines" }
    return linesDesc?.value?.any { lineIds.contains(lineToIndex(it)) } ?: false
}

fun EverbridgeAlert.toGithubAlert(): Alert {
    return Alert(
        stations = emptyList(),
        schedule = Schedule.once(
            from = Instant.fromEpochMilliseconds(modifiedDate).toLocalDateTime(NewYorkTimeZone),
            to = Clock.System.now().plus(1.toDuration(DurationUnit.HOURS))
                .toLocalDateTime(NewYorkTimeZone)
        ),
        trains = TrainFilter(),
        message = AlertText(incidentMessage.preMessage),
        url = AlertText(PATH_ALERTS_URL),
        isGlobal = true,
        level = "WARN"
    )
}

fun EverbridgeAlert.toGithubAlert(station: Station): Alert {
    return Alert(
        stations = listOf(station),
        schedule = Schedule.once(
            from = Instant.fromEpochMilliseconds(modifiedDate).toLocalDateTime(NewYorkTimeZone),
            to = Clock.System.now().plus(1.toDuration(DurationUnit.HOURS))
                .toLocalDateTime(NewYorkTimeZone)
        ),
        trains = TrainFilter(),
        message = AlertText(incidentMessage.preMessage),
        url = AlertText(PATH_ALERTS_URL)
    )
}

fun EverbridgeAlerts.getAlertsForStation(station: Station): List<Alert> {
    return data.filter {
        it.isForStation(station)
    }.map {
        it.toGithubAlert(station)
    }
}

fun EverbridgeAlerts.getAlertsForLines(lineIds: List<Int>): List<Alert> {
    return data.filter {
        it.isForLines(lineIds)
    }.map {
        it.toGithubAlert()
    }
}

fun lineToIndex(line: String): Int {
    when (line) {
        // TODO: weekend services
        "NWK-WTC" -> return 1
        "HOB-WTC" -> return 2
        "JSQ-33" -> return 3
        "HOB-33" -> return 4
    }
    return 0
}