package com.sixbynine.transit.path.app.external

import com.sixbynine.transit.path.analytics.Analytics
import com.sixbynine.transit.path.time.now
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.Instant
import platform.Foundation.NSURL
import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIWindow
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.minutes

object IosExternalRoutingManager : ExternalRoutingManager {
    private val recentAppRatingClicks = mutableListOf<Instant>()

    override suspend fun openEmail(): Boolean {
        return openUrl("mailto:$FeedbackEmail")
    }

    override suspend fun openUrl(url: String): Boolean {
        val nsUrl = NSURL.URLWithString(url) ?: return false
        return suspendCancellableCoroutine { continuation ->
            UIApplication.sharedApplication.openURL(
                url = nsUrl,
                options = emptyMap<Any?, Any?>()
            ) { result ->
                continuation.resume(result)
            }
        }
    }

    override fun shareTextToSystem(text: String): Boolean {
        val activityVC = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null
        )
        val window =
            UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow ?: return false
        val rootViewController = window.rootViewController ?: return false
        rootViewController.presentViewController(activityVC, animated = true, completion = null)
        return true
    }

    override suspend fun launchAppRating(): Boolean {
        pruneRecentAppRatingClicks()
        recentAppRatingClicks += now()

        if (recentAppRatingClicks.size >= 3) {
            // If the user keeps clicking, probably the review modal didn't work right. Log this and
            // chuck them to the App Store.
            val deviceName = UIDevice.currentDevice.name
            val iOSVersion = UIDevice.currentDevice.systemVersion
            Analytics.iosStoreReviewControllerIssue(deviceName, iOSVersion)
            openUrl(AppStoreUrl)
            return true
        }

        SKStoreReviewController.requestReview()
        return true
    }

    private fun pruneRecentAppRatingClicks() {
        recentAppRatingClicks.removeAll { it < now() - 1.minutes }
    }
}

actual fun ExternalRoutingManager(): ExternalRoutingManager = IosExternalRoutingManager
