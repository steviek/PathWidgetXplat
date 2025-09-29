package com.desaiwang.transit.path.schedule

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.isoDayNumber
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind.INT
import kotlinx.serialization.descriptors.PrimitiveKind.SHORT
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Serializable
data class Schedules(
    @Serializable(with = LocalDateTimeSerializer::class) val validFrom: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) val validTo: LocalDateTime?,
    val schedules: List<Schedule>,
    val timings: List<ScheduleTiming>,
    val name: String?,
)

@Serializable
data class ScheduleTiming(
    val scheduleId: Int,
    @Serializable(with = DayOfWeekAsIntSerializer::class) val startDay: DayOfWeek,
    @Serializable(with = LocalTimeAsShortSerializer::class) val startTime: LocalTime,
    @Serializable(with = DayOfWeekAsIntSerializer::class) val endDay: DayOfWeek,
    @Serializable(with = LocalTimeAsShortSerializer::class) val endTime: LocalTime,
)

@Serializable
data class Schedule(
    val id: Int,
    val name: String,
    val departures: Map<String, List<@Serializable(with = LocalTimeAsShortSerializer::class) LocalTime>>,
    @Serializable(with = LocalTimeAsShortSerializer::class) val firstSlowDepartureTime: LocalTime?,
    @Serializable(with = LocalTimeAsShortSerializer::class) val lastSlowDepartureTime: LocalTime?,
)

class LocalTimeAsShortSerializer : KSerializer<LocalTime> {
    override val descriptor = PrimitiveSerialDescriptor("LocalTimeAsInt", SHORT)

    override fun serialize(encoder: Encoder, value: LocalTime) {
        val packed = (value.hour * 100 + value.minute).toShort()
        encoder.encodeShort(packed)
    }

    override fun deserialize(decoder: Decoder): LocalTime {
        val packed = decoder.decodeShort()
        val hour = packed / 100
        val minute = packed % 100
        return LocalTime(hour = hour, minute = minute)
    }
}

class LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString())
    }
}

class DayOfWeekAsIntSerializer : KSerializer<DayOfWeek> {
    override val descriptor = PrimitiveSerialDescriptor("DayOfWeekInt", INT)

    override fun serialize(encoder: Encoder, value: DayOfWeek) {
        encoder.encodeInt(value.isoDayNumber)
    }

    override fun deserialize(decoder: Decoder): DayOfWeek {
        return DayOfWeek(isoDayNumber = decoder.decodeInt())
    }
}
