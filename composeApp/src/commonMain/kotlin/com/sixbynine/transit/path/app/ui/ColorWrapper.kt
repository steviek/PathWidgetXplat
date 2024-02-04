package com.sixbynine.transit.path.app.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = ColorWrapperSerializer::class)
data class ColorWrapper(val color: Color) {
    val red: Float
        get() = color.red

    val green: Float
        get() = color.green

    val blue: Float
        get() = color.blue
}

object ColorWrapperSerializer : KSerializer<ColorWrapper> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ColorWrapper", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: ColorWrapper) {
        encoder.encodeInt(value.color.toArgb())
    }

    override fun deserialize(decoder: Decoder): ColorWrapper {
        return ColorWrapper(Color(decoder.decodeInt()))
    }
}
