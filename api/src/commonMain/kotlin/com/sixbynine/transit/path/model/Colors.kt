package com.desaiwang.transit.path.model

import androidx.compose.ui.graphics.Color
import com.desaiwang.transit.path.Logging
import com.desaiwang.transit.path.api.templine.HobClosureConfigRepository

object Colors {
    fun parse(hexString: String): Color {
        return Color(hexString.removePrefix("#").toLong(16) or 0x00000000FF000000)
    }

    val Path: ColorWrapper
        get() = ColorWrapper(Color(0xFF1896D1))

    val NwkWtcSingle = Color(red = 0xd9, green = 0x3a, blue = 0x30).wrap()
    val Jsq33sSingle = Color(red = 0xff, green = 0x99, blue = 0x00).wrap()
    val Hob33sSingle = Color(red = 0x4d, green = 0x92, blue = 0xfb).wrap()
    val HobWtcSingle = Color(red = 0x65, green = 0xc1, blue = 0x00).wrap()
    val Wtc33sSingle by lazy {
        HobClosureConfigRepository.getConfig().tempLineInfo.lightColor
            .let { parse(it) }
            .wrap()
    }
    val NwkWtc = listOf(NwkWtcSingle)
    val Jsq33s = listOf(Jsq33sSingle)
    val Hob33s = listOf(Hob33sSingle)
    val HobWtc = listOf(HobWtcSingle)
    val Wtc33s by lazy { listOf(Wtc33sSingle) }

    fun background(isDark: Boolean): ColorWrapper {
        Logging.initialize()
        return if (isDark) {
            Color(0xFF191C1E).wrap()
        } else {
            Color(0xFFFCFCFF).wrap()
        }
    }

    fun Color.wrap(): ColorWrapper {
        return ColorWrapper(this)
    }

    infix fun Color.approxEquals(other: Color): Boolean {
        val dR = red - other.red
        val dG = green - other.green
        val dB = blue - other.blue
        val delta = (dR * dR) + (dG * dG) + (dB * dB)
        return delta < .1f
    }
}
