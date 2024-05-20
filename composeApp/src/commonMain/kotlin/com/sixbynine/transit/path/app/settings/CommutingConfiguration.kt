package com.sixbynine.transit.path.app.settings

import com.sixbynine.transit.path.api.State
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class CommutingConfiguration(
    @Serializable(with = StateSerializer::class) val homeState: State,
)

private object StateSerializer : KSerializer<State> {
    override val descriptor = PrimitiveSerialDescriptor("State", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): State {
        val isNewYork = decoder.decodeInt() == 1
        return if (isNewYork) State.NewYork else State.NewJersey
    }

    override fun serialize(encoder: Encoder, value: State) {
        encoder.encodeInt(if (value == State.NewYork) 1 else 0)
    }
}