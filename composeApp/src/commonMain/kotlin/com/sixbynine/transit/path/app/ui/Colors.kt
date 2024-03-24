package com.sixbynine.transit.path.app.ui

import androidx.compose.ui.graphics.Color
import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.app.ui.theme.md_theme_dark_background
import com.sixbynine.transit.path.app.ui.theme.md_theme_light_background
import com.sixbynine.transit.path.app.ui.theme.seed

object Colors {
    fun parse(hexString: String): Color {
        return Color(hexString.removePrefix("#").toLong(16) or 0x00000000FF000000)
    }

    val Path: ColorWrapper
        get() = ColorWrapper(seed)

    val NwkWtcSingle = Color(red = 0xd9, green = 0x3a, blue = 0x30).wrap()
    val Jsq33sSingle = Color(red = 0xff, green = 0x99, blue = 0x00).wrap()
    val Hob33sSingle = Color(red = 0x4d, green = 0x92, blue = 0xfb).wrap()
    val HobWtcSingle = Color(red = 0x65, green = 0xc1, blue = 0x00).wrap()

    val NwkWtc = listOf(NwkWtcSingle)
    val Jsq33s = listOf(Jsq33sSingle)
    val Hob33s = listOf(Hob33sSingle)
    val HobWtc = listOf(HobWtcSingle)

    fun background(isDark: Boolean): ColorWrapper {
        Logging.initialize()
        return if (isDark) {
            md_theme_dark_background.wrap()
        } else {
            md_theme_light_background.wrap()
        }
    }

    fun Color.wrap(): ColorWrapper {
        return ColorWrapper(this)
    }
}
