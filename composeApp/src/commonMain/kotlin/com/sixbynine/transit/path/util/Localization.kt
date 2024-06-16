package com.sixbynine.transit.path.util

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.language_code

private val locale by lazy { runBlocking { getString(string.language_code) } }

fun localizedString(en: () -> String, es: () -> String) : String {
    return when (locale) {
        "es" -> es()
        else -> en()
    }
}

fun localizedString(en: String, es: String) : String {
    return when (locale) {
        "es" -> es
        else -> en
    }
}
