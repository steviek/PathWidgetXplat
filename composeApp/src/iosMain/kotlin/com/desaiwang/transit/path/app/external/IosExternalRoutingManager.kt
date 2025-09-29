package com.desaiwang.transit.path.app.external

import com.desaiwang.transit.path.analytics.Analytics
import com.desaiwang.transit.path.time.now
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
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

    override suspend fun openUrl(url: String): Boolean = withContext(Dispatchers.Main) {
        val nsUrl = NSURL.URLWithString(url) ?: return@withContext false
        return@withContext suspendCancellableCoroutine { continuation ->
            UIApplication.sharedApplication.openURL(
                url = nsUrl,
                options = emptyMap<Any?, Any?>()
            ) { result ->
                continuation.resume(result)
            }
        }
    }

    override suspend fun shareTextToSystem(text: String): Boolean = withContext(Dispatchers.Main) {
        val activityVC = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null
        )
        val window =
            UIApplication.sharedApplication.windows.firstOrNull()
                    as? UIWindow
                ?: return@withContext false
        val rootViewController = window.rootViewController ?: return@withContext false
        rootViewController.presentViewController(activityVC, animated = true, completion = null)
        return@withContext true
    }

    override suspend fun launchAppRating(): Boolean = withContext(Dispatchers.Main) {
        pruneRecentAppRatingClicks()
        recentAppRatingClicks += now()

        if (recentAppRatingClicks.size >= 3) {
            // If the user keeps clicking, probably the review modal didn't work right. Log this and
            // chuck them to the App Store.
            val deviceName = UIDevice.currentDevice.name
            val iOSVersion = UIDevice.currentDevice.systemVersion
            Analytics.iosStoreReviewControllerIssue(deviceName, iOSVersion)
            openUrl(AppStoreUrl)
            return@withContext true
        }

        SKStoreReviewController.requestReview()
        return@withContext true
    }

    private fun pruneRecentAppRatingClicks() {
        recentAppRatingClicks.removeAll { it < now() - 1.minutes }
    }
}

actual fun ExternalRoutingManager(): ExternalRoutingManager = IosExternalRoutingManager
