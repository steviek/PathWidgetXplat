package com.sixbynine.transit.path.app.external

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import kotlin.coroutines.resume

class IosExternalRoutingManager : ExternalRoutingManager {
    override suspend fun openEmail(): Boolean {
        return openUrl("mailto:$FeedbackEmail")
    }

    override suspend fun openUrl(url: String): Boolean {
        val nsUrl = NSURL.URLWithString("url") ?: return false
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
        SKStoreReviewController.requestReview()
        return true
    }
}

actual fun ExternalRoutingManager(): ExternalRoutingManager = IosExternalRoutingManager()
