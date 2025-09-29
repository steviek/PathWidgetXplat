package com.desaiwang.transit.path.app.external

import PlatformType
import getPlatform
import org.jetbrains.compose.resources.getString
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.sharing_message

interface ExternalRoutingManager {
    suspend fun openEmail(): Boolean

    suspend fun openUrl(url: String): Boolean

    suspend fun shareTextToSystem(text: String): Boolean

    suspend fun launchAppRating(): Boolean
}

expect fun ExternalRoutingManager(): ExternalRoutingManager

suspend fun ExternalRoutingManager.shareAppToSystem(): Boolean {
    val text = StringBuilder()

    text.appendLine(getString(string.sharing_message))
    text.appendLine()

    listOf(AndroidSharingLine, "", IosSharingLine)
        .let { if (getPlatform().type == PlatformType.ANDROID) it else it.reversed() }
        .forEach { text.appendLine(it) }

    return shareTextToSystem(text.toString())
}

const val FeedbackEmail = "sixbynineapps@gmail.com"

const val AppStoreUrl = "https://apps.apple.com/id/app/departures-widget-for-path/id6470330823"

private const val IosSharingLine = "iOS: $AppStoreUrl"

private const val AndroidSharingLine =
    "Android: https://play.google.com/store/apps/details?id=com.desaiwang.transit.path"
