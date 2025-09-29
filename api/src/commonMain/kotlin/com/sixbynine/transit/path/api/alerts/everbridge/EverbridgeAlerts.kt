package com.desaiwang.transit.path.api.alerts.everbridge

import com.desaiwang.transit.path.api.Line
import com.desaiwang.transit.path.api.Line.Hoboken33rd
import com.desaiwang.transit.path.api.Line.HobokenWtc
import com.desaiwang.transit.path.api.Line.JournalSquare33rd
import com.desaiwang.transit.path.api.Line.NewarkWtc
import com.desaiwang.transit.path.api.Station
import com.desaiwang.transit.path.api.Stations
import com.desaiwang.transit.path.api.alerts.Alert
import com.desaiwang.transit.path.api.alerts.AlertText
import com.desaiwang.transit.path.api.alerts.Schedule
import com.desaiwang.transit.path.api.alerts.TrainFilter
import com.desaiwang.transit.path.api.templine.HobClosureConfigRepository
import com.desaiwang.transit.path.time.NewYorkTimeZone
import com.desaiwang.transit.path.util.InstantAsEpochMillisSerializer
import kotlinx.datetime.DateTimeUnit.DayBased
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atTime
import kotlinx.datetime.format.char
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlin.time.Duration.Companion.hours

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
    @SerialName("CreatedDate")
    @Serializable(with = InstantAsEpochMillisSerializer::class)
    val createdDate: Instant,
    /** It is suspected that these two dates are the same anyway **/
    @SerialName("ModifiedDate")
    @Serializable(with = InstantAsEpochMillisSerializer::class)
    val modifiedDate: Instant,
)

@Serializable
data class IncidentMessage(
    /** Title of the alert, usually describes the cause **/
    val subject: String,
    /** The full alert message **/
    val preMessage: String,
    /** Variables **/
    val formVariableItems: List<Variable>,
    /** Format for dates. */
    @SerialName("sysVarTodayDateFormat")
    val dateFormat: String?,
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

private fun EverbridgeAlert.isElevatorMessageFor(station: Station): Boolean {
    val elevatorDesc = incidentMessage.formVariableItems.firstOrNull {
        it.variableName.contains("elevator", ignoreCase = true)
    }
    val elevatorValue = elevatorDesc?.value?.firstOrNull() ?: return false

    val regex = "${station.displayName.split(" ").first()}|${station.pathApiName}"
        .toRegex(RegexOption.IGNORE_CASE)
    return elevatorValue.contains(regex)
}

fun EverbridgeAlert.toCommonAlert(): Alert {
    val elevatorStations = Stations.All.filter { isElevatorMessageFor(it) }
    val isElevatorAlert = elevatorStations.isNotEmpty()
    return Alert(
        stations = elevatorStations,
        hideTrainsSchedule = Schedule(),
        displaySchedule = schedule,
        hiddenTrainsFilter = TrainFilter(),
        message = AlertText(incidentMessage.preMessage),
        url = AlertText(PATH_ALERTS_URL),
        isGlobal = !isElevatorAlert,
        level = if (isElevatorAlert) "INFO" else "WARN",
        lines = incidentMessage.lines,
    )
}

private val EverbridgeAlert.schedule: Schedule
    get() {
        val date = incidentMessage.date
        val minEndTime = modifiedDate.plus(1.hours).toLocalDateTime(NewYorkTimeZone)
        val endOfOriginalDate = date?.plus(1, DayBased(1))?.atTime(0, 0)
        return Schedule.once(
            from = modifiedDate.toLocalDateTime(NewYorkTimeZone),
            to = if (endOfOriginalDate == null) {
                minEndTime
            } else {
                maxOf(minEndTime, endOfOriginalDate)
            }
        )
    }

private operator fun IncidentMessage.get(name: String): Variable? {
    return getVariable(name)
}

private fun IncidentMessage.getVariable(name: String): Variable? {
    return formVariableItems.find { it.variableName == name }
}

private val IncidentMessage.lines: Set<Line>
    get() {
        val linesDesc = getVariable("Lines")
        return linesDesc
            ?.value
            .orEmpty()
            .flatMap { line ->
                if (line in HobClosureConfigRepository.getConfig().tempLineInfo.codes) {
                    return@flatMap Line.permanentLinesForWtc33rd
                }
                when (line) {
                    "JSQ-33 via HOB" -> listOf(JournalSquare33rd, Hoboken33rd)
                    "NWK-WTC" -> listOf(NewarkWtc)
                    "HOB-WTC" -> listOf(HobokenWtc)
                    "JSQ-33" -> listOf(JournalSquare33rd)
                    "HOB-33" -> listOf(Hoboken33rd)
                    "WTC-33" -> Line.permanentLinesForWtc33rd
                    "PATH" -> Line.permanentLines
                    else -> emptyList()
                }
            }
            .toSet()
    }

val IncidentMessage.date: LocalDate?
    get() {
        val rawDate = getVariable("Date")?.value?.firstOrNull() ?: return null
        dateFormat ?: return null
        val format = LocalDate.Format {
            var hasYear = false
            var hasMonth = false
            var hasDay = false
            dateFormat.forEach {
                when (it) {
                    'y', 'Y' -> {
                        if (hasYear) return@forEach
                        hasYear = true
                        year()
                    }

                    'm', 'M' -> {
                        if (hasMonth) return@forEach
                        hasMonth = true
                        monthNumber()
                    }

                    'd', 'D' -> {
                        if (hasDay) return@forEach
                        hasDay = true
                        dayOfMonth()
                    }

                    else -> {
                        char(it)
                    }
                }
            }
        }
        return LocalDate.parse(rawDate, format)
    }
