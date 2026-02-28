package com.sixbynine.transit.path.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveKind.LONG
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlin.time.Instant

val JsonFormat = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    isLenient = true
}

class InstantAsEpochMillisSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("InstantAsEpochMillis", LONG)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeLong(value.toEpochMilliseconds())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.fromEpochMilliseconds(decoder.decodeLong())
    }
}

class InstantAsISO8601Serializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("InstantAsISO8601", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}
