package com.desaiwang.transit.path.app.settings

import com.desaiwang.transit.path.api.State
import com.desaiwang.transit.path.api.State.NewJersey
import com.desaiwang.transit.path.api.State.NewYork
import com.desaiwang.transit.path.time.minusDays
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.DayOfWeek.FRIDAY
import kotlinx.datetime.DayOfWeek.MONDAY
import kotlinx.datetime.DayOfWeek.THURSDAY
import kotlinx.datetime.DayOfWeek.TUESDAY
import kotlinx.datetime.DayOfWeek.WEDNESDAY
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class CommutingConfiguration(
    val schedules: List<Schedule>,
) {
    companion object {
        val DefaultSchedule = Schedule(
            days = setOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY),
            start = LocalTime(12, 0),
            end = LocalTime(3, 0),
        )

        fun default(): CommutingConfiguration {
            return CommutingConfiguration(
                schedules = listOf(DefaultSchedule)
            )
        }
    }
}

val CommutingConfiguration.activeSchedule: Schedule
    get() = schedules.firstOrNull() ?: CommutingConfiguration.DefaultSchedule

@Serializable
data class Schedule(
    val days: Set<DayOfWeek>,
    val start: LocalTime,
    val end: LocalTime,
)

fun CommutingConfiguration.isActiveAt(dateTime: LocalDateTime): Boolean {
    return activeSchedule.isActiveAt(dateTime)
}

fun Schedule.isActiveAt(dateTime: LocalDateTime): Boolean {
    return isActiveAt(dateTime.dayOfWeek, dateTime.time)
}

fun Schedule.isActiveAt(day: DayOfWeek, time: LocalTime): Boolean = when {
    start == end -> day in days // bonus feature?
    start < end -> day in days && time >= start && time < end
    time < end -> day.minusDays(1) in days
    time >= start -> day in days
    else -> false
}

private object StateSerializer : KSerializer<State> {
    override val descriptor = PrimitiveSerialDescriptor("State", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): State {
        val isNewYork = decoder.decodeInt() == 1
        return if (isNewYork) NewYork else NewJersey
    }

    override fun serialize(encoder: Encoder, value: State) {
        encoder.encodeInt(if (value == NewYork) 1 else 0)
    }
}
