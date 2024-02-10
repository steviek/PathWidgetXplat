package com.sixbynine.transit.path.resources

import com.sixbynine.transit.path.MR.strings

fun localizedString(english: () -> String, spanish: () -> String): String {
    return foldLocale(english, spanish)
}

fun <T> foldLocale(english: () -> T, spanish: () -> T): T {
    return when (getString(strings.langauge_code)) {
        "en" -> english()
        "es" -> spanish()

        else -> english()
    }
}
