package com.desaiwang.transit.path.util

import androidx.compose.ui.text.intl.LocaleList
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.language_code

private val locale by lazy {
    runCatching { runBlocking { getString(string.language_code) } }
        .getOrElse {
            // Apparently, getString can crash sometimes? Maybe only when called from non-compose
            // code. Let's try this, even though it's also compose logic. We can hand-roll even more
            // if needed.
            val localeList = LocaleList.current
            localeList
                .firstOrNull { it.language == "es" || it.language == "en" }
                ?.language
                ?: localeList.firstOrNull()?.language
                ?: "en"
        }
}

fun localizedString(en: () -> String, es: () -> String): String {
    return when (locale) {
        "es" -> es()
        else -> en()
    }
}

fun localizedString(en: String, es: String): String {
    return when (locale) {
        "es" -> es
        else -> en
    }
}
