package com.desaiwang.transit.path.app.ui.layout

import com.desaiwang.transit.path.preferences.IntPreferencesKey
import com.desaiwang.transit.path.preferences.persisting

enum class LayoutOption(val number: Int) {
    OneColumn(1), TwoColumns(2), ThreeColumns(3)
}

object LayoutOptionManager {
    private val LayoutOptionKey = IntPreferencesKey("layout_option")
    private var storedLayoutOption by persisting(LayoutOptionKey)

    var layoutOption: LayoutOption?
        get() =
            storedLayoutOption
                ?.let { storedOption -> LayoutOption.entries.find { it.number == storedOption } }
        set(value) {
            storedLayoutOption = value?.number
        }
}
