package com.sixbynine.transit.path.app.external

import PlatformType
import com.sixbynine.transit.path.MR.strings
import com.sixbynine.transit.path.resources.getString
import getPlatform

interface ExternalRoutingManager {
    suspend fun openEmail(): Boolean

    fun shareTextToSystem(text: String): Boolean

    suspend fun launchAppRating(): Boolean
}

expect fun ExternalRoutingManager(): ExternalRoutingManager

fun ExternalRoutingManager.shareAppToSystem(): Boolean {
    val text = StringBuilder()

    text.appendLine(getString(strings.sharing_message))
    text.appendLine()

    listOf(AndroidSharingLine, "", IosSharingLine)
        .let { if (getPlatform().type == PlatformType.ANDROID) it else it.reversed() }
        .forEach { text.appendLine(it) }

    return shareTextToSystem(text.toString())
}

const val FeedbackEmail = "sixbynineapps@gmail.com"

private const val IosSharingLine =
    "iOS: https://apps.apple.com/id/app/departures-widget-for-path/id6470330823"

private const val AndroidSharingLine =
    "Android: https://play.google.com/store/apps/details?id=com.sixbynine.transit.path"
